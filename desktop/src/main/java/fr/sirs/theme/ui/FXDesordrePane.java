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
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.RefCategorieDesordre;
import fr.sirs.core.model.RefTypeDesordre;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDesordrePane extends FXDesordrePaneStub {

    public FXDesordrePane(final Desordre desordre){
        super(desordre);

        // Update available types according to chosen category.
        final AbstractSIRSRepository<RefTypeDesordre> typeRepo = Injector.getSession().getRepositoryForClass(RefTypeDesordre.class);
        ui_categorieDesordreId.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {

            final ObservableList<RefTypeDesordre> typeList = SIRS.observableList(typeRepo.getAll());
            if (newValue instanceof RefCategorieDesordre) {
                final FilteredList fList = typeList.filtered(type -> type.getCategorieId().equals(((RefCategorieDesordre) newValue).getId()));
                ui_typeDesordreId.setItems(fList);
            } else {
                ui_typeDesordreId.setItems(typeList);
            }
        });
    }
}
