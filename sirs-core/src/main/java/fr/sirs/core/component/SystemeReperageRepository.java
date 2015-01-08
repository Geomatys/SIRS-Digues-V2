

package fr.sirs.core.component;

import fr.sirs.core.Repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import java.util.List;
import org.ektorp.DocumentNotFoundException;
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

    @Override
    public void update(SystemeReperage entity) {
        update(entity,null);
    }
    
    public void update(SystemeReperage entity, TronconDigue troncon) {
        super.update(entity);
        constraintBorneInTronconListBorne(entity,troncon);
    }

    @Override
    public void add(SystemeReperage entity) {
        add(entity,null);
    }
    
    public void add(SystemeReperage entity, TronconDigue troncon) {
        super.add(entity);
        constraintBorneInTronconListBorne(entity,troncon);
    }
   
    /**
     * Cette contraint s'assure que les bornes du systeme de reperage sont
     * dans la liste des bornes du troncon.
     * 
     * @param entity 
     */
    private void constraintBorneInTronconListBorne(SystemeReperage entity, TronconDigue troncon){
        final String tcId = entity.getTronconId();
        if(tcId==null) return;
        if(entity.getSystemereperageborneId().isEmpty()) return;
        
        final TronconDigueRepository tcRepo = new TronconDigueRepository(db);
        if(troncon==null){
            try{
                troncon = tcRepo.get(tcId);
            }catch(DocumentNotFoundException ex){
                //le troncon n'existe pas
                return;
            }
        }
        
        final List<String> borneIds = troncon.getBorneIds();
        
        boolean needSave = false;
        for(SystemeReperageBorne srb : entity.getSystemereperageborneId()){
            final String bid = srb.getBorneId();
            if(bid!=null && !borneIds.contains(bid)){
                borneIds.add(bid);
                needSave = true;
            }
        }
        
        if(needSave){
            tcRepo.update(troncon);
        }
        
    }
    
}

