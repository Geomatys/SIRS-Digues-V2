

package fr.sym.digue;

import fr.sym.*;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TreeView;

public class FXDigueController {

    public Parent root;
     
    @FXML
    private TreeView uiTree;

    private void init(){
        
    }
    
    @FXML
    void openSearchPopup(ActionEvent event) {
        System.out.println("TODO");
    }
    
    
    public static FXDigueController create() {
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource("/fr/sym/digue/diguesBase.fxml"));
        final Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        final FXDigueController controller = loader.getController();
        controller.root = root;
        controller.init();
        return controller;
    }
    
}
