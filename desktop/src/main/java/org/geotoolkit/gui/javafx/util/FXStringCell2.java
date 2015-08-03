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
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * 
 * TODO : REPLACE BY THE SAME CLASS DEFINED IN GEOTOOLKIT
 * 
 * @param <S> 
 */
public class FXStringCell2<S> extends TableCell<S, String> {
    
    private final TextField field = new TextField();

    public FXStringCell2() {
        setGraphic(field);
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.CENTER);
        field.setOnAction(event -> commitEdit(field.getText()));
    }

    @Override
    public void startEdit() {
        field.setText(getItem());
        super.startEdit();
        setText(null);
        setGraphic(field);
    }

    @Override
    public void commitEdit(String newValue) {
        setItem(newValue);
        super.commitEdit(newValue);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateItem(getItem(), false);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
        field.setText(item);
        if(item==null || empty){
            setText(null);
        }
        else {
            setText(item);
        }
    }
}

