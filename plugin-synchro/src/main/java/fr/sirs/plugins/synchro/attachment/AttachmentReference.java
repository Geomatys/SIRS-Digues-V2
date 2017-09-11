package fr.sirs.plugins.synchro.attachment;

import fr.sirs.core.model.Identifiable;

/**
 * Give information needed for attachment recovery. I.e : Its identifier, along
 * with the identifier of its parent document and revision.
 *
 * @author Alexis Manin (Geomatys)
 */
public class AttachmentReference implements Identifiable {

    private String parentId;
    private String revision;
    private String id;

    public AttachmentReference() {
    }

    public AttachmentReference(String parentId, String id) {
        this.parentId = parentId;
        this.id = id;
    }

    public AttachmentReference(String parentId, String revision, String id) {
        this.parentId = parentId;
        this.revision = revision;
        this.id = id;
    }

    /**
     *
     * @return Identifier of the document on which is located the attachment.
     */
    public String getParentId() {
        return parentId;
    }

    /**
     *
     * @param parentId Identifier of the document on which the attachment can be found.
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     *
     * @return The revision of the document to search in.
     */
    public String getRevision() {
        return revision;
    }

    /**
     *
     * @param revision The revision to search in.
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     *
     * @return Identifier of the wanted attachment.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     *
     * @param id Identifier of the target attachment.
     */
    public void setId(String id) {
        this.id = id;
    }
}
