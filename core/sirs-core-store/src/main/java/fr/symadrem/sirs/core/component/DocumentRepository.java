

package fr.symadrem.sirs.core.component;


import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.symadrem.sirs.core.model.Document;


@Component
@View(name="all", map="function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.Document') {emit(doc._id, doc._id)}}")
public class DocumentRepository extends CouchDbRepositorySupport<Document> {

    @Autowired
    public DocumentRepository ( CouchDbConnector db) {
		super(Document.class, db);
		initStandardDesignDocument();
	}
   
}

