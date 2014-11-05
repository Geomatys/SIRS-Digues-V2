

package fr.sirs.theme;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ProfilsEnTraversTheme extends Theme {

        
    public ProfilsEnTraversTheme() {
        super("Profils en travers", Type.OTHER);
    }

    @Override
    public Parent createPane() {
        final BorderPane pane = new BorderPane();
        pane.setCenter(new Label("Modèle Profils en travers non terminé"));
        return pane;
    }
    
}
