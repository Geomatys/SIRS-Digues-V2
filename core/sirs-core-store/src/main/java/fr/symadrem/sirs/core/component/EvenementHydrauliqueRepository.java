

package fr.symadrem.sirs.core.component;


import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.symadrem.sirs.core.model.EvenementHydraulique;


@Component
@View(name="all", map="function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.EvenementHydraulique') {emit(doc._id, doc._id)}}")
public class EvenementHydrauliqueRepository extends CouchDbRepositorySupport<EvenementHydraulique> {

    @Autowired
    public EvenementHydrauliqueRepository ( CouchDbConnector db) {
		super(EvenementHydraulique.class, db);
		initStandardDesignDocument();
	}
   
}

