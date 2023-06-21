
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.geotoolkit.gui.javafx.util.AbstractPathTextField;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Class used to group prestations' linked pojotables  in 'categories' as for Desordres.
 * The group should be introduced in .jet files to automatically generate it for each reference with a categoried element types.
 * @author Maxime Gavens (Geomatys)
 */
public class FXPrestationPane extends FXPrestationPaneStub {
    // As the Prestation's attribute syntheseTablePath is a String, the .jet automatically creates
    // a TextField ui_syntheseTablePath in FXPrestationPaneStub.java that shall also appear in FXPrestationPane.fxml.
    // However, we would like to have a FXHorodatageFileField instead of the basic TextField.
    // To do so, the ui_syntheseTablePath is removed from the parent and we add synthesisTableField instead.
    private FXHorodatageFileField ui_synthesisTableField;
    @FXML
    private GridPane ui_gridpane;
    @FXML
    private Label ui_errorMessage;

    public FXPrestationPane(final Prestation prestation){
        super(prestation);
        final Session session = Injector.getBean(Session.class);
        ui_desordreIds.setContent(() -> {
            // HACK_REDMINE_7605 - TronconLit has Prestation and DesordreLit : best would be to create PrestationLit and adapt all plugin-lit models
            Prestation presta = elementProperty().get();
            if (presta == null) throw new IllegalStateException("The prestation cannot be null");
            String linearId = presta.getLinearId();
            if (linearId == null) throw new IllegalStateException("The linearId of the prestation cannot be null");
            TronconDigue troncon = session.getRepositoryForClass(TronconDigue.class).get(linearId);
            if (troncon == null) throw new IllegalStateException("No element found for id " + linearId);
            if ("TronconLit".equals(troncon.getClass().getSimpleName()))
                desordreIdsTable = new PrestationDesordresPojoTable(elementProperty(), true);
            else desordreIdsTable = new PrestationDesordresPojoTable(elementProperty(), false);
            desordreIdsTable.editableProperty().bind(disableFieldsProperty().not());
            desordreIdsTable.createNewProperty().set(false);
            updateDesordreIdsTable(session, elementProperty.get());
            return desordreIdsTable;
        });
        // ui_syntheseTablePath is removed as it is replaced by ui_synthesisTableField
        ui_gridpane.getChildren().remove(ui_syntheseTablePath);
        ui_gridpane.add(ui_synthesisTableField, 1, 12);

        ui_synthesisTableField.disableFieldsProperty.bind(disableFieldsProperty());
        ui_errorMessage.visibleProperty().bind(ui_errorMessage.textProperty().isNotEmpty());
        ui_errorMessage.setTextFill(Color.RED);
        ui_errorMessage.setFont(new Font(12));
    }

    /**
     * Initialize fields at element setting.
     */
    @Override
    protected void initFields(ObservableValue<? extends Prestation > observableElement, Prestation oldElement, Prestation newElement) {
        super.initFields(observableElement, oldElement, newElement);
        // Cannot instantiate ui_synthesisTableField when creating the class attributes as it passes through initFields()
        // before creating the class attributes, leading to a NullPointerException.
        if (ui_synthesisTableField == null ) ui_synthesisTableField = new FXHorodatageFileField();
        // Unbind fields bound to previous element.
        if (oldElement != null) {
            // Propriétés de Prestation
            ui_synthesisTableField.textProperty().unbindBidirectional(oldElement.syntheseTablePathProperty());
            ui_synthesisTableField.setText(null);
        }

        if (newElement != null) {

            /*
             * Bind control properties to Element ones.
             */
            // Propriétés de Prestation
            // * syntheseTablePath
            ui_synthesisTableField.textProperty().bindBidirectional(newElement.syntheseTablePathProperty());
            ui_synthesisTableField.checkInputTextValid();
        }
    }

    private class FXHorodatageFileField extends AbstractPathTextField {

        private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty();

        /**
         * Supported formats for timestamped files.
         */
        private final List<String> supportedFormat = Collections.singletonList("*.pdf");

        public FXHorodatageFileField() {
            inputText.disableProperty().bind(disableFieldsProperty);
            choosePathButton.disableProperty().bind(disableFieldsProperty);

            inputText.focusedProperty().addListener((obs, oldValue, newValue) -> {
                if (oldValue && !newValue) {
                    checkInputTextValid();
                }
            });

            // Override the OnAction set in the parent.
            choosePathButton.setOnAction((ActionEvent e)-> {
                final String content = chooseInputContent();
                if (content != null) {
                    setText(content);
                }
                checkInputTextValid();
            });
        }

        private void checkInputTextValid() {
            final String text = this.getText();
            if (text != null && !text.isEmpty()) {
                if (!text.endsWith(".pdf")) {
                    ui_errorMessage.setText("Veuillez sélectionner un fichier au format PDF.");
                    return;
                }

                final File syntheseFile = new File(text);
                if (!syntheseFile.exists()) {
                    ui_errorMessage.setText("Le fichier est introuvable.");
                    return;
                }
            }
            ui_errorMessage.setText("");
        }

        @Override
        protected String chooseInputContent() {
            final FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported formats for cover and conclusion pages", supportedFormat));

            try {
                URI uriForText = getURIForText(getText());
                final Path basePath = Paths.get(uriForText);
                if (Files.isDirectory(basePath)) {
                    chooser.setInitialDirectory(basePath.toFile());
                } else if (Files.isDirectory(basePath.getParent())) {
                    chooser.setInitialDirectory(basePath.getParent().toFile());
                }
            } catch (Exception e) {
                // Well, we'll try without it...
                SirsCore.LOGGER.log(Level.FINE, "Input path cannot be decoded.", e);
            }
            File returned = chooser.showOpenDialog(null);
            if (returned == null) {
                return null;
            } else {
                return (completor.root != null) ?
                        completor.root.relativize(returned.toPath()).toString() : returned.getAbsolutePath();
            }
        }

        @Override
        protected URI getURIForText(String inputText) throws Exception {
            return new URI(inputText);
        }
    }
}
