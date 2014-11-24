

package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.Repository;
import fr.sirs.core.model.Element;
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
        final PojoTable tableProfilsTravers = new ProfilTraversTable(session.getProfilTraversRepository());
        pane.setCenter(tableProfilsTravers);
        return pane;
    }

    private static class ProfilTraversTable extends PojoTable {

        public ProfilTraversTable(Repository repo) {
            super(repo, "Liste des profils en travers");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            
        }
        
        @Override
        protected void createPojo() {
        }
    }
    
}
