package fr.sirs.plugin.aot.cot;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Bouton de consultation des documents associés aux AOT / COT.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class AssociatedDocumentsAotCotTheme extends AbstractPluginsButtonTheme {
    public AssociatedDocumentsAotCotTheme() {
        super("Documents associés", "Documents associés", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}