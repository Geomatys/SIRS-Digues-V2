package fr.sirs.plugins.mobile;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.core.model.SIRSReference;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.StringConverter;
import org.apache.sis.measure.NumberRange;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DocumentExportPane extends BorderPane {

    @FXML
    private ListView<SIRSFileReference> uiDesktopList;

    @FXML
    private Button uiDelete;

    @FXML
    private ListView<SIRSFileReference> uiMobileList;

    @FXML
    private Button uiDesktopToMobile;

    @FXML
    private ListView<Preview> uiTronconList;

    @FXML
    private DatePicker uiDate;

    @FXML
    private ComboBox<Class> uiDocumentType;

    @FXML
    private Button uiExportBtn;

    @FXML
    private ChoiceBox<Path> uiOutputChoice;

    @FXML
    private Label uiRemainingSpace;

    @Autowired
    private Session session;

    private final ObservableMap<Class, AbstractSIRSRepository<SIRSFileReference>> repositories = FXCollections.observableHashMap();

    public DocumentExportPane() {
        super();
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);

        uiDocumentType.setConverter(new ClassNameConverter());

        uiTronconList.setItems(FXCollections.observableList(session.getPreviews().getByClass(TronconDigue.class)));
        uiTronconList.setCellFactory((previews) -> new TextCell());
        uiTronconList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        uiDesktopList.setCellFactory(list -> new TextCell());
        uiDesktopList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiMobileList.setCellFactory(list -> new TextCell());
        uiMobileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        initRepositories();
        final ObservableList<Class> availableTypes = FXCollections.observableArrayList(repositories.keySet());
        availableTypes.add(0, SIRSFileReference.class);
        uiDocumentType.setItems(availableTypes);

        uiTronconList.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Preview> c) -> {
            updateDocumentList();
        });
        uiDocumentType.valueProperty().addListener((ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            updateDocumentList();
        });
        uiDate.valueProperty().addListener((ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            updateDocumentList();
        });

    }

    /**
     * Initialize map of available document providers from Spring context.
     */
    private final void initRepositories() {
        repositories.clear();
        final Collection<AbstractSIRSRepository> registeredRepositories = session.getRepositoriesForClass(SIRSFileReference.class);
        for (final AbstractSIRSRepository repo : registeredRepositories) {
            repositories.put(repo.getModelClass(), repo);
        }
    }

    private void updateDocumentList() {
        final Class docClass = uiDocumentType.getValue() == null? SIRSFileReference.class : uiDocumentType.getValue();
        final LocalDate date = uiDate.getValue();
        final ObservableList<Preview> selectedTroncons = uiTronconList.getSelectionModel().getSelectedItems();
        final ObservableList<SIRSFileReference> items = FXCollections.observableArrayList();
        /*
         * We will browse each troncon to get back all of its documents of queried
         * type and at the selected date.
         */
        for (final Preview p : selectedTroncons) {
            List<AbstractPositionDocument> docList = TronconUtils.getPositionDocumentList(p.getElementId());
            for (final AbstractPositionDocument doc : docList) {
                if (doc instanceof AbstractPositionDocumentAssociable) {
                    final String docId = ((AbstractPositionDocumentAssociable)doc).getSirsdocument();
                    if (docId != null && !docId.isEmpty()) {
                        session.getElement(docId).ifPresent(e -> {
                            // Temporal filter
                            if (docClass.isAssignableFrom(e.getClass())) {
                                if (date != null && e instanceof AvecBornesTemporelles) {
                                    AvecBornesTemporelles temp = (AvecBornesTemporelles) e;
                                    final Long filter = Timestamp.valueOf(date.atStartOfDay()).getTime();
                                    final Long start = temp.getDate_debut() == null? Long.MIN_VALUE : Timestamp.valueOf(temp.getDate_debut().atStartOfDay()).getTime();
                                    final Long end = temp.getDate_fin() == null? Long.MIN_VALUE : Timestamp.valueOf(temp.getDate_fin().atStartOfDay()).getTime();
                                    final NumberRange range = new NumberRange(Long.class, start, true, end, true);
                                    if (range.contains(filter)) {
                                        items.add((SIRSFileReference)e);
                                    }
                                } else {
                                    items.add((SIRSFileReference)e);
                                }
                            }
                        });
                    }
                }
            }
        }
        uiDesktopList.setItems(items);
    }

    /*
     * UI ACTIONS
     */

    @FXML
    void deleteFromMobile(ActionEvent event) {

    }

    @FXML
    void exportToMobile(ActionEvent event) {

    }


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

    private static class ClassNameConverter extends StringConverter<Class> {

        static final String ALL_DOCS = "Tous";

        @Override
        public String toString(Class object) {
            if (object == null)
                return "";
            else if (SIRSFileReference.class.equals(object))
                return ALL_DOCS;
            else try {
                LabelMapper mapper = LabelMapper.get(object);
                return mapper.mapClassName();
            } catch (MissingResourceException e) {
                return object.getSimpleName();
            }
        }

        @Override
        public Class fromString(String string) {
            if (ALL_DOCS.equalsIgnoreCase(string))
                return SIRSReference.class;
            else
                return null;
        }
    }
}