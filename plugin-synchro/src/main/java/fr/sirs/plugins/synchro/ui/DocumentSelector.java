package fr.sirs.plugins.synchro.ui;

import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugins.synchro.DocumentFinder;
import fr.sirs.ui.Growl;
import fr.sirs.util.DatePickerConverter;
import fr.sirs.util.SirsStringConverter;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DocumentSelector extends StackPane {

    @FXML
    private BorderPane uiConfigPane;

    @FXML
    private ListView<Preview> uiTronconList;

    @FXML
    private DatePicker uiDate;

    @FXML
    private ComboBox<Class> uiDocumentType;

    @FXML
    private BorderPane uiCopyPane;

    @FXML
    private Label uiCopyTitle;

    @FXML
    private ProgressBar uiCopyProgress;

    @FXML
    private Label uiCopyMessage;

    @FXML
    private BorderPane uiLoadingPane;

    @FXML
    private Label uiLoadingLabel;

    private final ObjectProperty<Task> searchTask = new SimpleObjectProperty<>();

    private final ObservableList<SIRSFileReference> documents;

    private final Session session;

    public DocumentSelector(final Session session) {
        super();
        SIRS.loadFXML(this);
        this.session = session;
        documents = FXCollections.observableArrayList();

        uiTronconList.setItems(FXCollections.observableList(session.getPreviews().getByClass(TronconDigue.class)));
        uiTronconList.setCellFactory((previews) -> new TextCell());
        uiTronconList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        final ObservableList<Class> availableTypes = FXCollections.observableArrayList(
                session.getRepositoriesForClass(SIRSFileReference.class).stream()
                        .map(AbstractSIRSRepository::getModelClass)
                        .collect(Collectors.toList())
        );
        availableTypes.add(0, SIRSFileReference.class);
        uiDocumentType.setItems(availableTypes);

        uiTronconList.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Preview> c) -> {
            updateDocuments();
        });
        uiDocumentType.valueProperty().addListener((ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            updateDocuments();
        });
        uiDate.valueProperty().addListener((ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            updateDocuments();
        });
        DatePickerConverter.register(uiDate);

        searchTask.addListener((obs, oldTask, newTask) -> {
            if (oldTask != null) {
                if (oldTask.isRunning()) {
                    oldTask.cancel(true);
                }
                uiCopyPane.visibleProperty().unbind();
            }

            if (newTask != null) {
                uiCopyPane.visibleProperty().bind(newTask.runningProperty());
                newTask.setOnSucceeded(evt -> {
                    final Object value = newTask.getValue();
                    if (value == null) {
                        documents.clear();
                    } else if (value instanceof Collection) {
                        documents.addAll((Collection)value);
                    } else {
                        SirsCore.LOGGER.warning("Search result :"+value);
                        new Growl(Growl.Type.ERROR, "La recherche a renvoyé un résultat inattendu : "+value).showAndFade();
                    }
                });
            }
        });
    }

    public ObservableList<SIRSFileReference> getDocuments() {
        return documents;
    }

    @FXML
    void cancelTask(ActionEvent event) {
        searchTask.set(null);
    }

    private void updateDocuments() {
        final Set<String> tdIds = uiTronconList.getSelectionModel().getSelectedItems().stream()
                .map(Preview::getElementId)
                .collect(Collectors.toSet());
        final DocumentFinder search = new DocumentFinder(uiDocumentType.getValue(), tdIds, uiDate.getValue(), session);
        searchTask.set(search);
    }

    private void manageTask(final ObservableValue obs, final Task oldTask, final Task newTask) {
        if (oldTask != null) {
            if (oldTask.isRunning()) {
                oldTask.cancel(true);
            }
            uiCopyPane.visibleProperty().unbind();
        }

        if (newTask != null) {
            uiCopyPane.visibleProperty().bind(newTask.runningProperty());
            newTask.setOnSucceeded(evt -> Platform.runLater(() -> {
                final Object value = newTask.getValue();
                if (value == null) {
                    documents.clear();
                } else if (value instanceof Collection) {
                    documents.addAll((Collection) value);
                } else {
                    SirsCore.LOGGER.warning("Search result :" + value);
                    new Growl(Growl.Type.ERROR, "La recherche a renvoyé un résultat inattendu : " + value).showAndFade();
                }
            }));

            newTask.setOnFailed(evt -> Platform.runLater(() -> {
                GeotkFX.newExceptionDialog("La recherche a échouée", newTask.getException()).show();
            }));

            TaskManager.INSTANCE.submit(newTask);
        }
    }

    /**
     * Cell to display label of input element.
     */
    private static class TextCell extends ListCell {

        final SirsStringConverter strConverter = new SirsStringConverter();

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || isEmpty()) {
                setText(null);
            } else {
                setText(strConverter.toString(item));
            }
        }
    }
}
