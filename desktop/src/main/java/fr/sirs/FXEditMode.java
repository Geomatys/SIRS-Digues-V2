
package fr.sirs;

import java.io.IOException;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXEditMode extends VBox {
    
    @FXML private ToggleButton uiEdit;
    @FXML private Button uiSave;
    @FXML private ToggleButton uiConsult;

    private Runnable saveAction;
    
    public FXEditMode() {
        final Class cdtClass = getClass();
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
        getStylesheets().add("/fr/sirs/theme.css");
                
        final BooleanBinding editBind = uiEdit.selectedProperty().not();
        uiSave.disableProperty().bind(editBind);
        
        final ToggleGroup group = new ToggleGroup();
        uiConsult.setToggleGroup(group);
        uiEdit.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null) group.selectToggle(uiConsult);
        });
    }
     
    public void setAllowedRoles(Role... allowed){
        final Session session = Injector.getBean(Session.class);
        boolean editionGranted = false;
        for(final Role role : allowed){
            if(session.getRole()==role) {
                editionGranted=true;
            }
        }
        uiEdit.setDisable(!editionGranted);
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
