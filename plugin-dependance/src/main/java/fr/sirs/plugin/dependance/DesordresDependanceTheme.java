package fr.sirs.plugin.dependance;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les désordres pour les dépendances.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DesordresDependanceTheme extends AbstractPluginsButtonTheme {
    public DesordresDependanceTheme() {
        super("Désordres", "Désordres", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}