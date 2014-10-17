

package fr.symadrem.sirs.core.component;

import fr.symadrem.sirs.core.Repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import fr.symadrem.sirs.core.model.Digue;

@View(name="all", map="function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.Digue') {emit(doc._id, doc._id)}}")
public class DigueRepository extends CouchDbRepositorySupport<Digue> implements Repository<Digue>{

    @Autowired
    public DigueRepository ( CouchDbConnector db) {
       super(Digue.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public Class<Digue> getModelClass() {
        return Digue.class;
    }
    
    @Override
    public Digue create(){
        return new Digue();
    }
   
}

