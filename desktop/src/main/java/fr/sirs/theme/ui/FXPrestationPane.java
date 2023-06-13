
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
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
import java.util.logging.Level;

/**
 * Class used to group prestations' linked pojotables  in 'categories' as for Desordres.
 * The group should be introduce in .jet files to automatically generate it for each reference with a categoried element types.
 * @author Maxime Gavens (Geomatys)
 */
public class FXPrestationPane extends FXPrestationPaneStub {
    private FXHorodatageFileField syntheseTableField;
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
        ui_gridpane.getChildren().remove(ui_syntheseTablePath);
        ui_gridpane.add(syntheseTableField, 1, 12);

        syntheseTableField.disableFieldsProperty.bind(disableFieldsProperty());
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
        if (syntheseTableField == null ) syntheseTableField = new FXHorodatageFileField();
        // Unbind fields bound to previous element.
        if (oldElement != null) {
            // Propriétés de Prestation
            syntheseTableField.textProperty().unbindBidirectional(oldElement.syntheseTablePathProperty());
            syntheseTableField.setText(null);
        }

        if (newElement != null) {

            /*
             * Bind control properties to Element ones.
             */
            // Propriétés de Prestation
            // * syntheseTablePath
            syntheseTableField.textProperty().bindBidirectional(newElement.syntheseTablePathProperty());
            syntheseTableField.checkInputTextValid(syntheseTableField.getText());
        }
    }

    private class FXHorodatageFileField extends AbstractPathTextField {

        private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty();

        public FXHorodatageFileField() {
            inputText.disableProperty().bind(disableFieldsProperty);
            choosePathButton.disableProperty().bind(disableFieldsProperty);

            inputText.focusedProperty().addListener((obs, oldValue, newValue) -> {
                final String text = this.getText();
                if (oldValue == true && newValue == false) {
                    checkInputTextValid(text);
                }
            });
        }

        private void checkInputTextValid(final String text) {
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
                checkInputTextValid(returned.getPath());
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
