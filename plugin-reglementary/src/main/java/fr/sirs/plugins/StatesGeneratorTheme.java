package fr.sirs.plugins;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les fonctionnalités de génération d'états.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class StatesGeneratorTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            StatesGeneratorTheme.class.getResourceAsStream("images/gen_etats.png"));

    public StatesGeneratorTheme() {
        super("Générateur d'états", "Générateur d'états", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}
