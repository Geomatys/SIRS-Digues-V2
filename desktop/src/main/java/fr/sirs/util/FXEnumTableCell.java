package fr.sirs.util;

import java.util.Arrays;
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
 * @author Samutl Andrés (Geomatys)
 * @param <S>
 * @param <T>
 */
public class FXEnumTableCell<S, T extends Enum> extends TableCell<S, T> {
    
    private final Class<T> enumClass;
    private final ComboBox<T> field = new ComboBox<>();
    private final StringConverter<T> converter;
    
    public FXEnumTableCell(final Class<T> enumClass) {
        this(enumClass, null);
    }

    public FXEnumTableCell(final Class<T> enumClass, final StringConverter<T> converter) {
        this.enumClass = enumClass;
        this.converter=converter;
        
        field.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                commitEdit(field.getValue());
            }
        });
        
        if(converter!=null){
            field.setConverter(converter);
        }
        
        setGraphic(field);
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.CENTER);
    }

    @Override
    public void startEdit() {
            T[] values = (T[]) enumClass.getEnumConstants();
            field.setItems(FXCollections.observableArrayList(Arrays.asList(values)));
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
            } else{
                setText(item.toString());
            }
        }
    }
}
