/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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
