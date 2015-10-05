package fr.sirs.plugin.carto;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class GestionServerTheme extends AbstractPluginsButtonTheme {
    public GestionServerTheme() {
        super("Gestion des serveurs de carte", "Gestion des serveurs de carte", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}