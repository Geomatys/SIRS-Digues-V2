package fr.sirs.plugin.document;

import fr.sirs.plugin.document.ui.DocumentsPane;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DocumentManagementTheme extends AbstractPluginsButtonTheme {
    
    private static final Image BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/gen_etats.png"));
    private final FileTreeItem root;
    
    public DocumentManagementTheme(final FileTreeItem root) {
        super("Gestion des documents", "Gestion des documents", BUTTON_IMAGE);
        this.root = root;
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane(new DocumentsPane(root));

        return borderPane;
    }
}