package fr.sirs.plugins;

import fr.sirs.theme.Theme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DocumentsTheme extends Theme {
    public DocumentsTheme() {
        super("Suivi des documents", Type.PLUGINS);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}
