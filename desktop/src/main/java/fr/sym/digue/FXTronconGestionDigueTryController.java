

package fr.sym.digue;

import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconGestionDigueTryController {
    
    public Parent root;
    private TronconDigue troncon;
    
    @FXML
    TextField section_name;
    
    @FXML
    TextArea commentaireTronconTextField;
    
    @FXML
    ToggleButton editionButton;
    
    @FXML
    public void enableFields(ActionEvent event){
        if (this.editionButton.isSelected()) {
            this.section_name.setEditable(false);
            this.commentaireTronconTextField.setEditable(false);
        } else {
            this.section_name.setEditable(true);
            this.commentaireTronconTextField.setEditable(true);
        }
    }
    
    public void init(TronconDigue troncon){
        this.troncon = troncon;
        
        this.section_name.setEditable(true);
        this.section_name.textProperty().bindBidirectional(this.troncon.libelle);
        
        this.commentaireTronconTextField.setEditable(true);
        this.commentaireTronconTextField.setWrapText(true);
        this.commentaireTronconTextField.textProperty().bindBidirectional(this.troncon.commentaire);
    }
    
    public static FXTronconGestionDigueTryController create(TronconDigue troncon) {
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource("/fr/sym/digue/tronconGestionDigueTryDisplay.fxml"));
        final Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        final FXTronconGestionDigueTryController controller = loader.getController();
        controller.root = root;
        controller.init(troncon);
        return controller;
    }
    
}
