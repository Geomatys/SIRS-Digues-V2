

package fr.symadrem.sirs.core.component;


import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.symadrem.sirs.core.model.Digue;


@Component
@View(name="all", map="function(doc) {if(doc.type=='Digue') {emit(doc._id, doc._id)}}")
public class DigueRepository extends CouchDbRepositorySupport<Digue> {

    @Autowired
    public DigueRepository ( CouchDbConnector db) {
		super(Digue.class, db);
		initStandardDesignDocument();
	}
   
}

