package fr.sirs.plugin.dependance;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les dépendances.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DependancesTheme extends AbstractPluginsButtonTheme {
    public DependancesTheme() {
        super("Gestion des dépendances", "Gestion des dépendances", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}