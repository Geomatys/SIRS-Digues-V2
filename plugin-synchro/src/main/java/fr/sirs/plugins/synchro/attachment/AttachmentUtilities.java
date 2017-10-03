package fr.sirs.plugins.synchro.attachment;

import fr.sirs.SIRS;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.SIRSFileReference;
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
import java.util.logging.Level;
import javax.activation.MimetypesFileTypeMap;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import static org.ektorp.util.Documents.setRevision;

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

    public static boolean isAvailable(final CouchDbConnector connector, final SIRSFileReference document) {
        try (final AttachmentInputStream in = AttachmentUtilities.download(connector, document)) {
            return true;
        } catch (DocumentNotFoundException e) {
            SIRS.LOGGER.log(Level.FINEST, e, () -> String.format("No attachment available for document%n%s", document));
        } catch (IOException e) {
            SIRS.LOGGER.log(Level.WARNING, "An http resource cannot be closed.", e);
        }

        return false;
    }

    public static AttachmentInputStream download(final CouchDbConnector connector, final SIRSFileReference document) {
        final Element realDoc = document.getCouchDBDocument();
        if (realDoc == null) {
            throw new DocumentNotFoundException("No Root document for given object.");
        }

        final String revision = getRevision(realDoc)
                .orElseThrow(() -> new DocumentNotFoundException("No valid reference found."));
        return AttachmentUtilities.download(connector, new AttachmentReference(realDoc.getId(), revision, document.getId()));
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

    /**
     * Send a file to CouchDB server as an attachment.
     *
     * @param connector Database connection to use for upload.
     * @param ref The reference object specifies on which document we should
     * attach the uploaded file. It is mandatory, and the parent id must not be
     * null. However, if no identifier is specified for the attachment, we will
     * use the name of the provided file.
     * @param data The file to send content to CouchDb.
     * @return The revision created.
     * @throws IOException If an error occurs while analyzing or sending given
     * file content.
     */
    public static String upload(final CouchDbConnector connector, final AttachmentReference ref, final Path data) throws IOException {
        ArgumentChecks.ensureNonNull("Attachment reference", ref);
        ArgumentChecks.ensureNonNull("File to upload", data);
        try (final AttachmentInputStream ath = AttachmentUtilities.create(ref.getId(), data)) {
            if (ref.getRevision() == null || ref.getRevision().isEmpty()) {
                return connector.createAttachment(ref.getParentId(), ath);
            } else {
                return connector.createAttachment(ref.getParentId(), ref.getRevision(), ath);
            }
        }
    }

    public static void upload(final CouchDbConnector connector, final SIRSFileReference data) throws IOException {
        final Path file = SIRS.getDocumentAbsolutePath(data);
        final Element e = data.getCouchDBDocument();
        if (e == null)
            throw new IOException("Cannot upload an attachment for a document which does not exists in database.");
        String revision = getRevision(e)
                .orElseThrow(() -> new IOException("Cannot find a valid revision to uplad data"));
        revision = upload(connector, new AttachmentReference(e.getId(), revision, data.getId()), file);
        setRevision(e, revision);
    }

    private static Optional<String> getRevision(final Object input) {
        try {
            return Optional.ofNullable(input.getClass().getMethod("getRevision").invoke(input))
                    .map(Object::toString);
        } catch (SecurityException|ReflectiveOperationException ex) {
            SIRS.LOGGER.log(Level.FINE, "No revision available", ex);
        }

        return Optional.empty();
    }

    public static void delete(final CouchDbConnector connector, final SIRSFileReference ref) {
        final Element e = ref.getCouchDBDocument();
        if (e == null)
            throw new IllegalArgumentException("Cannot delete an attachment for a document which does not exists in database.");

        String revision = getRevision(e).orElseThrow(() -> new IllegalArgumentException("Cannot find a valid revision to delete attachment."));
        revision = connector.deleteAttachment(e.getId(), revision, ref.getId());
        setRevision(e, revision);
    }

    public static long size(final CouchDbConnector connector, final SIRSFileReference doc) {
        final AttachmentInputStream in = download(connector, doc);
        try {
            return in.getContentLength();
        } finally {
            // Use finally here, because bad closing is not a real problem as long
            // as we've got the size.
            try {
                in.close();
            } catch (IOException ex) {
                SIRS.LOGGER.log(Level.WARNING, "A resource cannot be closed properly.", ex);
            }
        }
    }
}
