package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.util.property.SirsPreferences;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import javafx.stage.Stage;
import org.apache.sis.util.logging.Logging;

import static fr.sirs.util.property.SirsPreferences.PROPERTIES.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javafx.beans.DefaultProperty;
import javafx.beans.property.Property;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import org.apache.sis.util.ArgumentChecks;

/**
 * Une fenêtre permettant d'éditer les préferences de l'application
 * (installation locale).
 *
 * TODO : replace spacing rules by CSS, and add styling rules.
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXPreferenceEditor extends Stage {

    private static final HashMap<SirsPreferences.PROPERTIES, Node> EDITOR_OVERRIDES = new HashMap<>();

    static {
        EDITOR_OVERRIDES.put(DOCUMENT_ROOT, new FXDirectoryTextField());
    }

    final BorderPane root = new BorderPane();

    final Button cancelBtn = new Button("Annuler");

    final Button saveBtn = new Button("Sauvegarder");

    final ObservableMap<SirsPreferences.PROPERTIES, String> editedProperties = FXCollections.observableHashMap();

    public FXPreferenceEditor() {
        setScene(new Scene(root));
        root.setPadding(new Insets(5));

        setTitle("Préférences");
        initializeCenter();
        initializeBottom();
    }

    private void initializeBottom() {
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction((ActionEvent e) -> {
            editedProperties.clear();
            hide();
        });

        saveBtn.setOnAction((ActionEvent e) -> this.save());
        saveBtn.disableProperty().bind(Bindings.size(editedProperties).lessThan(1));

        final HBox btnBar = new HBox(cancelBtn, saveBtn);
        btnBar.setSpacing(10);
        btnBar.setPadding(new Insets(10));
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(btnBar);
    }

    private void initializeCenter() {

        final GridPane propertyPane = new GridPane();
        propertyPane.setAlignment(Pos.CENTER);
        propertyPane.setMaxSize(Double.MAX_VALUE, USE_COMPUTED_SIZE);
        
        // label column, we always want it to fit content size.
        propertyPane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, true));
        // Propery editor. We make it fit available space.
        propertyPane.getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.SOMETIMES, HPos.LEFT, true));
        // Undo button, unresizable.
        propertyPane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, HPos.RIGHT, true));
        propertyPane.setVgap(10);
        propertyPane.setHgap(10);
        propertyPane.setPadding(new Insets(10));

        int row = 0;
        for (final SirsPreferences.PROPERTIES p : SirsPreferences.PROPERTIES.values()) {
            final Label propLibelle = new Label(p.title);
            propLibelle.setTooltip(new Tooltip(p.description));
            propertyPane.add(propLibelle, 0, row);

            try {
                final Node propEditor = getValidEditor(p);
                final Property targetProperty = getNodeProperty(propEditor);

                final String propertySafe = SirsPreferences.INSTANCE.getPropertySafe(p.name());
                if (propertySafe != null) {
                    targetProperty.setValue(propertySafe);
                } else {
                    targetProperty.setValue(p.getDefaultValue());
                }

                targetProperty.addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
                    if (!newValue.equals(SirsPreferences.INSTANCE.getPropertySafe(p.name()))) {
                        editedProperties.put(p, newValue.toString());
                    }
                });
                propertyPane.add(propEditor, 1, row);

                final Button resetValueBtn = new Button();
                resetValueBtn.setGraphic(new ImageView(SIRS.ICON_UNDO_BLACK));
                resetValueBtn.setPrefSize(16, 16);
                resetValueBtn.setOnAction((ActionEvent e) -> {
                    final String propertySafe1 = SirsPreferences.INSTANCE.getPropertySafe(p.name());
                    if (propertySafe1 != null) {
                        targetProperty.setValue(propertySafe1);
                    } else if (p.getDefaultValue() != null) {
                        targetProperty.setValue(p.getDefaultValue());
                    } else {
                        targetProperty.setValue(null);
                    }
                });
                propertyPane.add(resetValueBtn, 2, row);
            } catch (Exception e) {
                SIRS.LOGGER.log(Level.WARNING, "No editor can be displayed for property " + p.name(), e);
                propertyPane.add(new Label("Une erreur s'est produite pendant la création de l'éditeur."), 2, row, 2, 1);
            }
            row++;
        }
        
        final ScrollPane scroll = new ScrollPane(propertyPane);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);
    }

//    private void initializeTop() {
//        final Label titleLabel = new Label("Préférences");
//        root.setTop(titleLabel);
//    }
    private synchronized void save() {
        try {
            SirsPreferences.INSTANCE.store(editedProperties);
            editedProperties.clear();
        } catch (IOException ex) {
            final String errorCode = UUID.randomUUID().toString();
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Les préférences ne peuvent être sauvegardées. Code d'erreur : " + errorCode, ButtonType.CLOSE);
            alert.setResizable(true);
            alert.showAndWait();
            Logging.getLogger(SirsPreferences.class).log(Level.SEVERE, errorCode + " : Preferences cannot be saved.", ex);
        }
    }

    protected static Node getValidEditor(SirsPreferences.PROPERTIES p) {
        Node editor = EDITOR_OVERRIDES.get(p);
        if (editor != null && editor.getClass().isAnnotationPresent(DefaultProperty.class)) {
            return editor;
        } else {
            return new TextField();
        }
    }

    protected static Property getNodeProperty(final Node inputNode) throws IllegalArgumentException {
        ArgumentChecks.ensureNonNull("Input node", inputNode);
        if (inputNode instanceof TextInputControl) {
            return ((TextInputControl) inputNode).textProperty();
        } else if (inputNode instanceof CheckBox) {
            return ((CheckBox)inputNode).selectedProperty();
        }
        
        // Fallback case. We'll try to get node default property.
        final Class<? extends Node> nodeClass = inputNode.getClass();
        DefaultProperty annotation = nodeClass.getAnnotation(DefaultProperty.class);
        if (annotation == null) {
            throw new IllegalArgumentException("No valid default property for input node.");
        }

        final String propertyName = annotation.value() + "Property";
        try {
            Field field = nodeClass.getField(propertyName);
            return (Property) field.get(inputNode);
        } catch (Exception e) {
            try {
                Method method = nodeClass.getMethod(propertyName);
                return (Property) method.invoke(inputNode);
            } catch (Exception eBis) {
                IllegalArgumentException toThrow = new IllegalArgumentException("No valid default property for input node.", eBis);
                toThrow.addSuppressed(e);
                throw toThrow;
            }
        }
    }
}
