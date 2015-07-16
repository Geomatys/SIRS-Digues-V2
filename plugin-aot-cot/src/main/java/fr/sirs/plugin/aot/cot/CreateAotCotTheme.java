package fr.sirs.plugin.aot.cot;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Bouton de création d'AOT / COT.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class CreateAotCotTheme extends AbstractPluginsButtonTheme {
    public CreateAotCotTheme() {
        super("Création AOT/COT", "Création AOT/COT", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}