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
package fr.sirs.ui;

import fr.sirs.util.property.SirsPreferences;
import javafx.beans.DefaultProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@DefaultProperty("stringValue")
public class IncrementCheckBox extends BorderPane {

    protected final StringProperty stringValue = new SimpleStringProperty(this, "stringValue");
    protected static final String DESCRIPTION = "Attention, l'auto-increment présente les limitations suivantes : " + System.lineSeparator()
            + "\t- Les valeurs incrémentées peuvent présenter des doublons, du fait du format distribué de la base de données," + System.lineSeparator()
            + "\t- Le calcul des valeurs incrémentales implique un surcoût non-négligeable de calcul, pouvant ralentir l'application," + System.lineSeparator()
            + "\t- La détermination des valeurs possible ne se basera que sur les désignations au format numérique." + System.lineSeparator();

    final CheckBox checkBox;

    public IncrementCheckBox() {
        checkBox = new CheckBox();
        setCenter(checkBox);

        Bindings.bindBidirectional(stringValue, checkBox.selectedProperty(), new StringConverter<Boolean>() {
            @Override
            public String toString(Boolean object) {
                return object == null? null : object.toString();
            }

            @Override
            public Boolean fromString(String string) {
                return string == null? null : Boolean.valueOf(string);
            }
        });

        checkBox.selectedProperty().setValue(
                Boolean.valueOf(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.DESIGNATION_AUTO_INCREMENT))
        );

        checkBox.selectedProperty().addListener((obs) -> {
            if (checkBox.isSelected()) {
                final Alert alert = new Alert(Alert.AlertType.WARNING, DESCRIPTION, ButtonType.CLOSE);
                alert.setResizable(true);
                alert.showAndWait();
            }
        });
    }

    public StringProperty stringValueProperty() {
        return stringValue;
    }
}
