package fr.sirs.plugin.document;

import fr.sirs.Plugin;
import fr.sirs.core.model.sql.DocumentSqlHelper;
import fr.sirs.core.model.sql.SQLHelper;
import javafx.scene.image.Image;

/**
 * Minimal example of a plugin.
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
    public SQLHelper getSQLHelper() {
        return DocumentSqlHelper.getInstance();
    }
}
