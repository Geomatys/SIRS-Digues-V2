

package fr.sym.digue;

import fr.sym.Symadrem;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXDamController {
    
    public Parent root;
    
    public void init(){
        
    }
    
    public static FXDamController create() {
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource("/fr/sym/digue/diguesGestion.fxml"));
        final Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        final FXDamController controller = loader.getController();
        controller.root = root;
        controller.init();
        return controller;
    }
    
}
