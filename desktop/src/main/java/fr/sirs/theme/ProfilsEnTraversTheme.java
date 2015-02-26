

package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.theme.ui.PojoTable;
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
        
        final Session session = Injector.getSession();
        final PojoTable tableProfilsTravers = new PojoTable(session.getProfilTraversRepository(), "Liste des profils en travers");
        pane.setCenter(tableProfilsTravers);
        return pane;
    }    
}
