package fr.sirs.plugin.document;

import fr.sirs.plugin.document.ui.DocumentsPane;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DocumentManagementTheme extends AbstractPluginsButtonTheme {
    
    private static final Image BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/gestion_documents.png"));
    private final FileTreeItem root;
    private final DynamicDocumentTheme dynDcTheme;
    
    public DocumentManagementTheme(final FileTreeItem root, final DynamicDocumentTheme dynDcTheme) {
        super("Gestion des documents", "Gestion des documents", BUTTON_IMAGE);
        this.root = root;
        this.dynDcTheme = dynDcTheme;
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane(new DocumentsPane(root, dynDcTheme));

        return borderPane;
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