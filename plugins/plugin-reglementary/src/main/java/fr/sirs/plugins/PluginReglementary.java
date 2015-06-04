package fr.sirs.plugins;

import fr.sirs.Plugin;
import fr.sirs.theme.Theme;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Plugin correspondant au module réglementaire, permettant de gérer des documents de suivis.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class PluginReglementary extends Plugin {
    private static final String NAME = "plugin-reglementary";

    public PluginReglementary() {
        name = NAME;
        loadingMessage.set("Chargement du module réglementaire");
        themes.add(new Theme("Module réglementaire", Theme.Type.PLUGINS) {
            
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
        getConfiguration();
    }
}
