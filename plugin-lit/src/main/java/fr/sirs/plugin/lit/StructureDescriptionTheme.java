
package fr.sirs.plugin.lit;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author guilhem
 */
public class StructureDescriptionTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            StructureDescriptionTheme.class.getResourceAsStream("images/lit-description.png"));
    
    public StructureDescriptionTheme() {
        super("Description du lit", "Descriptions du lit", BUTTON_IMAGE);
        getSubThemes().add(new OuvragesLitTheme());
        getSubThemes().add(new IleBancTheme());
        getSubThemes().add(new ReseauxVoirieTheme());
        getSubThemes().add(new ReseauxOuvrageTheme());
        getSubThemes().add(new DesordresTheme());
        getSubThemes().add(new PrestationsTheme());
        getSubThemes().add(new DocumentTheme());
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}