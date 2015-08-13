package fr.sirs.core.component;

import fr.sirs.core.SessionCore;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.SIRSDocument;
import java.util.Collection;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class AbstractPositionDocumentRepository<T extends AbstractPositionDocumentAssociable> extends AbstractPositionableRepository<T> {

    public static final String BY_DOCUMENT_ID = "byDocumentId";

    protected AbstractPositionDocumentRepository(Class<T> type, CouchDbConnector db) {
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

    /**
     * Return the AbstractPositionDocumentAssociable linked to one document
     * specified by the given id.
     * 
     * If you know the AbstractPositionDocumentAssociable concrete class, 
     * prefer to use :
     * getPositionDocumentByDocumentId(final Class<T> targetClass, final String documentId, final SessionCore session)
     *
     * @param documentId
     * @param session
     * @return
     */
    public static ObservableList<? extends AbstractPositionDocumentAssociable> getPositionDocumentByDocumentId(final String documentId, final SessionCore session){
        ObservableList<? extends AbstractPositionDocumentAssociable> result = null;
        final Collection<AbstractSIRSRepository> candidateRepos = session.getRepositoriesForClass(AbstractPositionDocumentAssociable.class);
        for(AbstractSIRSRepository candidateRepo : candidateRepos){
            if(candidateRepo instanceof AbstractPositionDocumentRepository){
                result = FXCollections.observableList(((AbstractPositionDocumentRepository) candidateRepo).getByDocumentId(documentId));
                if(!result.isEmpty()) return result; // Si la liste n'est pas vide c'est qu'on a trouvé le bon repo et on sort donc de la boucle en renvoyant la liste.
            }
        }
        /*
        Si aucun repo n'a été trouvé (ce qui est normalement impossible étant
        donné le modèle, on renvoie null. Si des repos ont été trouvés mais qu'
        on arrive tout de même à ce point c'est qu'ils ont tous renvoyé une
        liste vide. Parmi elles, la dernière est renvoyée.
        */
        return result;
    }

    /**
     * Return the AbstractPositionDocumentAssociable linked to one document
     * specified by the given id.
     *
     * @param <T>
     * @param targetClass
     * @param documentId
     * @param session
     * @return
     */
    public static <T extends AbstractPositionDocumentAssociable> ObservableList<T> getPositionDocumentByDocumentId(final Class<T> targetClass, final String documentId, final SessionCore session){
        ObservableList<T> result = null;
        final AbstractSIRSRepository<T> repo = session.getRepositoryForClass(targetClass);
        if(repo instanceof AbstractPositionDocumentRepository){
            result = FXCollections.observableList(((AbstractPositionDocumentRepository<T>) repo).getByDocumentId(documentId));
        }
        return result;
    }
}
