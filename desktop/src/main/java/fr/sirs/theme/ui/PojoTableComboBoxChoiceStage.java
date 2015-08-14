package fr.sirs.theme.ui;

import fr.sirs.util.SirsStringConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T> The type of retrieved element
 * @param <C> The type of ComboBox items
 */
public abstract class PojoTableComboBoxChoiceStage<T, C> extends PojoTableChoiceStage<T> {

    protected final ComboBox<C> comboBox = new ComboBox<>();

    protected final Button cancel = new Button("Annuler");
    protected final Button add = new Button("Ajouter");

    protected PojoTableComboBoxChoiceStage(){
        comboBox.setConverter(new SirsStringConverter());

        cancel.setOnAction((ActionEvent event) -> {
                retrievedElement.unbind();
                retrievedElement.set(null);
                hide();
        });
        add.setOnAction((ActionEvent event) -> {
                hide();
        });
        
        final HBox hBox = new HBox(cancel, add);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(20));

        final VBox vBox = new VBox(comboBox, hBox);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(20));
        setScene(new Scene(vBox));
    }
}
