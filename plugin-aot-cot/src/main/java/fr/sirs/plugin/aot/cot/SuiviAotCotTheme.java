package fr.sirs.plugin.aot.cot;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Bouton de suivi d'AOT / COT.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class SuiviAotCotTheme extends AbstractPluginsButtonTheme {
    public SuiviAotCotTheme() {
        super("Suivi AOT/COT", "Suivi AOT/COT", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}