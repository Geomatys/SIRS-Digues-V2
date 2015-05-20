package fr.sirs.core.component;

import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.SIRSDocument;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class AbstractPositionDocumentRepository<T extends AbstractPositionDocumentAssociable> extends AbstractPositionableRepository<T> {

    public static final String BY_DOCUMENT_ID = "byDocumentId";
    
    public AbstractPositionDocumentRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
    }

    public List<T> getByDocument(final SIRSDocument document) {
        ArgumentChecks.ensureNonNull("Document", document);
        return this.queryView(BY_DOCUMENT_ID, document.getId());
    }

    public List<T> getByDocumentId(final String documentId) {
        ArgumentChecks.ensureNonNull("Document", documentId);
        return this.queryView(BY_DOCUMENT_ID, documentId);
    }
}
