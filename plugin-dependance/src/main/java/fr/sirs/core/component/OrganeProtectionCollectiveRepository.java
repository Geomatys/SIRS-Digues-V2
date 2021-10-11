/*
 * This file is part of SIRS-Digues 2.
 *
 *  Copyright (C) 2021, FRANCE-DIGUES,
 *
 *  SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any
 *  later version.
 *
 *  SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
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
@View(name=BY_AMENAGEMENT_HYDRAULIQUE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.OrganeProtectionCollective') { if(doc.amenagementHydrauliqueId==null) {emit('-1', doc._id)} else {emit(doc.amenagementHydrauliqueId, doc._id)}}}")
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
