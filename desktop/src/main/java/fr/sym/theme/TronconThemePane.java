
package fr.sym.theme;

import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.TronconDigue;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel
 */
public class TronconThemePane extends BorderPane {

    @FXML
    private BorderPane uiCenter;

    @FXML
    private ChoiceBox<Object> uiTronconChoice;
    
    public TronconThemePane() {
        Symadrem.loadJRXML(this);
    }
    
    /**
     * Called by FXMLLoader after creating controller.
     */
    public void initialize(){
        
    }
    
}
