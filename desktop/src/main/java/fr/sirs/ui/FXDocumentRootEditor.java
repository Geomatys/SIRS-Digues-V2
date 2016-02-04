package fr.sirs.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.AvecPhotos;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.SIRSFileReference;
import static fr.sirs.util.FXAuthenticationWalletEditor.LIST_CLASSES;
import fr.sirs.util.SaveableConfiguration;
import fr.sirs.util.property.DocumentRoots;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.geotoolkit.gui.javafx.util.FXDirectoryTextField;

/**
 * An editor to set document root paths to use in the application.
 *
 * Note : Due to a bug with {@link ListView} component, we've "simulated" a list
 * by stacking nodes in a VBox. The problem with javafx list views is that
 * as cell is re-used, it's impossible to attach an editor to modify an attribute
 * of the cell item.
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXDocumentRootEditor extends BorderPane implements SaveableConfiguration {

    @FXML
    private FXDirectoryTextField uiDefaultRoot;

    @FXML
    private VBox uiRootList;

    private final ObservableList<RootInfo> items;

    public FXDocumentRootEditor() {
        SIRS.loadFXML(this);
        items = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(
                new PhotoRootInfo("Photographies", null),
                new DocumentRootInfo("Documents associés aux profils en long", ProfilLong.class),
                new DocumentRootInfo("Documents associés aux levés de profil en travers", LeveProfilTravers.class),
                new DocumentRootInfo("Documents associés aux articles de journaux", ArticleJournal.class),
                new DocumentRootInfo("Documents à grande échelle", DocumentGrandeEchelle.class),
                new DocumentRootInfo("Documents associés aux marchés", Marche.class),
                new DocumentRootInfo("Documents associés aux rapports d'étude", RapportEtude.class)
        ));

        for (int i = 0 ; i < items.size() ; i++) {
            RootInfo info = items.get(i);
            info.getStyleClass().add(LIST_CLASSES[i%LIST_CLASSES.length]);
        }
        uiRootList.getChildren().addAll(items);

        Path root = DocumentRoots.getRoot(null).orElse(null);
        uiDefaultRoot.setText(root == null? null : root.toString());
    }

    @Override
    public String getTitle() {
        return "Répertoires racines";
    }

    @Override
    public void save() throws BackingStoreException {
        final String defaultRootStr = uiDefaultRoot.getText();
        if (defaultRootStr == null || defaultRootStr.isEmpty()) {
            DocumentRoots.setDefaultRoot(null);
        } else {
            DocumentRoots.setDefaultRoot(Paths.get(defaultRootStr));
        }

        final int total = items.size();
        for (int i = 0; i < total; i++) {
            items.get(i).save();
        }

        DocumentRoots.flush();
    }

    /**
     * Base class for root folder information
     */
    private static abstract class RootInfo extends VBox {
        protected final String title;
        protected final StringProperty rootPath;
        
        public RootInfo(final String title) {
            this.title = title;

            final FXDirectoryTextField pathField = new FXDirectoryTextField();
            rootPath = pathField.textProperty();
            
            setFillWidth(true);
            setSpacing(5);
            getChildren().addAll(new Label(title), pathField);
        }

        protected abstract void save();
    }

    /**
     * Information about a root folder which should contain image files bound to
     * a {@link AbstractPhoto}.
     */
    private static class PhotoRootInfo extends RootInfo {
        private final Class<? extends AvecPhotos> associatedType;

        public PhotoRootInfo(String title, Class<? extends AvecPhotos> associatedType) {
            super(title);
            this.associatedType = associatedType;
            final Path tmpPath = DocumentRoots.getPhotoRoot(this.associatedType, true).orElse(null);
            rootPath.set(tmpPath == null? null : tmpPath.toString());
        }

        @Override
        protected void save() {
            final String rootStr = rootPath.get();
            DocumentRoots.setPhotoRoot(rootStr == null || rootStr.isEmpty()? null : Paths.get(rootStr), associatedType);
        }
    }

    /**
     * Information about a root folder which can contain files refered by a {@link SIRSFileReference}.
     */
    private static class DocumentRootInfo extends RootInfo {

        private final Class<? extends SIRSFileReference> refType;

        public DocumentRootInfo(String title, Class<? extends SIRSFileReference> refType) {
            super(title);
            this.refType = refType;
            final Path tmpPath = DocumentRoots.getRoot(this.refType, true).orElse(null);
            rootPath.set(tmpPath == null? null : tmpPath.toString());
        }

        @Override
        protected void save() {
            final String rootStr = rootPath.get();
            DocumentRoots.setDocumentRoot(rootStr == null || rootStr.isEmpty()? null : Paths.get(rootPath.get()), refType);
        }
    }
}
