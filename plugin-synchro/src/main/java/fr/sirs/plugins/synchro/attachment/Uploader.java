package fr.sirs.plugins.synchro.attachment;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class Uploader {

    final CouchDbConnector connector;

    Uploader(final CouchDbConnector connector) {
        ArgumentChecks.ensureNonNull("Database connector", connector);
        this.connector = connector;
    }

    /**
     * Send a file to CouchDB server as an attachment.
     *
     * @param ref The reference object specifies on which document we should
     * attach the uploaded file. It is mandatory, and the parent id must not be
     * null. However, if no identifier is specified for the attachment, we will
     * use the name of the provided file.
     * @param data The file to send content to CouchDb.
     * @throws IOException If an error occurs while analyzing or sending given
     * file content.
     */
    public void upload(final AttachmentReference ref, final Path data) throws IOException {
        ArgumentChecks.ensureNonNull("Attachment reference", ref);
        ArgumentChecks.ensureNonNull("File to upload", data);
        try (final AttachmentInputStream ath = AttachmentUtilities.create(ref.getId(), data)) {
            if (ref.getRevision() == null || ref.getRevision().isEmpty()) {
                connector.createAttachment(ref.getParentId(), ath);
            } else {
                connector.createAttachment(ref.getParentId(), ref.getRevision(), ath);
            }
        }
    }
}
