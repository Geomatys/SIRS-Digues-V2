package fr.sirs.plugins.synchro.ui.database;

import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.plugins.synchro.attachment.AttachmentUtilities;
import fr.sirs.plugins.synchro.common.DocumentUtilities;
import fr.sirs.plugins.synchro.common.PhotoFinder;
import fr.sirs.plugins.synchro.concurrent.AsyncPool;
import fr.sirs.ui.Growl;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoDownload extends StackPane {

    @FXML
    private Button uiCancel;

    @FXML
    private VBox uiProgressPane;

    @FXML
    private VBox uiForm;

    @FXML
    private Label uiProgressLabel;

    @FXML
    private RadioButton uiDateTrigger;

    @FXML
    private DatePicker uiDate;

    @FXML
    private Button uiEstimate;

    @FXML
    private Label uiNb;

    @FXML
    private Label uiSize;

    @FXML
    private Button uiImportBtn;

    private final Session session;

    private final ObservableValue<Function<AbstractPhoto, Path>> destinationProvider;

    private final AsyncPool pool;

    public PhotoDownload(final AsyncPool pool, final Session session, final ObservableValue<Function<AbstractPhoto, Path>> destinationProvider) {
        ArgumentChecks.ensureNonNull("Asynchronous executor", pool);
        ArgumentChecks.ensureNonNull("Session", session);
        ArgumentChecks.ensureNonNull("Path provider", destinationProvider);
        SIRS.loadFXML(this);
        this.pool = pool;
        this.session = session;
        this.destinationProvider = destinationProvider;

        uiForm.disableProperty().bind(uiProgressPane.visibleProperty());
        uiDate.disableProperty().bind(uiDateTrigger.selectedProperty().not());
    }

    @FXML
    void estimate(ActionEvent event) {
        final Stream<AbstractPhoto> photos = getPhotographs();

        final CouchDbConnector connector = session.getConnector();

        final AtomicLong count = new AtomicLong();
        final TaskManager.MockTask<Long> t = new TaskManager.MockTask("Estimation...", () -> {
            final Thread th = Thread.currentThread();
            try {
                return photos
                        .peek(photo -> {
                            if (th.isInterrupted())
                                throw new RuntimeException(new InterruptedException());
                        })
                        .peek(photo -> count.incrementAndGet())
                        .mapToLong(photo -> AttachmentUtilities.size(connector, photo))
                        .sum();
            } catch (RuntimeException e) {
                if (e.getCause() instanceof InterruptedException) {
                    throw (InterruptedException) e.getCause();
                } else {
                    throw e;
                }
            }
        });

        t.setOnFailed(evt -> {
            SIRS.LOGGER.log(Level.WARNING, "Estimation failed", t.getException());
            Platform.runLater(() -> new Growl(Growl.Type.ERROR, "Impossible d'estimer le volume des données à télécharger.").showAndFade());
                });
        t.setOnSucceeded(evt -> Platform.runLater(() -> {
            final long nb = count.get();
            uiNb.setText(nb < 0? "inconnu" : Long.toString(nb));
            uiSize.setText(SIRS.toReadableSize(t.getValue()));
        }));

        uiProgressPane.visibleProperty().bind(t.runningProperty());

        uiCancel.setOnAction(evt -> t.cancel(true));

        TaskManager.INSTANCE.submit(t);
    }

    @FXML
    void importPhotos(ActionEvent event) {
        final CouchDbConnector connector = session.getConnector();
        final Function<AbstractPhoto, Path> destinationFinder = destinationProvider.getValue();
        if (destinationFinder == null) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Aucune destination spécifiée pour télécharger les données", ButtonType.OK);
            alert.setResizable(true);
            alert.show();
            return;
        }

        final LongProperty count = new SimpleLongProperty(0);
        final Function<AbstractPhoto, LongProperty> downloader = photo -> {
            final Path output = destinationFinder.apply(photo);
            try {
                download(connector, photo, output);
                return count;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };

        final Task<Void> download = pool.prepare(downloader)
                .setTarget(getPhotographs())
                .setWhenComplete(this::handleResult)
                .build();

        uiProgressPane.visibleProperty().bind(download.runningProperty());
        uiProgressLabel.textProperty().bind(Bindings.createStringBinding(() -> "Images téléchargées : "+count.get(), count));

        uiCancel.setOnAction(evt -> download.cancel(true));

        TaskManager.INSTANCE.submit(download);
    }

    private Stream<AbstractPhoto> getPhotographs() {
        Stream<AbstractPhoto> distantPhotos = new PhotoFinder(session).get();

        if (uiDateTrigger.isSelected()) {
            final LocalDate since = uiDate.getValue().minusDays(1);
            distantPhotos = distantPhotos.filter(photo -> photo.getDate() != null && since.isBefore(photo.getDate()));
        }

        final CouchDbConnector connector = session.getConnector();
        return distantPhotos
                // Skip photographs already downloaded
                .filter(photo -> !DocumentUtilities.isFileAvailable(photo))
                // Find photographs uploaded in database.
                .filter(photo -> AttachmentUtilities.isAvailable(connector, photo));
    }

    private void handleResult(final LongProperty count, final Throwable error) {
        if (error == null) {
            Platform.runLater(() -> count.set(count.get() + 1));
        } else if (error instanceof Error) {
            throw (Error) error;
        } else {
            SIRS.LOGGER.log(Level.WARNING, "Cannot download image", error);
            final String msg = String.format("Une image ne peut être téléchargée.%nCause : %s", error.getLocalizedMessage() == null? "Inconnue" : error.getLocalizedMessage());
            Platform.runLater(() -> new Growl(Growl.Type.ERROR, msg).showAndFade());
        }
    }

    private static void download(final CouchDbConnector connector, final AbstractPhoto photo, Path destination) throws IOException {
        AttachmentUtilities.download(connector, photo, destination);

        /* Once we've downloaded the image, we'll try to delete it from database.
         * Note : If this operation fails, we still consider operation as a
         * success, because the image is available locally.
         */
        try {
            AttachmentUtilities.delete(connector, photo);
        } catch (Exception e) {
            SIRS.LOGGER.log(Level.WARNING, "Cannot delete image from database", e);
        }
    }
}
