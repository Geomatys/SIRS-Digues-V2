package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.report.ModeleElement;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.odt.ODTUtils;
import java.awt.Desktop;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.internal.GeotkFX;
import org.odftoolkit.simple.TextDocument;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXModeleElementPane extends AbstractFXElementPane<ModeleElement> {

    /**
     * Attributs que l'on ne souhaite pas garder dans le formulaire.
     */
    private static final List<String> FIELDS_TO_IGNORE = Arrays.asList(new String[] {SIRS.AUTHOR_FIELD, SIRS.VALID_FIELD, SIRS.FOREIGN_PARENT_ID_FIELD});

    private static String DESKTOP_UNSUPPORTED = "Impossible de dialoguer avec le système. Pour éditer le modèle, vous pouvez cependant utiliser la fonction d'export, "
            + "puis ré-importer votre ficher lorsque vous aurez terminé vos modifications.";

    private static String DESKTOP_FAILED = "Impossible de trouver un éditeur. Cela peut-être dû au fait que vous n'avez pas installé LibreOffice ou OpenOffice sur votre machine.";

    private static String MANUAL_EDIT = "Pour éditer le modèle, vous pouvez cependant utiliser la fonction d'export, puis ré-importer votre"
            + " ficher lorsque vous aurez terminé vos modifications.";

    @FXML
    private TextField uiTitle;

    @FXML
    private ComboBox<Class> uiTargetClass;

    @FXML
    private Button uiImportODT;

    @FXML
    private Button uiExportODT;

    @FXML
    private Label uiNoModelLabel;

    @FXML
    private Label uiModelPresentLabel;

    @FXML
    private Label uiSizeLabel;

    @FXML
    private HBox uiEditBar;

    @FXML
    private BorderPane uiPropertyPane;

    @FXML
    private ListView<String> uiAvailableProperties;

    @FXML
    private ListView<String> uiUsedProperties;

    @FXML
    private Button uiGenerate;

    @FXML
    private Label uiProgressLabel;

    @FXML
    private ProgressIndicator uiProgress;


    /** When ODT document is updated, it change label displaying its size. */
    private final ChangeListener<byte[]> sizeListener;

    /** A temporary file used for ODT modifications happening before save. */
    private final Path tempODT;
    private final SimpleBooleanProperty tempODTexists = new SimpleBooleanProperty(false);

    public FXModeleElementPane() {
        super();
        SIRS.loadFXML(this);

        // Build ordered list of possible target classes.
        final SirsStringConverter converter = new SirsStringConverter();
        final Collator collator = Collator.getInstance();
        final ObservableList<Class> classes = FXCollections.observableArrayList(
                Injector.getSession().getAvailableModels());
        classes.sort((o1, o2) -> collator.compare(converter.toString(o1), converter.toString(o2)));
        SIRS.initCombo(uiTargetClass, classes, null);

        // Modify available properties when target class is modified.
        uiTargetClass.valueProperty().addListener(this::updateAvailableProperties);
        uiExportODT.setDisable(true);
        uiEditBar.managedProperty().bind(visibleProperty());
        uiEditBar.setVisible(false);
        uiSizeLabel.setVisible(false);
        uiModelPresentLabel.setVisible(false);
        uiGenerate.setDisable(true);

        // Activation rules.
        uiTitle.disableProperty().bind(disableFieldsProperty());
        uiTargetClass.disableProperty().bind(disableFieldsProperty());
        uiImportODT.disableProperty().bind(disableFieldsProperty());
        uiEditBar.disableProperty().bind(disableFieldsProperty());
        uiPropertyPane.disableProperty().bind(disableFieldsProperty());
        uiGenerate.disableProperty().bind(disableFieldsProperty().or(Bindings.isEmpty(uiUsedProperties.getItems())));

        // Panel update
        elementProperty.addListener(this::elementChanged);
        sizeListener = (obs, oldValue, newValue) -> {
            if (newValue == null) {
                uiSizeLabel.setText("Modèle inexistant");
            } else {
                uiSizeLabel.setText(SIRS.toReadableSize(newValue.length));
            }
        };

        // Prepare reference to temporary file used for ODT edition. Keep it suppressed until we need it.
        try {
            tempODT = Files.createTempFile("sirs", "odt");
            Files.delete(tempODT);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create temporary file !", ex);
        }
    }

    public FXModeleElementPane(final ModeleElement input) {
        this();
        setElement(input);
    }

    private void elementChanged(ObservableValue<? extends ModeleElement> obs, ModeleElement oldElement, ModeleElement newElement) {
        if (oldElement != null) {
            uiTitle.textProperty().unbindBidirectional(oldElement.libelleProperty());
            uiEditBar.visibleProperty().unbind();
            uiExportODT.visibleProperty().unbind();
            uiNoModelLabel.visibleProperty().unbind();
            uiModelPresentLabel.visibleProperty().unbind();

            uiSizeLabel.visibleProperty().unbind();
            oldElement.odtProperty().removeListener(sizeListener);
        }

        if (newElement != null) {
            uiTitle.textProperty().bindBidirectional(newElement.libelleProperty());
            final BooleanBinding elementPresent = newElement.odtProperty().isNotNull();
            uiEditBar.visibleProperty().bind(elementPresent.or(tempODTexists));
            uiExportODT.disableProperty().bind(elementPresent);
            uiNoModelLabel.visibleProperty().bind(elementPresent);
            uiModelPresentLabel.visibleProperty().bind(elementPresent);

            uiSizeLabel.visibleProperty().bind(elementPresent);
            newElement.odtProperty().addListener(sizeListener);

            final byte[] odt = newElement.getOdt();

            // Temporary file for ODT edition
            try {
                if (odt == null || odt.length < 1) {
                    Files.deleteIfExists(tempODT);
                    tempODTexists.set(false);
                } else {
                    Files.write(tempODT, odt, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    tempODTexists.set(true);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot work with temporary file !", ex);
            }
        } else {
            uiTitle.setText(null);
            uiTargetClass.setValue(null);
        }
    }

    /**
     * Called when model class is changed. It reloads property lists, and update
     * ODT file to keep only properties common to both the old and new types.
     *
     * TODO : update ODT file
     *
     * @param obs Originating property which has changed.
     * @param oldValue Old model type.
     * @param newValue New model type.
     */
    private void updateAvailableProperties(ObservableValue<? extends Class> obs, Class oldValue, Class newValue) {
        if (newValue == null) {
            uiAvailableProperties.setItems(null);
            uiUsedProperties.setItems(null);
            return;
        }

        final Set<String> properties;
        try {
            properties = SIRS.listSimpleProperties(newValue).keySet();
        } catch (IntrospectionException ex) {
            GeotkFX.newExceptionDialog("Une erreur inattendue est survenue lors de la sélection du type d'objet.", ex);
            uiTargetClass.setValue(oldValue);
            return;
        }
        properties.removeAll(FIELDS_TO_IGNORE);

        // refresh available property list
        uiAvailableProperties.setItems(FXCollections.observableArrayList(properties));

        // Remove all selected properties not present into selected type.
        final Iterator<String> usedProps = uiUsedProperties.getItems().iterator();
        while (usedProps.hasNext()) {
            if (!properties.remove(usedProps.next())) {
                usedProps.remove();
            }
        }

        generateODT(null);
    }

    @FXML
    void addProperties(ActionEvent event) {
        final ObservableList<String> selectedItems = uiAvailableProperties.getSelectionModel().getSelectedItems();
        uiUsedProperties.getItems().addAll(selectedItems);
        uiAvailableProperties.getItems().removeAll(selectedItems);
    }

    @FXML
    void deleteODT(ActionEvent event) {
        try {
            Files.deleteIfExists(tempODT);
            tempODTexists.set(false);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot delete temporary file !", ex);
        }
    }

    @FXML
    void editODT(ActionEvent event) {
        if (Desktop.isDesktopSupported()) {
            final Task<Boolean> editTask  = TaskManager.INSTANCE.submit("Edition d'un modèle ODT", () -> {
                final Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.EDIT)) {
                    desktop.edit(tempODT.toAbsolutePath().toFile());
                } else if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(tempODT.toAbsolutePath().toFile());
                } else {
                    return false;
                }
                return true;
            });

            uiProgressLabel.setText("Édition du modèle en cours");
            uiProgressLabel.visibleProperty().bind(editTask.runningProperty());
            uiProgress.visibleProperty().bind(editTask.runningProperty());

            editTask.setOnSucceeded(taskEvent -> {
                final Alert alert;
                if (Boolean.TRUE.equals(taskEvent.getSource().getValue())) {
                    alert = new Alert(AlertType.WARNING, DESKTOP_FAILED + " " + MANUAL_EDIT, ButtonType.OK);
                } else {
                    alert = new Alert(AlertType.INFORMATION, "Edition terminée", ButtonType.OK);
                }
                alert.setResizable(true);
                alert.show();
            });

            editTask.setOnFailed(taskEvent -> {
                if (taskEvent.getSource().getException() != null) {
                    GeotkFX.newExceptionDialog("Une erreur est survenue lors de l'édition du modèle ODT", taskEvent.getSource().getException()).show();
                } else {
                    final Alert alert = new Alert(AlertType.ERROR, "Une erreur inattendue est survenue lors de l'édition du modèle ODT.", ButtonType.OK);
                    alert.setResizable(true);
                    alert.show();
                }
            });
        } else {
            Alert alert = new Alert(AlertType.WARNING, DESKTOP_UNSUPPORTED+ " "+MANUAL_EDIT, ButtonType.OK);
            alert.setResizable(true);
            alert.show();
        }
    }

    @FXML
    void exportODT(ActionEvent event) {
        final FileChooser outputChooser = new FileChooser();
        File output = outputChooser.showSaveDialog(getScene().getWindow());
        if (output != null) {
            try {
                if (Files.isRegularFile(tempODT)) {
                    Files.copy(tempODT, output.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.write(output.toPath(), elementProperty.get().getOdt(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot export ODT template !", ex);
            }
        }
    }

    @FXML
    void generateODT(ActionEvent event) {
        final ObservableList<String> items = uiUsedProperties.getItems();

        // Get a title for each wanted property.
        final HashMap<String, String> properties = new HashMap<>(items.size());
        final Function<String, String> nameMapper = createPropertyNameMapper(uiTargetClass.getValue());
        for (final String prop : items) {
            properties.put(prop, nameMapper.apply(prop));
        }

        if (Files.isRegularFile(tempODT)) {
            final Alert alert = new Alert(AlertType.WARNING, "Attention, le modèle existant sera modifié, êtes-vous sûr ?", ButtonType.NO, ButtonType.YES);
            alert.setResizable(true);
            if (ButtonType.YES.equals(alert.showAndWait().orElse(null))) {
                try {
                    final TextDocument tmpDoc;
                    try (final InputStream docInput = Files.newInputStream(tempODT, StandardOpenOption.READ)) {
                        tmpDoc = TextDocument.loadDocument(docInput);
                        ODTUtils.setVariables(tmpDoc, properties);
                    }
                    try (OutputStream docOutput = Files.newOutputStream(tempODT, StandardOpenOption.WRITE)) {
                        tmpDoc.save(docOutput);
                    }
                    tempODTexists.set(true);
                } catch (Exception e) {
                    SirsCore.LOGGER.log(Level.WARNING, "Cannot modify template !", e);
                    GeotkFX.newExceptionDialog("Une erreur est survenue lors de la génération du modèle ODT.", e);
                }
            }
        } else {
            try (final OutputStream outputStream = Files.newOutputStream(tempODT, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                final TextDocument newTemplate = ODTUtils.newSimplePropertyModel(uiTitle.getText(), properties);
                newTemplate.save(outputStream);
                tempODTexists.set(true);
            } catch (Exception ex) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot create template !", ex);
                GeotkFX.newExceptionDialog("Une erreur est survenue lors de la génération du modèle ODT.", ex);
            }
        }
    }

    @FXML
    void importODT(ActionEvent event) {
        final FileChooser inputChooser = new FileChooser();
        inputChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tous", "*"));
        inputChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OpenOffice document", "*.odt", "*.ODT"));
        File tmpInput = inputChooser.showOpenDialog(getScene().getWindow());
        if (tmpInput != null) {
            final Path input = tmpInput.toPath();
            try (final InputStream stream = Files.newInputStream(input, StandardOpenOption.READ)) {
                TextDocument.loadDocument(stream); // only here to ensure we import a valid ODT.
                Files.copy(input, tempODT, StandardCopyOption.REPLACE_EXISTING);
                tempODTexists.set(true);
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot import file : "+input.toString(), ex);
            } catch (Exception ex) {
                throw new IllegalStateException("Input file is not an ODT file !");
            }
        }
    }

    @FXML
    void removeProperties(ActionEvent event) {
        final ObservableList<String> selectedItems = uiUsedProperties.getSelectionModel().getSelectedItems();
        uiAvailableProperties.getItems().addAll(selectedItems);
        uiUsedProperties.getItems().removeAll(selectedItems);
    }

    @Override
    public void preSave() throws Exception {
        ModeleElement result = elementProperty.get();
        final Class targetClass = uiTargetClass.valueProperty().get();
        if (targetClass != null) {
            result.setTargetClass(targetClass.getCanonicalName());
        } else {
            result.setTargetClass(null);
        }

        // TODO : Do this only if ODT has been edited.
        if (Files.isRegularFile(tempODT)) {
            result.setOdt(Files.readAllBytes(tempODT));
        } else {
            result.setOdt(null);
        }
    }

    /**
     * Create a function giving display name for properties of a given class.
     * @param propertyHolder Class holding properties to translate.
     * @return A function which take a property name, and return a display name for it.
     */
    public static Function<String, String> createPropertyNameMapper(final Class propertyHolder) {
        final LabelMapper mapper = LabelMapper.get(propertyHolder);
        if (mapper == null) {
            /*
             * If we cannot find any mapper, we try to build a decent name by putting space on word end / beginning.
             */
            return (input) -> input.replaceAll("([A-Z0-9][^A-Z0-9])", " $1").replaceAll("([^A-Z0-9\\s])([A-Z0-9])", "$1 $2");
        } else {
            return (input) -> mapper.mapPropertyName(input);
        }
    }
}
