
package fr.sirs.plugin.lit;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author guilhem
 */
public class StructureDescriptionTheme extends AbstractPluginsButtonTheme {
    
    public StructureDescriptionTheme() {
        super("Description du lit", "Descriptions du lit", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}