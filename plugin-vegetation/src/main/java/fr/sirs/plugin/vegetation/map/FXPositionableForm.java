
package fr.sirs.plugin.vegetation.map;

import fr.sirs.Injector;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PositionableVegetation;
import fr.sirs.theme.ui.FXPositionablePane;
import fr.sirs.theme.ui.FXPositionableVegetationPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableForm extends BorderPane {

    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
    private Node editor = null;

    public FXPositionableForm() {

        final BorderPane top = new BorderPane();
        final Button gotoForm = new Button("Fiche complète");
        gotoForm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Positionable pos = positionableProperty.get();
                if(pos!=null){
                    Injector.getSession().showEditionTab(pos);
                }
            }
        });
        final Button delete = new Button("Supprimer");
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
            }
        });

        top.setLeft(gotoForm);
        top.setRight(delete);
        setTop(top);

        positionableProperty.addListener(this::changed);
        gotoForm.disableProperty().bind(positionableProperty.isNull());
        delete.disableProperty().bind(positionableProperty.isNull());

    }

    public ObjectProperty<Positionable> positionableProperty(){
        return positionableProperty;
    }

    public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue){
        if(newValue instanceof PositionableVegetation){
            editor = new FXPositionableVegetationPane();
            ((FXPositionableVegetationPane)editor).setPositionable(newValue);
        }else if(newValue instanceof Positionable){
            editor = new FXPositionablePane();
            ((FXPositionablePane)editor).setPositionable(newValue);
        }
        setCenter(editor);
    }
    

}
