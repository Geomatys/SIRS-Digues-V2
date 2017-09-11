package fr.sirs.plugins.synchro.attachment;

import fr.sirs.Session;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.AttachmentInputStream;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class Downloader {

    @Autowired
    Session session;


    public AttachmentInputStream download(final AttachmentReference ref) {
        ArgumentChecks.ensureNonNull("Attachment reference", ref);
        ArgumentChecks.ensureNonEmpty("Document id", ref.getParentId());
        ArgumentChecks.ensureNonEmpty("Attachment id", ref.getId());

        final AttachmentInputStream stream;
        if (ref.getRevision() == null || ref.getRevision().isEmpty()) {
            stream = session.getConnector().getAttachment(ref.getParentId(), ref.getId());
        } else {
            stream = session.getConnector().getAttachment(ref.getParentId(), ref.getId(), ref.getRevision());
        }

        return stream;
    }
}
