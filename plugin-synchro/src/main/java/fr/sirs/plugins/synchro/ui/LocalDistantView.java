package fr.sirs.plugins.synchro.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.plugins.synchro.attachment.AttachmentReference;
import fr.sirs.plugins.synchro.attachment.AttachmentUtilities;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class LocalDistantView extends SplitPane {

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

        // We cannot just get removed list and put it back in desktop list, because
        // filter could have changed multiple times.
        uiMobileList.getItems().removeAll(selectedItems);
    }

    @FXML
    void sendToMobileList(ActionEvent event) {
        ObservableList<SIRSFileReference> selectedItems = uiDesktopList.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty())
            return;

        uiMobileList.getItems().addAll(selectedItems);
        uiDesktopList.getItems().removeAll(selectedItems);
    }

    private void updateLists() {
        final ArrayList<SIRSFileReference> defCopy = new ArrayList<>(documents);
        final ArrayList<SIRSFileReference> localList = new ArrayList<>();
        final ArrayList<SIRSFileReference> distantList = new ArrayList<>();
        for (final SIRSFileReference ref : defCopy) {
            final String fileName = SIRS.getDocumentAbsolutePath(ref).getFileName().toString();
            try (final AttachmentInputStream in
                    = AttachmentUtilities.download(connector, new AttachmentReference(ref.getId(), fileName))) {
                distantList.add(ref);
            } catch (DocumentNotFoundException e) {
                localList.add(ref);
            } catch (IOException e) {
                SIRS.LOGGER.log(Level.WARNING, "An http resource cannot be closed.", e);
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
        } else {
            final AttachmentInputStream in = AttachmentUtilities.download(connector, new AttachmentReference(ref.getId(), doc.getFileName().toString()));
            return in.getContentLength();
        }
    }
}
