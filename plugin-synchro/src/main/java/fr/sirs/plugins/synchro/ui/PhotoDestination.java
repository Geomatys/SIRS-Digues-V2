package fr.sirs.plugins.synchro.ui;

import fr.sirs.SIRS;
import fr.sirs.util.property.DocumentRoots;
import fr.sirs.util.property.SirsPreferences;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoDestination extends StackPane {
    @FXML
    private Label uiRootLabel;

    @FXML
    private Hyperlink uiChooseSubDir;

    @FXML
    private Label uiSubDirLabel;

    @FXML
    private ProgressIndicator uiDestSpaceProgress;

    /**
     * Destination root path, as it should be defined in {@link SirsPreferences.PROPERTIES#DOCUMENT_ROOT}.
     */
    private final SimpleObjectProperty<Path> rootDirProperty = new SimpleObjectProperty<>();

    /**
     * A sub-directory of {@link #rootDirProperty} to put imported photos into.
     */
    private final SimpleObjectProperty<Path> subDirProperty = new SimpleObjectProperty<>();

    private final ObjectBinding<Path> destination;

    public PhotoDestination() {
        SIRS.loadFXML(this);

        final BooleanBinding noRootConfigured = rootDirProperty.isNull();
        uiChooseSubDir.disableProperty().bind(noRootConfigured);
        uiSubDirLabel.disableProperty().bind(noRootConfigured);

        rootDirProperty.addListener(this::destinationChanged);
        subDirProperty.addListener(this::destinationChanged);

        rootDirProperty.set(DocumentRoots.getPhotoRoot(null, false).orElse(null));

        destination = Bindings.createObjectBinding(() -> {
            final Path root = rootDirProperty.get();
            if (root == null)
                return null;
            final Path subDir = subDirProperty.get();
            if (subDir == null) {
                return root;
            }

            return root.resolve(subDir);

        }, rootDirProperty, subDirProperty);
    }

    public ObjectBinding<Path> getDestination() {
        return destination;
    }

    @FXML
    void chooseSubDirectory(ActionEvent event) {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Répertoire destination");
        final Path root = rootDirProperty.get();
        chooser.setInitialDirectory(root.toFile());
        File chosen = chooser.showDialog(getScene().getWindow());
        if (chosen != null) {
            subDirProperty.set(root.relativize(chosen.toPath()));
        }
    }

    @FXML
    void configureRoot(ActionEvent event) {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir un répertoire racine : ");
        if (rootDirProperty.get() != null) {
            chooser.setInitialDirectory(rootDirProperty.get().toFile());
        }
        File chosen = chooser.showDialog(getScene().getWindow());
        if (chosen != null) {
            rootDirProperty.set(chosen.toPath().toAbsolutePath());
            DocumentRoots.setDefaultPhotoRoot(rootDirProperty.get());
        }
    }

    /**
     * Compute back destination usable space each time root or subdirectory change.
     * We do it for both elements, in case sub-directory is not on the same filestore.
     * Ex : root is /media, and sub-directory is myUsbKey/photos
     * @param obs
     * @param oldValue
     * @param newValue
     */
    private void destinationChanged(final ObservableValue<? extends Path> obs, final Path oldValue, final Path newValue) {
        if (rootDirProperty.get() == null) {
            uiRootLabel.setText("N/A");
            uiSubDirLabel.setText("N/A");
            uiDestSpaceProgress.setProgress(0);
        } else {
            uiRootLabel.setText(rootDirProperty.get().toString());
            final Path subDir = subDirProperty.get();
            final Path absolutePath;
            if (subDir == null || subDir.toString().isEmpty()) {
                uiSubDirLabel.setText("N/A");
                absolutePath = rootDirProperty.get();
            } else {
                uiSubDirLabel.setText(subDir.toString());
                absolutePath = rootDirProperty.get().resolve(subDir);
            }

            try {
                final FileStore fileStore = newValue.getFileSystem().provider().getFileStore(absolutePath);

                final long usableSpace = fileStore.getUsableSpace();
                final long totalSpace = fileStore.getTotalSpace();
                // HACK : Never set to 1 to avoid message print.
                uiDestSpaceProgress.setProgress(totalSpace <= 0 || usableSpace <= 0? 0.99999 : 1 - ((double)usableSpace / totalSpace));
            } catch (IOException e) {
                GeotkFX.newExceptionDialog("L'analyse du dossier destination a échoué. Veuillez choisir un autre dossier destination.", e);
            }
        }
    }
}
