

package fr.sirs.core.component;

import fr.sirs.core.Repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import java.util.List;
import org.ektorp.support.Views;


@Component
@Views({
    @View(name="all",         map="function(doc) {if(doc['@class']=='fr.sirs.core.model.SystemeReperage') {emit(doc._id, doc._id)}}"),
    @View(name="byTronconId", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.SystemeReperage') {emit(doc.tronconId, doc._id)}}")
})
public class SystemeReperageRepository extends CouchDbRepositorySupport<SystemeReperage> implements Repository<SystemeReperage>{

    @Autowired
    public SystemeReperageRepository ( CouchDbConnector db) {
       super(SystemeReperage.class, db);
       initStandardDesignDocument();
   }
    
    public Class<SystemeReperage> getModelClass() {
        return SystemeReperage.class;
    }
    
    public SystemeReperage create(){
        return new SystemeReperage();
    }
    
    public List<SystemeReperage> getByTroncon(final TronconDigue troncon) {
        return this.queryView("byTronconId", troncon.getId());
    }
   
}

