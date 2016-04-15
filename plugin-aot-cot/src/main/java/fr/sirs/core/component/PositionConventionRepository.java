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
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.PositionConvention;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets PositionConvention.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Views ({
@View(name=AbstractPositionDocumentRepository.BY_DOCUMENT_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PositionConvention') {emit(doc.sirsdocument, doc._id)}}"),
@View(name=PositionConventionRepository.BY_OBJET_ID, map="classpath:positionsConventionsByObjetId.js")
})
@Component("fr.sirs.core.component.PositionConventionRepository")
public class PositionConventionRepository extends 
AbstractPositionDocumentRepository
<PositionConvention> {
        
    public static final String BY_OBJET_ID = "byObjetId";
    
    @Autowired
    private PositionConventionRepository ( CouchDbConnector db) {
       super(PositionConvention.class, db);
       initStandardDesignDocument();
   }
    
    public List<PositionConvention> getByObjet(final Objet objet) {
        ArgumentChecks.ensureNonNull("objet", objet);
        return this.queryView(BY_OBJET_ID, objet.getId());
    }
    
    @Override
    public PositionConvention create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(PositionConvention.class);
    }
}

