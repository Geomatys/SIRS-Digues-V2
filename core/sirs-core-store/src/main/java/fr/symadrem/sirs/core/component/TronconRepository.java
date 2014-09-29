

package fr.symadrem.sirs.core.component;


import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.symadrem.sirs.core.model.Troncon;


@Component
@View(name="all", map="function(doc) {if(doc.type=='Troncon') {emit(doc._id, doc._id)}}")
public class TronconRepository extends CouchDbRepositorySupport<Troncon> {

    @Autowired
    public TronconRepository ( CouchDbConnector db) {
		super(Troncon.class, db);
		initStandardDesignDocument();
	}
   
}

