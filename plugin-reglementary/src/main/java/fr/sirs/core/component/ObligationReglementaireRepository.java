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

import fr.sirs.core.model.PlanificationObligationReglementaire;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.ObligationReglementaire;

import java.util.List;


/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets ObligationReglementaire.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 * @author Cédric Briançon  (Geomatys)
 */
@View(name = ObligationReglementaireRepository.OBLIGATIONS_FOR_PLANIF, map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.ObligationReglementaire') {emit(doc.planifId, doc._id)}}")
@Component("fr.sirs.core.component.ObligationReglementaireRepository")
public class ObligationReglementaireRepository extends
        AbstractSIRSRepository
                <ObligationReglementaire> {

    /**
     * Nom de la vue permettant de récupérer les obligations associées à une planification.
     */
    public static final String OBLIGATIONS_FOR_PLANIF = "obligationsForPlanif";

    @Autowired
    public ObligationReglementaireRepository(CouchDbConnector db) {
        super(ObligationReglementaire.class, db);
        initStandardDesignDocument();
    }

    @Override
    public ObligationReglementaire create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(ObligationReglementaire.class);
    }

    /**
     * Récupère l'ensemble des obligations pour la planification fournie en paramètre.
     *
     * @param planif Planification pour laquelle on souhaite récupérer les obligations associées.
     * @return La liste des obligations connectées à cette planification.
     */
    public List<ObligationReglementaire> getByPlanification(final PlanificationObligationReglementaire planif) {
        ArgumentChecks.ensureNonNull("Obligation parent", planif);
        return this.queryView(OBLIGATIONS_FOR_PLANIF, planif.getId());
    }
}
