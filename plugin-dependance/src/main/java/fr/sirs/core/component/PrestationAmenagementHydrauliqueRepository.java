/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static fr.sirs.core.component.PrestationAmenagementHydrauliqueRepository.BY_AMENAGEMENT_HYDRAULIQUE_ID;
import fr.sirs.core.model.PrestationAmenagementHydraulique;

/**
 *
 * @author maximegavens
 */
@View(name=BY_AMENAGEMENT_HYDRAULIQUE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PrestationAmenagementHydraulique') {emit(doc.amenagementHydrauliqueId, doc._id)}}")
@Component("fr.sirs.core.component.PrestationAmenagementHydrauliqueRepository")
public class PrestationAmenagementHydrauliqueRepository extends DescriptionAmenagementHydrauliqueRepository<PrestationAmenagementHydraulique> {

    public static final String BY_AMENAGEMENT_HYDRAULIQUE_ID = "byAmenagementHydrauliqueId";

    @Autowired
    private PrestationAmenagementHydrauliqueRepository( CouchDbConnector db) {
       super(PrestationAmenagementHydraulique.class, db);
       initStandardDesignDocument();
   }

    @Override
    public PrestationAmenagementHydraulique create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(PrestationAmenagementHydraulique.class);
    }
}
