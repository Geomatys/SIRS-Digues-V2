/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */

package fr.sirs;

import fr.sirs.core.model.RefSuiteApporter;
import fr.sirs.util.CheckboxMenuButton;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.SirsPreferences;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.util.List;
public class FXSuiteApporterPredicater extends VBox {

    @FXML
    protected CheckboxMenuButton uiOptionSuiteAApporter;

    public FXSuiteApporterPredicater() {
        SIRS.loadFXML(this, FXSuiteApporterPredicater.class);
        uiOptionSuiteAApporter.setItems(RefSuiteApporter.class);

        SirsStringConverter converter = new SirsStringConverter();
        SirsPreferences.INSTANCE.showCasePropProperty().addListener((c, o, n) -> uiOptionSuiteAApporter.updateTextsToSirsPreferences(converter));
    }

    protected void addListener(final InvalidationListener parameterListener) {
        uiOptionSuiteAApporter.addListenerToItems(parameterListener);
    }

    protected List<String> getCheckedItems() {
        return uiOptionSuiteAApporter.getCheckedItems();
    }
}
