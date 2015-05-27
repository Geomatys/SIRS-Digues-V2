

package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeExecption;
import fr.sirs.core.model.ElementCreator;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.support.Views;


@Component
@Views({
    @View(name="all",         map="function(doc) {if(doc['@class']=='fr.sirs.core.model.SystemeReperage') {emit(doc._id, doc._id)}}"),
    @View(name="byTronconId", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.SystemeReperage') {emit(doc.linearId, doc._id)}}")
})
public class SystemeReperageRepository extends AbstractSIRSRepository<SystemeReperage>{

    @Autowired
    public SystemeReperageRepository ( CouchDbConnector db) {
       super(SystemeReperage.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public Class<SystemeReperage> getModelClass() {
        return SystemeReperage.class;
    }
    
    @Override
    public SystemeReperage create(){
        final SessionGen session = InjectorCore.getBean(SessionGen.class);
        if(session!=null && session instanceof SessionCore){
            final ElementCreator elementCreator = ((SessionCore) session).getElementCreator();
            return elementCreator.createElement(SystemeReperage.class);
        } else {
            throw new SirsCoreRuntimeExecption("Pas de session courante");
        }
//        return new SystemeReperage();
    }
    
    public List<SystemeReperage> getByTroncon(final TronconDigue troncon) {
        return this.queryView("byTronconId", troncon.getId());
    }
    
    public void update(SystemeReperage entity, TronconDigue troncon) {
        ArgumentChecks.ensureNonNull("SR to update", entity);
        ArgumentChecks.ensureNonNull("Troncon bound to updated SR", troncon);
        super.update(entity);
        constraintBorneInTronconListBorne(entity, troncon, false);
    }
    
    public void add(SystemeReperage entity, TronconDigue troncon) {
        add(entity, troncon, false);
    }
    
    public void add(SystemeReperage entity, TronconDigue troncon, final boolean forceDefaultSR) {
        ArgumentChecks.ensureNonNull("SR to add", entity);
        ArgumentChecks.ensureNonNull("Troncon bound to added SR", troncon);
        if (entity.getLinearId() == null && troncon != null) {
            entity.setLinearId(troncon.getId());
        }
        super.add(entity);
        constraintBorneInTronconListBorne(entity,troncon, forceDefaultSR);
    }

    @Override
    public void update(SystemeReperage entity) {
        throw new UnsupportedOperationException("Operation interdite : le SR doit être mis à jour en même temps que le tronçon associé.");
    }

    @Override
    public void remove(SystemeReperage entity) {
        throw new UnsupportedOperationException("Operation interdite : le SR doit être mis à jour en même temps que le tronçon associé.");
    }

    @Override
    public void add(SystemeReperage entity) {
        throw new UnsupportedOperationException("Operation interdite : le SR doit être mis à jour en même temps que le tronçon associé.");
    }
   
    public void remove(SystemeReperage source, TronconDigue troncon) {
        ArgumentChecks.ensureNonNull("SR to delete", source);
        ArgumentChecks.ensureNonNull("Troncon bound to deleted SR", troncon);
        if (source.getId().equals(troncon.getSystemeRepDefautId())) {
            troncon.setSystemeRepDefautId(null);
            new TronconDigueRepository(db).update(troncon);
        }
        super.remove(source);
    }
    
    /**
     * Cette contrainte s'assure que les bornes du systeme de reperage sont
     * dans la liste des bornes du troncon.
     * 
     * @param entity 
     */
    private void constraintBorneInTronconListBorne(SystemeReperage entity, TronconDigue troncon, final boolean forceDefaultSR) {
        final String tcId = entity.getLinearId();
        if(tcId==null) return;
        if(entity.getSystemeReperageBornes().isEmpty()) return;
        
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
        for(SystemeReperageBorne srb : entity.getSystemeReperageBornes()){
            final String bid = srb.getBorneId();
            if(bid!=null && !borneIds.contains(bid)){
                borneIds.add(bid);
                needSave = true;
            }
        }
        
        if (troncon.getSystemeRepDefautId() == null || troncon.getSystemeRepDefautId().isEmpty() || forceDefaultSR) {
            troncon.setSystemeRepDefautId(entity.getId());
            needSave = true;
        }
        
        if(needSave){
            tcRepo.update(troncon);
        }
    }
    
}

