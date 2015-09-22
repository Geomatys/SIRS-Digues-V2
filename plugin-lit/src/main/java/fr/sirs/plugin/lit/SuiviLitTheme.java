package fr.sirs.plugin.lit;

import fr.sirs.plugin.lit.ui.SuiviLitPane;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class SuiviLitTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            SuiviLitTheme.class.getResourceAsStream("images/lit-suivi.png"));
    
    public SuiviLitTheme() {
        super("Suivi des lits", "Suivi des lits", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        return new SuiviLitPane();
    }
}