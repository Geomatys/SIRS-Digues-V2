

package fr.sym.theme;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class EvenementsHydrauliquesTheme extends Theme {

        
    public EvenementsHydrauliquesTheme() {
        super("Evènements hydrauliques", Type.OTHER);
    }

    @Override
    public Parent createPane() {
        final BorderPane pane = new BorderPane();
        pane.setCenter(new Label("TODO"));
        return pane;
    }
    
}
