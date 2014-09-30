

package fr.symadrem.sirs.core.component;


import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.symadrem.sirs.core.model.TronconDigue;


@Component
@View(name="all", map="function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.TronconDigue') {emit(doc._id, doc._id)}}")
public class TronconDigueRepository extends CouchDbRepositorySupport<TronconDigue> {

    @Autowired
    public TronconDigueRepository ( CouchDbConnector db) {
		super(TronconDigue.class, db);
		initStandardDesignDocument();
	}
   
}

