package fr.sirs.plugins.mobile;

import fr.sirs.Plugin;
import fr.sirs.core.model.sql.SQLHelper;
import java.awt.Color;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class MobilePlugin extends Plugin {
    private static final String NAME = "mobile-desktop";
    private static final String TITLE = "Application mobile";

    private static final Image PLUGIN_ICON = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_MOBILE, 100, Color.BLACK),null);
    public MobilePlugin() {
        name = NAME;
        loadingMessage.set("Chargement du module pour la synchronisation bureau/mobile");
        themes.add(new DocumentExportTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
    }

    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    @Override
    public Image getImage() {
        return PLUGIN_ICON;
    }

    @Override
    public SQLHelper getSQLHelper() {
        return null;
    }
}
