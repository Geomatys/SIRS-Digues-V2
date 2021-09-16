/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import static fr.sirs.core.component.TraitBergeRepository.BY_BERGE_ID;
import fr.sirs.core.model.Berge;
import fr.sirs.core.model.TraitBerge;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets TraitBerge.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@View(name=BY_BERGE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.TraitBerge') {emit(doc.bergeId, doc._id)}}")
@Component("fr.sirs.core.component.TraitBergeRepository")
public class TraitBergeRepository extends 
AbstractSIRSRepository
<TraitBerge> {

    public static final String BY_BERGE_ID = "byBergeId";
        
    @Autowired
    private TraitBergeRepository ( CouchDbConnector db) {
       super(TraitBerge.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public TraitBerge create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(TraitBerge.class);
    }

    public List<TraitBerge> getByBergeId(final String planId) {
        ArgumentChecks.ensureNonNull("Berge id", planId);
        return this.queryView(BY_BERGE_ID, planId);
    }

    public List<TraitBerge> getByBerge(final Berge berge) {
        ArgumentChecks.ensureNonNull("Berge", berge);
        return getByBergeId(berge.getId());
    }
}

