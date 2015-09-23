package fr.sirs.plugin.berge;

import fr.sirs.plugin.berge.ui.SuiviBergePane;
import fr.sirs.theme.Theme;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les fonctionnalités de suivi de berges.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class SuiviBergeTheme extends AbstractPluginsButtonTheme {
    public SuiviBergeTheme() {
        super("Suivi des berges", "Suivi des berges", new Image("fr/sirs/plugin/berge/berge-suivi.png"));
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new SuiviBergePane();
        return borderPane;
    }

    
}