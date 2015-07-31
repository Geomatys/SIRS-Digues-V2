package fr.sirs.plugins.mobile;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import java.awt.Color;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoImportTheme extends AbstractPluginsButtonTheme {

    private static final Image ICON = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_SEND_ALIAS, 100, Color.BLACK),null);

    public PhotoImportTheme() {
        super("Importer les photos", "Interface permettant de récupérer les photos prises depuis l'appareil mobile pour transfert sur le disque.", ICON);
    }

    @Override
    public Parent createPane() {

        return null;
    }

}
