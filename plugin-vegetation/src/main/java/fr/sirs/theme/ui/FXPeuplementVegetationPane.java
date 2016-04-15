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

import fr.sirs.core.model.Element;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.Preview;
import static fr.sirs.plugin.vegetation.PluginVegetation.paramTraitement;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXPeuplementVegetationPane extends FXPeuplementVegetationPaneStub {
    
    public FXPeuplementVegetationPane(final PeuplementVegetation peuplementVegetation){
        super(peuplementVegetation);


        // Paramétrage du traitement lors du changement de type de peuplement
        ui_typeVegetationId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                final PeuplementVegetation peuplement = elementProperty().get();

                if (peuplement != null && newValue != null) {
                    final String typeId = (newValue instanceof Element) ? ((Element) newValue).getId() : (newValue instanceof Preview) ? ((Preview) newValue).getElementId() : null;
                    if (typeId != null) {
                        paramTraitement(PeuplementVegetation.class, peuplement, typeId);
                    }
                }
            }
        });
    }
}
