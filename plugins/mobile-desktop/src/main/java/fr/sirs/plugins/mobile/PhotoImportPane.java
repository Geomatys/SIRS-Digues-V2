package fr.sirs.plugins.mobile;

import fr.sirs.SIRS;
import fr.sirs.util.CopyTask;
import fr.sirs.util.property.SirsPreferences;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoImportPane extends StackPane {

    @FXML
    private Label uiSourceName;

    @FXML
    private Label uiSourceType;

    @FXML
    private Label uiSourceUsableSpace;

    @FXML
    private ProgressIndicator uiSoureSpaceProgress;

    @FXML
    private Label uiRootLabel;

    @FXML
    private Hyperlink uiChooseSubDir;

    @FXML
    private Label uiSubDirLabel;

    @FXML
    private ProgressIndicator uiDestSpaceProgress;

    @FXML
    private ProgressBar uiImportProgress;

    @FXML
    private Button uiImportBtn;

    @FXML
    private Label uiCopyMessage;
    private final Tooltip copyMessageTooltip = new Tooltip();

    /**
     * Source directory in which we'll find photos to transfer.
     */
    private final SimpleObjectProperty<Path> sourceDirProperty = new SimpleObjectProperty<>();

    /**
     * Destination root path, as it should be defined in {@link SirsPreferences.PROPERTIES#DOCUMENT_ROOT}.
     */
    private final SimpleObjectProperty<Path> rootDirProperty = new SimpleObjectProperty<>();

    /**
     * A sub-directory of {@link #rootDirProperty} to put imported photos into.
     */
    private final SimpleObjectProperty<Path> subDirProperty = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<CopyTask> copyTaskProperty = new SimpleObjectProperty<>();

    public PhotoImportPane() {
        super();
        SIRS.loadFXML(this);

        final BooleanBinding noRootConfigured = rootDirProperty.isNull();
        uiChooseSubDir.disableProperty().bind(noRootConfigured);
        uiSubDirLabel.disableProperty().bind(noRootConfigured);

        uiRootLabel.textProperty().bind(rootDirProperty.asString());
        uiSubDirLabel.textProperty().bind(subDirProperty.asString());

        sourceDirProperty.addListener(this::sourceChanged);
        rootDirProperty.addListener(this::destinationChanged);
        subDirProperty.addListener(this::destinationChanged);

        uiCopyMessage.managedProperty().bind(uiCopyMessage.visibleProperty());
        uiCopyMessage.visibleProperty().bind(uiCopyMessage.textProperty().isEmpty());
    }

    private void sourceChanged(final ObservableValue<? extends Path> obs, final Path oldValue, final Path newValue) {
        if (newValue != null) {
            try {
                final FileStore fileStore = newValue.getFileSystem().provider().getFileStore(newValue);

                uiSourceName.setText(fileStore.name());
                uiSourceType.setText(fileStore.type());

                final long usableSpace = fileStore.getUsableSpace();
                final long totalSpace = fileStore.getTotalSpace();
                uiSourceUsableSpace.setText(SIRS.toReadableSize(usableSpace));
                uiSoureSpaceProgress.setProgress(totalSpace <= 0 ? 1 : 1 - (usableSpace / totalSpace));
            } catch (IOException e) {
                GeotkFX.newExceptionDialog("L'analyse du media source a échoué. Impossible de définir le périphérique choisi comme source de l'import.", e);
                sourceDirProperty.set(null);
            }
        } else {
            uiSourceName.setText("N/A");
            uiSourceType.setText("N/A");
            uiSourceUsableSpace.setText("N/A");
            uiSoureSpaceProgress.setProgress(0);
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
            uiDestSpaceProgress.setProgress(0);
        } else {
            final Path subDir = subDirProperty.get();
            final Path absolutePath = subDir == null? rootDirProperty.get() : rootDirProperty.get().resolve(subDir);

            try {
                final FileStore fileStore = newValue.getFileSystem().provider().getFileStore(absolutePath);

                final long usableSpace = fileStore.getUsableSpace();
                final long totalSpace = fileStore.getTotalSpace();
                uiDestSpaceProgress.setProgress(totalSpace <= 0 ? 1 : 1 - (usableSpace / totalSpace));
            } catch (IOException e) {
                GeotkFX.newExceptionDialog("L'analyse du dossier destination a échoué. Veuillez choisir un autre dossier destination.", e);
            }
        }
    }

    /*
     * UI ACTIONS
     */

    @FXML
    void chooseSource(ActionEvent event) {
        sourceDirProperty.set(MobilePlugin.chooseMedia(getScene().getWindow()));
    }

    @FXML
    void chooseSubDirectory(ActionEvent event) {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Répertoire destination");
        chooser.setInitialDirectory(rootDirProperty.get().toFile());
        File chosen = chooser.showDialog(getScene().getWindow());
        if (chosen != null) {
            subDirProperty.set(chosen.toPath());
        }
    }

    @FXML
    void configureRoot(ActionEvent event) {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir un répertoire racine : ");
        chooser.setInitialDirectory(rootDirProperty.get().toFile());
        File chosen = chooser.showDialog(getScene().getWindow());
        if (chosen != null) {
            rootDirProperty.set(chosen.toPath().toAbsolutePath());
            SirsPreferences.INSTANCE.setProperty(SirsPreferences.PROPERTIES.DOCUMENT_ROOT.name(), rootDirProperty.get().toString());
        }
    }

    /**
     * Refresh UI bindings on copy task change.
     *
     * @param obs
     * @param oldTask
     * @param newTask
     */
    void copyTaskUpdate(final ObservableValue<? extends CopyTask> obs, CopyTask oldValue, CopyTask newValue) {
        if (oldValue != null) {
            uiCopyMessage.textProperty().unbind();
            copyMessageTooltip.textProperty().unbind();
            uiImportProgress.progressProperty().unbind();
        }
        if (newValue != null) {
            uiCopyMessage.textProperty().bind(newValue.messageProperty());
            copyMessageTooltip.textProperty().bind(newValue.messageProperty());
            uiImportProgress.progressProperty().bind(newValue.progressProperty());
        }
    }

    @FXML
    void importPhotos(ActionEvent event) {
        // If another task is already running, import button will have "cancel" button role.
        if (copyTaskProperty.get() != null) {
            copyTaskProperty.get().cancel();
            copyTaskProperty.set(null);
            return;
        }

        // TODO : analyze media files.
        final CopyTask cpTask = new CopyTask(null, null);
        copyTaskProperty.set(cpTask);

        // TODO : disable buttons.
        uiImportBtn.setText("Annuler");
        TaskManager.INSTANCE.submit(cpTask);

        // When finished, we reset task and panel.
        cpTask.runningProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                Platform.runLater(() -> {
                    uiImportBtn.setText("Importer");
                    copyTaskProperty.set(null);
                });
            }
        });
    }
}
