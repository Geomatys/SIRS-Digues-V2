package fr.sirs.theme.ui;

import fr.sirs.util.SirsStringConverter;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T> The type of retrieved element
 * @param <C> The type of ComboBox items
 */
public abstract class PojoTableComboBoxChoiceStage<T, C> extends PojoTableChoiceStage<T> {

    protected final ComboBox<C> comboBox;

    protected PojoTableComboBoxChoiceStage(){
        comboBox = new ComboBox<>();
        comboBox.setConverter(new SirsStringConverter());
    }
}
