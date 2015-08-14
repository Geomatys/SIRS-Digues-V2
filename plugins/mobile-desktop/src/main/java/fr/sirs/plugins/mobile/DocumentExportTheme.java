package fr.sirs.plugins.mobile;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DocumentExportTheme extends AbstractPluginsButtonTheme {

    private static final Image ICON = new Image(DocumentExportTheme.class.getResourceAsStream("documentExport.png"));

    public DocumentExportTheme() {
        super("Export de documents", "Interface permettant de transférer des documents depuis l'application de bureau vers l'application mobile.", ICON);
    }

    @Override
    public Parent createPane() {
        return new DocumentExportPane();
    }
}