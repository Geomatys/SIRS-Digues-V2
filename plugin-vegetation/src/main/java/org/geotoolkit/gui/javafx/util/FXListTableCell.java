package org.geotoolkit.gui.javafx.util;

import java.util.List;
import javafx.collections.FXCollections;
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
            field.setItems(FXCollections.observableList(list));
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