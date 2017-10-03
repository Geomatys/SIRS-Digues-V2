package fr.sirs.plugins.synchro.ui.database;

import fr.sirs.SIRS;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.plugins.synchro.attachment.AttachmentUtilities;
import fr.sirs.plugins.synchro.common.DocumentUtilities;
import fr.sirs.plugins.synchro.common.TaskProvider;
import fr.sirs.plugins.synchro.concurrent.AsyncPool;
import fr.sirs.plugins.synchro.ui.DocumentSelector;
import fr.sirs.ui.Growl;
import fr.sirs.util.SirsStringConverter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletionException;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class LocalDistantView extends SplitPane implements TaskProvider {

    @FXML
    private ListView<SIRSFileReference> uiDesktopList;

    @FXML
    private ListView<SIRSFileReference> uiMobileList;

    @FXML
    private Button uiDesktopToMobile;

    @FXML
    private Button uiDelete;

    @FXML
    private Label uiLocalSize;

    @FXML
    private Label uiDistantSize;

    private final CouchDbConnector connector;
    private final ObservableList<SIRSFileReference> documents;

    private final AsyncPool pool = new AsyncPool(7);

    private final ReadOnlyObjectWrapper<Task> taskProperty = new ReadOnlyObjectWrapper<>();

    public LocalDistantView(final CouchDbConnector connector, final ObservableList<SIRSFileReference> documents) {
        this.connector = connector;
        this.documents = documents;

        SIRS.loadFXML(this);

        uiDesktopList.setCellFactory((param) -> new DocumentSelector.TextCell());
        uiMobileList.setCellFactory((param) -> new DocumentSelector.TextCell());

        uiDelete.disableProperty().bind(uiMobileList.getSelectionModel().selectedItemProperty().isNull());
        uiDesktopToMobile.disableProperty().bind(uiDesktopList.getSelectionModel().selectedItemProperty().isNull());

        updateSizeOnSelectionChange(uiDesktopList.getSelectionModel().getSelectedItems(), uiLocalSize);
        updateSizeOnSelectionChange(uiMobileList.getSelectionModel().getSelectedItems(), uiDistantSize);

        this.documents.addListener((Observable o) -> updateLists());
    }

    private void updateSizeOnSelectionChange(final ObservableList<SIRSFileReference> selection, final Label display) {
        selection.addListener((Observable obs) -> {
            if (selection.isEmpty()) {
                uiDistantSize.setText("N/A");
            } else {
                final ArrayList<SIRSFileReference> defCopy = new ArrayList<>(selection);
                final TaskManager.MockTask t = new TaskManager.MockTask(() -> {return this.getSize(defCopy.stream());});
                t.setOnSucceeded((evt) -> Platform.runLater(() -> {
                    final long size = (long) t.getValue();
                    display.setText(size <= 0 ? "N/A" : SIRS.toReadableSize(size));
                }));
                TaskManager.INSTANCE.submit(t);
            }
        });
    }

    @FXML
    void deleteFromMobile(ActionEvent event) {
        ObservableList<SIRSFileReference> selectedItems = uiMobileList.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty())
            return;

        final UnaryOperator<SIRSFileReference> delete = ref -> {
            try {
                AttachmentUtilities.delete(connector, ref);
            } catch (Exception e) {
                throw new ParameterizedException(ref, e.getLocalizedMessage(), e);
            }

            return ref;
        };

        final Task<Void> deletor = pool.prepare(delete)
                .setTarget(new ArrayList<>(selectedItems).stream())
                .setWhenComplete(this::handleDeletionResult)
                .build();

        // TODO : better management of synchronous tasks.
        TaskManager.INSTANCE.submit(deletor);
    }

    private void handleDeletionResult(final SIRSFileReference ref, final Throwable error) {
        if (error == null) {
            SIRS.fxRun(false, () -> {
                uiMobileList.getItems().remove(ref);
                if (DocumentUtilities.isFileAvailable(ref))
                    uiDesktopList.getItems().add(ref);
            });
        } else if (error instanceof Error) {
            throw (Error) error;
        } else {
            Throwable search = error;
            while (search != null && !(search instanceof ParameterizedException)) {
                search = search.getCause();
            }

            final String msg;
            if (search instanceof ParameterizedException) {
                SIRS.LOGGER.log(Level.WARNING, search, () -> "Cannot delete attachment for " + ref);
                msg = String.format("Impossible de supprimer le document en base.%nDocument : %s%nCause : %s",
                        new SirsStringConverter().toString(ref), search.getLocalizedMessage());
            } else {
                SIRS.LOGGER.log(Level.WARNING, error, () -> "Cannot delete an attachment");
                msg = String.format("Impossible de supprimer un document de la base de données.%nCause : %s", error.getLocalizedMessage());
            }

            SIRS.fxRun(false, () -> new Growl(Growl.Type.ERROR, msg).showAndFade());
        }
    }

    private void handleUploadResult(final SIRSFileReference ref, final Throwable error) {
        if (error == null) {
            SIRS.fxRun(false, () -> {
                uiDesktopList.getItems().remove(ref);
                uiMobileList.getItems().add(ref);
            });
        } else if (error instanceof Error) {
            throw (Error) error;
        } else {
            Throwable search = error;
            while (search != null && !(search instanceof ParameterizedException)) {
                search = search.getCause();
            }

            final String msg;
            if (search instanceof ParameterizedException) {
                SIRS.LOGGER.log(Level.WARNING, search, () -> "Cannot upload attachment for " + ref);
                msg = String.format("Impossible d'envoyer le document en base.%nDocument : %s%nCause : %s",
                        new SirsStringConverter().toString(ref), search.getLocalizedMessage());
            } else {
                SIRS.LOGGER.log(Level.WARNING, error, () -> "Cannot upload an attachment");
                msg = String.format("Impossible d'envoyer un document vers la base de données.%nCause : %s", error.getLocalizedMessage());
            }

            SIRS.fxRun(false, () -> new Growl(Growl.Type.ERROR, msg).showAndFade());
        }
    }

    @FXML
    void sendToMobileList(ActionEvent event) {
        ObservableList<SIRSFileReference> selectedItems = uiDesktopList.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty())
            return;

        final UnaryOperator<SIRSFileReference> upload = ref -> {
            try {
                AttachmentUtilities.upload(connector, ref);
            } catch (Exception e) {
                throw new ParameterizedException(ref, e.getLocalizedMessage(), e);
            }
            return ref;
        };

        Task<Void> uploader = pool.prepare(upload)
                .setTarget(new ArrayList<>(selectedItems).stream())
                .setWhenComplete(this::handleUploadResult)
                .build();
        TaskManager.INSTANCE.submit(uploader);
    }

    private void updateLists() {
        final ArrayList<SIRSFileReference> defCopy = new ArrayList<>(documents);
        final ArrayList<SIRSFileReference> localList = new ArrayList<>();
        final ArrayList<SIRSFileReference> distantList = new ArrayList<>();
        for (final SIRSFileReference ref : defCopy) {
            try {
                if (AttachmentUtilities.isAvailable(connector, ref)) {
                    distantList.add(ref);
                } else if (DocumentUtilities.isFileAvailable(ref)) {
                    localList.add(ref);
                }
            } catch (Exception e) {
                SIRS.LOGGER.log(Level.WARNING, "An document cannot be analyzed properly.", e);
            }
        }

        uiDesktopList.setItems(FXCollections.observableList(localList));
        uiMobileList.setItems(FXCollections.observableList(distantList));
    }

    private long getSize(final Stream<SIRSFileReference> data) {
        return data
                .mapToLong(this::size)
                .sum();
    }

    private long size(final SIRSFileReference ref) {
        final Path doc = SIRS.getDocumentAbsolutePath(ref);
        if (Files.exists(doc)) {
            try {
                return Files.size(doc);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else return AttachmentUtilities.size(connector, ref);
    }

    @Override
    public ObservableValue<Task> taskProperty() {
        return taskProperty.getReadOnlyProperty();
    }

    @Override
    public Task getTask() {
        return taskProperty.get();
    }

    public static class ParameterizedException extends CompletionException {
        final SIRSFileReference input;

        public ParameterizedException(SIRSFileReference input, String message) {
            super(message);
            this.input = input;
        }

        public ParameterizedException(SIRSFileReference input, String message, Throwable cause) {
            super(message, cause);
            this.input = input;
        }

        public ParameterizedException(SIRSFileReference input, Throwable cause) {
            super(cause);
            this.input = input;
        }
    }
}
