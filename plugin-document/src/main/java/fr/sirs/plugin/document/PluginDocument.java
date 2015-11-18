package fr.sirs.plugin.document;

import fr.sirs.Plugin;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javafx.scene.image.Image;

/**
 * Document d'ouvrage. Permet de générer des rapports concernant les ouvrages
 * présents sur les digues
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginDocument extends Plugin {
    private static final String NAME = "plugin-document";
    private static final String TITLE = "Module dossier d'ouvrages";

    public PluginDocument() {
        name = NAME;
        final FileTreeItem root = new FileTreeItem(false);
        loadingMessage.set("module dossier d'ouvrages");
        final DynamicDocumentTheme dynDcTheme = new DynamicDocumentTheme(root);
        themes.add(new DocumentManagementTheme(root, dynDcTheme));
        themes.add(dynDcTheme);

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
        // TODO: choisir une image pour ce plugin
        return null;
    }

    @Override
    public Optional<Image> getModelImage() throws IOException {
        final Image image;

        try (final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("fr/sirs/documentModel.png")) {
            image = new Image(in);
        }
        return Optional.of(image);
    }
}
