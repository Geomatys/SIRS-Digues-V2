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
import static fr.sirs.core.component.OrganeProtectionRepository.BY_AMENAGEMENT_HYDRAULIQUE_ID;
import fr.sirs.core.model.OrganeProtectionCollective;

/**
 *
 * @author maximegavens
 */
@View(name=BY_AMENAGEMENT_HYDRAULIQUE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.OrganeProtectionCollective') {emit(doc.amenagementHydrauliqueId, doc._id)}}")
@Component("fr.sirs.core.component.OrganeProtectionRepository")
public class OrganeProtectionRepository extends DescriptionAmenagementHydrauliqueRepository<OrganeProtectionCollective> {

    public static final String BY_AMENAGEMENT_HYDRAULIQUE_ID = "byAmenagementHydrauliqueId";

    @Autowired
    private OrganeProtectionRepository ( CouchDbConnector db) {
       super(OrganeProtectionCollective.class, db);
       initStandardDesignDocument();
   }

    @Override
    public OrganeProtectionCollective create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(OrganeProtectionCollective.class);
    }
}
