package fr.sirs.plugins;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les fonctionnalités de suivi de documents.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class DocumentsTheme extends AbstractPluginsButtonTheme {
    public DocumentsTheme() {
        super("Suivi des documents", Type.PLUGINS, "Suivi des documents", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}
