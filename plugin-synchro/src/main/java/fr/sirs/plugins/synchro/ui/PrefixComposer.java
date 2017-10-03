package fr.sirs.plugins.synchro.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.plugins.synchro.ui.mount.PhotoImportPane;
import static fr.sirs.plugins.synchro.ui.mount.PhotoImportPane.ICON_TRASH_BLACK;
import static fr.sirs.plugins.synchro.ui.mount.PhotoImportPane.moveSelectedElements;
import fr.sirs.plugins.synchro.common.PrefixBuilder;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PrefixComposer extends StackPane {
    @FXML
    private TitledPane uiPrefixTitledPane;

    @FXML
    private ComboBox<Character> uiSeparatorChoice;

    @FXML
    private ListView<PropertyDescriptor> uiPrefixListView;

    @FXML
    private Button uiAddPrefixBtn;

    @FXML
    private Button uiMoveUpBtn;

    @FXML
    private Button uiMoveDownBtn;

    @FXML
    private Button uiDeletePrefixBtn;

    private final ObservableList<PropertyDescriptor> availablePrefixes = FXCollections.observableArrayList();

    private final ObjectBinding<Function<SIRSFileReference, String>> prefixBuilder;

    public PrefixComposer() {
        SIRS.loadFXML(this);

        final ObservableList<Character> prefixSeparators = FXCollections.observableArrayList();
        prefixSeparators.addAll(' ', '.', '-', '_');
        SIRS.initCombo(uiSeparatorChoice, prefixSeparators, '.');

        uiPrefixListView.setItems(FXCollections.observableArrayList());
        uiPrefixListView.setCellFactory(param -> new PhotoImportPane.PrefixCell());
        uiPrefixListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set button icons
        uiAddPrefixBtn.setGraphic(new ImageView(SIRS.ICON_ADD_BLACK));
        uiAddPrefixBtn.setText(null);
        uiMoveUpBtn.setGraphic(new ImageView(SIRS.ICON_CARET_UP_BLACK));
        uiMoveUpBtn.setText(null);
        uiMoveDownBtn.setGraphic(new ImageView(SIRS.ICON_CARET_DOWN_BLACK));
        uiMoveDownBtn.setText(null);
        uiDeletePrefixBtn.setGraphic(new ImageView(ICON_TRASH_BLACK));
        uiDeletePrefixBtn.setText(null);

        try {
            availablePrefixes.addAll(SIRS.listSimpleProperties(AbstractPhoto.class).values());
        } catch (IntrospectionException ex) {
            SIRS.LOGGER.log(Level.WARNING, "Cannot identify available prefixes.", ex);
            setVisible(false);
            setManaged(false);
        }

        prefixBuilder = Bindings.createObjectBinding(() -> {
            if (uiPrefixListView.getItems().isEmpty()) {
                return null;
            }
            final Character separator = uiSeparatorChoice.getValue();
            return new PrefixBuilder(uiPrefixListView.getItems().stream().distinct().collect(Collectors.toList()), separator == null? "_":separator.toString());
        }, uiPrefixListView.getItems(), uiSeparatorChoice.valueProperty());
    }

    public ObjectBinding<Function<SIRSFileReference, String>> getPrefixBuilder() {
        return prefixBuilder;
    }



    @FXML
    void addPrefix(ActionEvent event) {
        ComboBox<PropertyDescriptor> choices = new ComboBox<>();
        SIRS.initCombo(choices, availablePrefixes, null);
        choices.setConverter(new PhotoImportPane.DescriptorConverter());

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.CANCEL, ButtonType.OK);
        alert.setResizable(true);
        alert.setWidth(400);
        alert.getDialogPane().setContent(choices);
        alert.setHeaderText("Choisissez un attribut à utiliser comme préfixe");

        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (ButtonType.OK.equals(result)) {
            PropertyDescriptor value = choices.getValue();
            if (value != null) {
                uiPrefixListView.getItems().add(value);
                availablePrefixes.remove(value);
            }
        }
    }

    @FXML
    void deletePrefix(ActionEvent event) {
        final MultipleSelectionModel<PropertyDescriptor> selectionModel = uiPrefixListView.getSelectionModel();
        final ObservableList<PropertyDescriptor> selected = selectionModel.getSelectedItems();
        if (!selected.isEmpty()) {
            uiPrefixListView.getItems().removeAll(selected);
            availablePrefixes.addAll(selected);
        }
        selectionModel.clearSelection();
    }

    @FXML
    void movePrefixDown(ActionEvent event) {
        moveSelectedElements(uiPrefixListView, PhotoImportPane.DIRECTION.DOWN);
    }

    @FXML
    void movePrefixUp(ActionEvent event) {
        moveSelectedElements(uiPrefixListView, PhotoImportPane.DIRECTION.UP);
    }
}
