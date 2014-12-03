package fr.sirs.plugins;

import fr.sirs.Plugin;
import fr.sirs.theme.Theme;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 */
public class PluginExample extends Plugin {

    public PluginExample() {
        loadingMessage.set("I'm an information text. I'm displayed when the plugin is loaded at application initialisation.");
        themes.add(new Theme("PluginExample theme", Theme.Type.OTHER) {
            
            @Override
            public Parent createPane() {
                final BorderPane uiCenter = new BorderPane();
                final TabPane tabPane = new TabPane();

                final Tab tabIntervenant = new Tab("first tab");
                final Tab tabOrganisme = new Tab("second tab");

                tabPane.getTabs().add(tabIntervenant);
                tabPane.getTabs().add(tabOrganisme);

                uiCenter.setCenter(tabPane);
                return tabPane;
            }
        });
    }

    @Override
    public void load() throws Exception {
        getDescription();
    }
    
    
}
