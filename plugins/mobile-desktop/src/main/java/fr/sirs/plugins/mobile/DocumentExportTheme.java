package fr.sirs.plugins.mobile;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import java.awt.Color;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DocumentExportTheme extends AbstractPluginsButtonTheme {

    private static final Image ICON = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_SEND_ALIAS, 100, Color.BLACK),null);

    public DocumentExportTheme() {
        super("Export de documents", "Interface permettant de transférer des documents depuis l'application de bureau vers l'application mobile.", ICON);
    }

    @Override
    public Parent createPane() {
        return new DocumentExportPane();
    }
}