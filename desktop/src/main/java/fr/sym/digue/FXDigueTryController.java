

package fr.sym.digue;

import fr.sym.Symadrem;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXDigueTryController {
    
    public Parent root;
    
    public void init(){
        
    }
    
    public static FXDigueTryController create() {
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource("/fr/sym/digue/digueTryDisplay.fxml"));
        final Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        final FXDigueTryController controller = loader.getController();
        controller.root = root;
        controller.init();
        return controller;
    }
    
}
