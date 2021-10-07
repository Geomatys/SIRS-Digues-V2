/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.DesordreDependance;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static fr.sirs.core.component.DesordreDependanceRepository.BY_AMENAGEMENT_HYDRAULIQUE_ID;

/**
 *
 * @author maximegavens
 */
@View(name=BY_AMENAGEMENT_HYDRAULIQUE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.DesordreDependance') {emit(doc.amenagementHydrauliqueId, doc._id)}}")
@Component("fr.sirs.core.component.DesordreDependanceRepository")
public class DesordreDependanceRepository extends AbstractAmenagementHydrauliqueRepository<DesordreDependance> {

    public static final String BY_AMENAGEMENT_HYDRAULIQUE_ID = "byAmenagementHydrauliqueId";

    @Autowired
    private DesordreDependanceRepository ( CouchDbConnector db) {
       super(DesordreDependance.class, db);
       initStandardDesignDocument();
   }

    @Override
    public DesordreDependance create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(DesordreDependance.class);
    }
}
