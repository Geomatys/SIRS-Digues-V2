package fr.sirs.plugin.reglementaire;

import fr.sirs.plugin.reglementaire.ui.TemplatesTable;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les fonctionnalités de génération d'états.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class TemplatesTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            TemplatesTheme.class.getResourceAsStream("images/template.png"));

    public TemplatesTheme() {
        super("Documents de mise en forme", "Documents de mise en forme", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        final BorderPane pane = new BorderPane(new TemplatesTable());
        return pane;
    }
    
}
