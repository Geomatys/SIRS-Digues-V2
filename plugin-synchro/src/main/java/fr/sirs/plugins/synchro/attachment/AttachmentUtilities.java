package fr.sirs.plugins.synchro.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.activation.MimetypesFileTypeMap;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class AttachmentUtilities {

    private static final List<Function<Path, String>> MIME_STRATEGIES = Arrays.asList(
            AttachmentUtilities::cTypeFromNio,
            AttachmentUtilities::cTypeFromURL,
            AttachmentUtilities::cTypeFromJavax
    );

    /**
     * Create a data stream from given file. The stream is specialized for
     * attachment creation on CouchDB server.
     *
     * @param identifier The identifier to affect to created attachment. If null,
     * the file name will be used as identifier.
     * @param input Path to the file to access/send to CouchDB.
     * @return A stream ready to be sent to CouchDB.
     * @throws IOException If we cannot access given file, or we cannot guess its
     * mime type, or if it's an empty file.
     */
    public static AttachmentInputStream create(String identifier, final Path input) throws IOException {
        final long fileSize = Files.size(input);
        if (fileSize <= 0) {
            throw new IOException("Cannot create an attachment from empty file : " + input);
        }
        final String mime = guessMimeType(input)
                .orElseThrow(() -> new IOException("Cannot find a mime-type for " + input));
        if (identifier == null) {
            identifier = input.getFileName().toString();
        }
        final InputStream fileInput = Files.newInputStream(input);
        try {
            return new AttachmentInputStream(identifier, fileInput, mime, fileSize);
        } catch (Exception e) {
            try {
                fileInput.close();
            } catch (Exception bis) {
                e.addSuppressed(bis);
            }
            throw e;
        }
    }

    /**
     * Try to deduce the mime-type of the given file.
     *
     * @param input The file (must not be a directory, read access is needed) to
     * find mime-type for.
     * @return A string representing the deduced mime-type.
     * @throws java.io.IOException If an error occured while analysing given file.
     */
    public static Optional<String> guessMimeType(final Path input) throws IOException {
        String cType = null;
        final Iterator<Function<Path, String>> tries = MIME_STRATEGIES.iterator();
        while (cType == null && tries.hasNext()) {
            try {
                cType = tries.next().apply(input);
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }

        return Optional.ofNullable(cType);
    }

    private static String cTypeFromNio(final Path input) {
        try {
            return Files.probeContentType(input);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String cTypeFromURL(final Path input) {
        try (final InputStream stream = Files.newInputStream(input)) {
            return URLConnection.guessContentTypeFromStream(stream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String cTypeFromJavax(final Path input) {
        return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(input.toFile());
    }

    public static AttachmentInputStream download(final CouchDbConnector connector, final AttachmentReference ref) {
        ArgumentChecks.ensureNonNull("Attachment reference", ref);
        ArgumentChecks.ensureNonEmpty("Document id", ref.getParentId());
        ArgumentChecks.ensureNonEmpty("Attachment id", ref.getId());

        final AttachmentInputStream stream;
        if (ref.getRevision() == null || ref.getRevision().isEmpty()) {
            stream = connector.getAttachment(ref.getParentId(), ref.getId());
        } else {
            stream = connector.getAttachment(ref.getParentId(), ref.getId(), ref.getRevision());
        }

        return stream;
    }
}
