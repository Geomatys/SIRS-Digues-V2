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

import fr.sirs.SIRS;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.util.StringConverter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 *
 * @param <S> The type of the TableView generic type (i.e. S == TableView&lt;S&gt;).
 *           This should also match with the first generic type in TableColumn.
 * @param <T> The type of the item contained within the Cell.
 */
public class FXListTableCell<S, T> extends TableCell<S, T> {

    private final List<T> list;
    private final ComboBox<T> field = new ComboBox<>();
    private final StringConverter<T> converter;

    public FXListTableCell(final List<T> collection) {
        this(collection, null);
    }

    public FXListTableCell(final List<T> collection, final StringConverter<T> converter) {
        this.list = collection;
        field.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                commitEdit(field.getValue());
            }
        });

        this.converter=converter;
        if(converter!=null) field.setConverter(converter);

        setGraphic(field);
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.CENTER);
    }

    @Override
    public void startEdit() {
            field.setItems(SIRS.observableList(list));
            T value = getItem();
            field.setValue(value);
            field.getSelectionModel().select(value);
            super.startEdit();
            setText(null);
            setGraphic(field);
            field.requestFocus();
    }

    @Override
    public void commitEdit(T newValue) {
        itemProperty().set(newValue);
        super.commitEdit(newValue);
        updateItem(newValue, false);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateItem(getItem(), false);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);
        if (item != null) {
            if(converter!=null){
                setText(converter.toString(item));
            }else{
                setText(item.toString());
            }
        }
    }
}
