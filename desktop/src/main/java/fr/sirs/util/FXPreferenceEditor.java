package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.util.property.SirsPreferences;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.sis.util.logging.Logging;

/**
 * Une fenêtre permettant d'éditer les préferences de l'application (installation
 * locale).
 * 
 * TODO : replace spacing rules by CSS, and add styling rules.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class FXPreferenceEditor extends Stage {

    final BorderPane root = new BorderPane();
    
    final Button cancelBtn = new Button("Annuler");
    
    final Button saveBtn = new Button("Sauvegarder");
    
    final ObservableMap<SirsPreferences.PROPERTIES, String> editedProperties = FXCollections.observableHashMap();
        
    public FXPreferenceEditor() {
        setScene(new Scene(root));        
        root.setPadding(new Insets(5));
        
        initializeTop();
        initializeCenter();
        initializeBottom();
        
    }
    
    private void initializeBottom() {
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction((ActionEvent e)->{editedProperties.clear(); hide();});
        
        saveBtn.setOnAction((ActionEvent e) -> this.save());
        saveBtn.disableProperty().bind(Bindings.size(editedProperties).lessThan(1));
        
        final HBox btnBar = new HBox(cancelBtn, saveBtn);
        btnBar.setSpacing(10);
        btnBar.setPadding(new Insets(10));
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(btnBar);
    }
    
    private void initializeCenter() {
        final VBox propertyList = new VBox();
        ObservableList<Node> children = propertyList.getChildren();
        propertyList.setSpacing(10);
        for (final SirsPreferences.PROPERTIES p : SirsPreferences.PROPERTIES.values()) {
            final TextInputControl propEditor;
            if (p.propertyEditor == null) {
                propEditor = new TextField();
            } else {
                propEditor = p.propertyEditor;
                propEditor.setMinWidth(16);
                propEditor.setMaxWidth(500);
            }
            propEditor.textProperty().setValue(SirsPreferences.INSTANCE.getProperty(p.name()));
            propEditor.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                if (!newValue.equals(SirsPreferences.INSTANCE.getProperty(p.name()))) {
                    editedProperties.put(p, newValue);
                }
            });
            final Label propLibelle = new Label(p.title);
            final Button resetValueBtn = new Button();
            resetValueBtn.setGraphic(new ImageView(SIRS.ICON_UNDO_BLACK));
            resetValueBtn.setOnAction((ActionEvent e)->propEditor.textProperty().setValue(SirsPreferences.INSTANCE.getProperty(p.name())));
            final Tooltip tip = new Tooltip(p.description);
            propLibelle.setTooltip(tip);
            propEditor.setTooltip(tip);
            
            final HBox hbox = new HBox(propLibelle, propEditor, resetValueBtn);
            hbox.setPadding(new Insets(10));
            hbox.setSpacing(10);
            HBox.setHgrow(propEditor, Priority.ALWAYS);
            hbox.setAlignment(Pos.CENTER_LEFT);
            children.add(hbox);
        }
        root.setCenter(new ScrollPane(propertyList));
    }
    
    private void initializeTop() {
        final Label titleLabel = new Label("Préférences");
        root.setTop(titleLabel);
    }
    
    private synchronized void save() {
        try {
            SirsPreferences.INSTANCE.store(editedProperties);
            editedProperties.clear();
        } catch (IOException ex) {
            final String errorCode  = UUID.randomUUID().toString();
            new Alert(Alert.AlertType.ERROR, "Les préférences ne peuvent être sauvegardées. Code d'erreur : "+errorCode, ButtonType.CLOSE).showAndWait();
            Logging.getLogger(SirsPreferences.class).log(Level.SEVERE, errorCode + " : Preferences cannot be saved.", ex);
        }
    }
    
}
