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

import java.time.LocalDate;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * 
 * TODO : REPLACE BY THE SAME CLASS DEFINED IN GEOTOOLKIT
 * 
 * @param <S> 
 */
public class FXLocalDateCell<S> extends TableCell<S, LocalDate> {
    public static final Image ICON_REMOVE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TIMES_CIRCLE, 16, FontAwesomeIcons.DEFAULT_COLOR), null);
    private final Button del = new Button(null, new ImageView(ICON_REMOVE));
    private final DatePicker field = new DatePicker();
    private final BorderPane pane = new BorderPane(field, null, del, null, null);

    public FXLocalDateCell() {
        setGraphic(field);
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.CENTER);
        del.setPrefSize(16, 16);
        del.setFocusTraversable(false);
        del.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (isEditing()) {
                    commitEdit(null);
                }
            }
        });
        del.setStyle("-fx-background-color:transparent; -fx-focus-color: transparent;");
        field.setOnAction(event -> commitEdit(field.getValue()));
    }

    @Override
    public void startEdit() {
        LocalDate time = getItem();
        if (time == null) {
            time = LocalDate.now();
        }
        field.setValue(time);
        super.startEdit();
        setText(null);
        setGraphic(pane);
        field.requestFocus();
    }

    @Override
    public void commitEdit(LocalDate newValue) {
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
    protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
        if(item==null || empty){
            setText(null);
        }
        else {
            setText(item.toString());
        }
    }
}

