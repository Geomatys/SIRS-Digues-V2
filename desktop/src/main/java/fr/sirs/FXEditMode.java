
package fr.sirs;

import static fr.sirs.SIRS.COLOR_INVALID_ICON;
import static fr.sirs.SIRS.CSS_PATH;
import static fr.sirs.SIRS.ICON_CHECK_CIRCLE;
import static fr.sirs.SIRS.ICON_EXCLAMATION_CIRCLE;
import fr.sirs.core.model.Element;
import java.io.IOException;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXEditMode extends VBox {

    private static final String VALID_TEXT = "Validé";
    private static final String INVALID_TEXT = "Invalidé";

    @FXML protected HBox uiValidationBox;
    @FXML protected ImageView uiImageValid;
    @FXML protected Label uiLabelValid;
    @FXML protected ToggleButton uiEdit;
    @FXML protected Button uiSave;
    @FXML protected ToggleButton uiConsult;

    private final Session session = Injector.getBean(Session.class);
    private final StringProperty authorIDProperty;
    private final BooleanProperty validProperty;

    private Runnable saveAction;

    public FXEditMode() {
        final Class cdtClass = FXEditMode.class;
        final String fxmlpath = "/"+cdtClass.getName().replace('.', '/')+".fxml";
        final FXMLLoader loader = new FXMLLoader(cdtClass.getResource(fxmlpath));
        loader.setController(this);
        loader.setRoot(this);
        //in special environement like osgi or other, we must use the proper class loaders
        //not necessarly the one who loaded the FXMLLoader class
        loader.setClassLoader(cdtClass.getClassLoader());
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        getStylesheets().add(CSS_PATH);

        uiEdit.disableProperty().bind(session.nonGeometryEditionProperty().not());
        final BooleanBinding editBind = uiEdit.selectedProperty().not();
        uiSave.disableProperty().bind(editBind);

        final ToggleGroup group = new ToggleGroup();
        uiConsult.setToggleGroup(group);
        uiEdit.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            // We check that user has enough rights
            if (!session.nonGeometryEditionProperty().get())
                group.selectToggle(uiConsult);
            else if (newValue==null)
                group.selectToggle(oldValue);
            });

        authorIDProperty = new SimpleStringProperty();
        validProperty = new SimpleBooleanProperty();
        validProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                resetValidUIs(newValue);
            });
        validProperty.set(true);
    }

    private void resetValidUIs(final boolean valid){
        if(valid){
            uiImageValid.setImage(ICON_CHECK_CIRCLE);
            uiLabelValid.setText(VALID_TEXT);
            uiLabelValid.setTextFill(Color.WHITE);
        }
        else {
            uiImageValid.setImage(ICON_EXCLAMATION_CIRCLE);
            uiLabelValid.setText(INVALID_TEXT);
            uiLabelValid.setTextFill(Color.valueOf(COLOR_INVALID_ICON));
        }
    }

    public StringProperty authorIDProperty(){return authorIDProperty;}
    public BooleanProperty validProperty(){return validProperty;}

    private void requireEdition(){
        if(!uiEdit.isDisabled()){
            uiEdit.setSelected(true);
        }
    }

    public void requireEditionForElement(final Element element){
        ArgumentChecks.ensureNonNull("element", element);
        if(element.getDesignation()==null || "".equals(element.getDesignation())){
            requireEdition();
        }
    }

    public void setSaveAction(Runnable saveAction) {
        this.saveAction = saveAction;
    }

    public BooleanProperty editionState(){
        return uiEdit.selectedProperty();
    }

    @FXML
    public void save(ActionEvent event) {
        if(saveAction!=null) saveAction.run();
    }
}
