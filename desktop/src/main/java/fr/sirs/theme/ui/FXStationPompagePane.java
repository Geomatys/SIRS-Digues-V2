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
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.model.ObservationStationPompage;
import fr.sirs.core.model.StationPompage;
import fr.sirs.theme.ui.pojotable.PojoTableExternalAddable;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXStationPompagePane extends FXStationPompagePaneStub {

    public FXStationPompagePane(final StationPompage ouvrage){
        super(ouvrage);

        /*

        /!\/!\/!\ HACK /!\/!\/!\ HACK /!\/!\/!\ HACK /!\/!\/!\ HACK /!\/!\/!\

        SYM-1756 : on souhaite ajouter le même hack que SYM-1727 pour les tableaux d'observations de désordres.
        */

        ui_observations.setContent(() -> {
            observationsTable = new PojoTableExternalAddable(ObservationStationPompage.class, elementProperty());
            observationsTable.editableProperty().bind(disableFieldsProperty().not());
            updateObservationsTable(Injector.getSession(), elementProperty.get());
            return observationsTable;
        });

    }
}
