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
package org.geotoolkit.gui.javafx.util;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;

/**
 * A simple table cell allowing to edit a String value using a text field.
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXStringCell<S> extends FXTableCell<S, String> {

    private final TextField field = new TextField();

    public FXStringCell() {
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.CENTER);
        field.setOnAction(event -> commitEdit(field.getText()));

        // Remove editor from display every time edition is cancelled / finished.
        editingProperty().addListener((obs, oldValue, newValue) -> {
            if (oldValue && !newValue)
                setGraphic(null);
        });
    }

    @Override
    public void terminateEdit() {
        commitEdit(field.getText());
    }

    @Override
    public void cancelEdit() {
        setText(getItem());
        super.cancelEdit();
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setGraphic(field);
        field.setText(getItem());
        field.requestFocus();
        setText(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
        } else {
            setText(item);
        }
    }
}
