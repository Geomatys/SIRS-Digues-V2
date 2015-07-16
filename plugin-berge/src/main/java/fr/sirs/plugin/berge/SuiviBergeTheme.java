package fr.sirs.plugin.berge;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les fonctionnalités de suivi de berges.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class SuiviBergeTheme extends AbstractPluginsButtonTheme {
    public SuiviBergeTheme() {
        super("Suivi des berges", "Suivi des berges", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}