

package fr.symadrem.sirs.core.component;


import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.symadrem.sirs.core.model.Contact;


@Component
@View(name="all", map="function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.Contact') {emit(doc._id, doc._id)}}")
public class ContactRepository extends CouchDbRepositorySupport<Contact> {

    @Autowired
    public ContactRepository ( CouchDbConnector db) {
		super(Contact.class, db);
		initStandardDesignDocument();
	}
   
}

