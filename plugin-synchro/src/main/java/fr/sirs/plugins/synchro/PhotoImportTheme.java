package fr.sirs.plugins.synchro;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoImportTheme extends AbstractPluginsButtonTheme {

    private static final Image ICON = new Image(PhotoImportPane.class.getResourceAsStream("photoImport.png"));

    public PhotoImportTheme() {
        super("Importer les photos", "Interface permettant de récupérer les photos prises depuis l'appareil mobile pour transfert sur le disque.", ICON);
    }

    @Override
    public Parent createPane() {
        return new PhotoImportPane();
    }

}
