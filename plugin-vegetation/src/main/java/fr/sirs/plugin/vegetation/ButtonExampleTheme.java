package fr.sirs.plugin.vegetation;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ButtonExampleTheme extends AbstractPluginsButtonTheme {
    public ButtonExampleTheme() {
        super("Bouton exemple", "Bouton exemple", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}