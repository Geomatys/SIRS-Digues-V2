package fr.sirs.plugins;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les fonctionnalités de génération d'états.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class StatesGeneratorTheme extends AbstractPluginsButtonTheme {
    public StatesGeneratorTheme() {
        super("Générateur d'états", "Générateur d'états", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}
