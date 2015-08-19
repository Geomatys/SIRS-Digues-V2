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

import java.text.DecimalFormat;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.util.StringConverter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <S> The type of the TableView generic type (i.e. S == TableView&lt;S&gt;).
 *           This should also match with the first generic type in TableColumn.
 * @param <T> The type of the item contained within the Cell.
 */
public abstract class FXNumberCell2<S, T extends Number> extends TableCell<S, Number> {
    
    protected final Spinner<T> field = new Spinner<>();
    protected final T initNumber;

    public FXNumberCell2(Class<? extends Number> clazz, T initValue) {
        field.setEditable(true);
        initNumber = initValue;
        setGraphic(field);
        setAlignment(Pos.CENTER_RIGHT);
        setContentDisplay(ContentDisplay.CENTER);

        // Override "onAction" of Spinner to commitEdit.
        field.getEditor().setOnAction(action -> {
            String text = field.getEditor().getText();
            SpinnerValueFactory<T> valueFactory = field.getValueFactory();
            if (valueFactory != null) {
                StringConverter<T> converter = valueFactory.getConverter();
                if (converter != null) {
                    T value = converter.fromString(text);
                    valueFactory.setValue(value);
                }
            }
            commitEdit(field.getValue());
        });
    }

    @Override
    public void startEdit() {
        final T item = (T) getItem();
        if(item==null)
            field.getValueFactory().setValue(initNumber);
        else
            field.getValueFactory().setValue(item);
        super.startEdit();
        setText(null);
        setGraphic(field);
    }

    @Override
    public final void commitEdit(Number newValue) {
        setItem(newValue);
        super.commitEdit(newValue);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateItem(getItem(), false);
    }

    @Override
    protected void updateItem(Number item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
        if(item==null || empty){
            field.getValueFactory().setValue(initNumber);
            setText(null);
        }
        else {
            field.getValueFactory().setValue((T) item);
            setText(DecimalFormat.getNumberInstance().format(item));
        }
    }
}
