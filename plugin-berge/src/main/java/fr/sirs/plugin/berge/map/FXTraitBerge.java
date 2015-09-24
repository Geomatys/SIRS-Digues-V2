
package fr.sirs.plugin.berge.map;

import fr.sirs.SIRS;
import fr.sirs.core.model.Berge;
import fr.sirs.core.model.TraitBerge;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTraitBerge extends GridPane{

    private static final String MESSAGE_BERGE = "Séléctionner une berge sur la carte.";
    private static final String MESSAGE_TRAIT = "Séléctionner un trait de berge sur la carte ou cliquer sur nouveau.";

    @FXML private Label uiLblBerge;
    @FXML private Label uiLblTrait;
    @FXML private DatePicker uiDateDebut;
    @FXML private Label uiLblGeom;
    @FXML private DatePicker uiDateFin;
    @FXML private Button uiBtnDelete;
    @FXML private Button uiBtnSave;
    @FXML private Button uiBtnNew;

    private final ObjectProperty<Berge> bergeProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<TraitBerge> traitProperty = new SimpleObjectProperty<>();

    public FXTraitBerge(){
        SIRS.loadFXML(this);

        uiLblBerge.setText(MESSAGE_BERGE);
        bergeProperty.addListener(new ChangeListener<Berge>() {
            @Override
            public void changed(ObservableValue<? extends Berge> observable, Berge oldValue, Berge newValue) {
                if(newValue!=null){
                    uiLblBerge.setText(new SirsStringConverter().toString(newValue));
                }else{
                    uiLblBerge.setText(MESSAGE_BERGE);
                }
            }
        });

        uiLblTrait.setText("");
        traitProperty.addListener(new ChangeListener<TraitBerge>() {
            @Override
            public void changed(ObservableValue<? extends TraitBerge> observable, TraitBerge oldValue, TraitBerge newValue) {
                if(newValue!=null){
                    uiLblTrait.setText(new SirsStringConverter().toString(newValue));
                }else{
                    uiLblTrait.setText(MESSAGE_TRAIT);
                }
            }
        });

        uiBtnNew.disableProperty().bind(bergeProperty.isNull().or(traitProperty.isNotNull()));


        uiDateDebut.disableProperty().bind(traitProperty.isNull());
        uiDateFin.disableProperty().bind(traitProperty.isNull());
        uiBtnSave.disableProperty().bind(traitProperty.isNull());
        uiBtnDelete.disableProperty().bind(traitProperty.isNull());

    }

    public ObjectProperty<Berge> bergeProperty(){
        return bergeProperty;
    }

    public ObjectProperty<TraitBerge> traitProperty(){
        return traitProperty;
    }

    @FXML
    void delete(ActionEvent event) {

    }

    @FXML
    void save(ActionEvent event) {

    }

}
