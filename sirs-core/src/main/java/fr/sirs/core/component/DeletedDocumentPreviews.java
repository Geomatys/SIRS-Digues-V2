package fr.sirs.core.component;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.stereotype.Component;

@Views({
        @View(name = DeletedDocumentPreviews.DELETED_DOCUMENT, map = "classpath:Deleted-document.js"),
})
@Component
public class DeletedDocumentPreviews extends CouchDbRepositorySupport<DeletedCouchDbDocument> {
     static final String DELETED_DOCUMENT ="deleted_documents";

    protected DeletedDocumentPreviews(CouchDbConnector db) {
        super(DeletedCouchDbDocument.class, db, false);
    }

}