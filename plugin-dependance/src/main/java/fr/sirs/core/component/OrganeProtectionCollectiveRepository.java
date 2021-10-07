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
import static fr.sirs.core.component.OrganeProtectionCollectiveRepository.BY_AMENAGEMENT_HYDRAULIQUE_ID;
import fr.sirs.core.model.OrganeProtectionCollective;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
@View(name=BY_AMENAGEMENT_HYDRAULIQUE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.OrganeProtectionCollective') {emit(doc.amenagementHydrauliqueId, doc._id)}}")
@Component("fr.sirs.core.component.OrganeProtectionCollectiveRepository")
public class OrganeProtectionCollectiveRepository extends AbstractAmenagementHydrauliqueRepository<OrganeProtectionCollective> {

    @Autowired
    private OrganeProtectionCollectiveRepository ( CouchDbConnector db) {
       super(OrganeProtectionCollective.class, db);
       initStandardDesignDocument();
   }

    @Override
    public OrganeProtectionCollective create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(OrganeProtectionCollective.class);
    }
}
