
package fr.sirs.plugin.vegetation.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PositionableVegetation;
import fr.sirs.theme.ui.FXPositionablePane;
import fr.sirs.theme.ui.FXPositionableVegetationPane;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableForm extends BorderPane {

    @FXML private Button uiGoto;
    @FXML private Button uiDelete;
    @FXML private Button uiSave;

    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
    private Node editor = null;

    public FXPositionableForm() {
        SIRS.loadFXML(this, Positionable.class);

        positionableProperty.addListener(this::changed);
        uiGoto.disableProperty().bind(positionableProperty.isNull());
        uiDelete.disableProperty().bind(positionableProperty.isNull());
        uiSave.disableProperty().bind(positionableProperty.isNull());
    }

    @FXML
    void delete(ActionEvent event) {
        final Positionable pos = positionableProperty.get();
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la suppression de "+ new SirsStringConverter().toString(pos),
                ButtonType.YES, ButtonType.NO);
        alert.initOwner(this.getScene().getWindow());
        alert.initModality(Modality.WINDOW_MODAL);
        final ButtonType res = alert.showAndWait().get();
        if (res == ButtonType.YES) {
            final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(pos.getClass());
            repo.remove(pos);
            positionableProperty().set(null);
        }
    }

    @FXML
    void save(ActionEvent event) {
        final Positionable pos = positionableProperty.get();
        final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(pos.getClass());
        repo.update(pos);
        positionableProperty.set(null);
    }

    @FXML
    void gotoForm(ActionEvent event) {
        final Positionable pos = positionableProperty.get();
        if(pos!=null){
            Injector.getSession().showEditionTab(pos);
        }
    }

    public ObjectProperty<Positionable> positionableProperty(){
        return positionableProperty;
    }

    public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue){
        if(newValue instanceof PositionableVegetation){
            editor = new FXPositionableVegetationPane();
            ((FXPositionableVegetationPane)editor).setPositionable(newValue);
            ((FXPositionableVegetationPane)editor).disableFieldsProperty().set(false);
        }else if(newValue instanceof Positionable){
            editor = new FXPositionablePane();
            ((FXPositionablePane)editor).setPositionable(newValue);
            ((FXPositionablePane)editor).disableFieldsProperty().set(false);
        }

        setCenter(editor);
    }
    
}
