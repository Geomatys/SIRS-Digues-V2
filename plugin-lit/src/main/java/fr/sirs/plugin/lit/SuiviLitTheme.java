package fr.sirs.plugin.lit;

import fr.sirs.core.model.Element;
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

    SuiviLitPane pane;
    
    public SuiviLitTheme() {
        super("Suivi des lits", "Suivi des lits", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        if(pane==null) pane = new SuiviLitPane();
        return pane;
    }

    public void display(final Element element){
        if(pane==null) pane = new SuiviLitPane();
        pane.displayElement(element);
    }
}