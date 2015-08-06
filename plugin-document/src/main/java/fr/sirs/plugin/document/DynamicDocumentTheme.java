package fr.sirs.plugin.document;

import fr.sirs.plugin.document.ui.DynamicDocumentsPane;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Gestion de la création de documents dynamiques.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class DynamicDocumentTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/gen_etats.png"));

    private final FileTreeItem root;
    
    public DynamicDocumentTheme(final FileTreeItem root) {
        super("Documents dynamiques", "Documents dynamiques", BUTTON_IMAGE);
        this.root = root;
    }

    @Override
    public Parent createPane() {
        return new DynamicDocumentsPane(root);
    }
    
    @Override
    public ChangeListener<Boolean> getSelectedPropertyListener() {
        return new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue && root.getValue() != null) {
                    PropertiesFileUtilities.updateFileSystem(root.getValue());
                    root.update(root.rootShowHiddenFile);
                }
            }
        };
    }
}
