package fr.sirs.plugin.lit;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class SuiviLitTheme extends AbstractPluginsButtonTheme {
    
    public SuiviLitTheme() {
        super("Suivi des lits", "Suivi des lits", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}