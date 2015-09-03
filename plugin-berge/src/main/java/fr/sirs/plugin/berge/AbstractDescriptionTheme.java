
package fr.sirs.plugin.berge;

import fr.sirs.plugin.berge.ui.AbstractDescriptionPane;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractDescriptionTheme extends AbstractPluginsButtonTheme {
    
    public AbstractDescriptionTheme(String name, String description) {
        super(name, description, null);
    }
    
    @Override
    public Parent createPane() {
        final BorderPane borderPane = new AbstractDescriptionPane();
        return borderPane;
    }
}
