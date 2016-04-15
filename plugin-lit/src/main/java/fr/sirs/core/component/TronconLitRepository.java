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


import static fr.sirs.core.component.TronconLitRepository.BY_LIT_ID;
import fr.sirs.core.model.Lit;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.TronconLit;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets TronconLit.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */

@View(name = BY_LIT_ID, map = "function(doc) {if(doc['@class'] && doc.litId) {emit(doc.litId, doc._id)}}")
@Component
public class TronconLitRepository extends AbstractTronconDigueRepository<TronconLit> {

    public static final String BY_LIT_ID = "byLitId";

    @Autowired
    private TronconLitRepository ( CouchDbConnector db) {
       super(db, TronconLit.class);
   }

    public List<TronconLit> getByLit(final Lit lit) {
        ArgumentChecks.ensureNonNull("Lit parent", lit);
        return this.queryView(BY_LIT_ID, lit.getId());
    }
}

