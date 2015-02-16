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
        
        this.setTitle("Préférences");
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
        
        final GridPane propertyPane = new GridPane();
        propertyPane.getColumnConstraints().add(new ColumnConstraints(10, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, true));
        propertyPane.getColumnConstraints().add(new ColumnConstraints(10, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, true));
        propertyPane.getColumnConstraints().add(new ColumnConstraints(10, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, true));
//        final VBox propertyList = new VBox();
//        ObservableList<Node> children = propertyList.getChildren();
//        propertyList.setSpacing(10);
        int row = 0;
        for (final SirsPreferences.PROPERTIES p : SirsPreferences.PROPERTIES.values()) {
            final Label propLibelle = new Label(p.title);
            propertyPane.add(propLibelle, 0, row);
            
            final TextInputControl propEditor;
            if (p.propertyEditor == null) {
                propEditor = new TextField();
                propEditor.setPrefWidth(USE_COMPUTED_SIZE);
            } else {
                propEditor = p.propertyEditor;
                propEditor.setMinWidth(16);
                propEditor.setMaxWidth(500);
            }
            if(SirsPreferences.INSTANCE.getPropertySafe(p.name())!=null){
                propEditor.textProperty().setValue(SirsPreferences.INSTANCE.getPropertySafe(p.name()));
            }
            else{
                propEditor.textProperty().setValue(p.getDefaultValue());
            }
            propEditor.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                if (!newValue.equals(SirsPreferences.INSTANCE.getPropertySafe(p.name()))) {
                    editedProperties.put(p, newValue);
                }
            });
            propertyPane.add(propEditor, 1, row);
            
            final Button resetValueBtn = new Button();
            resetValueBtn.setMaxHeight(propEditor.getHeight());
            resetValueBtn.setGraphic(new ImageView(SIRS.ICON_UNDO_BLACK));
            resetValueBtn.setOnAction((ActionEvent e)->propEditor.textProperty().setValue(SirsPreferences.INSTANCE.getPropertySafe(p.name())));
            propertyPane.add(resetValueBtn, 2, row);
            
            final Tooltip tip = new Tooltip(p.description);
            propLibelle.setTooltip(tip);
            propEditor.setTooltip(tip);
            
//            final HBox hbox = new HBox(propLibelle, propEditor, resetValueBtn);
//            hbox.setPadding(new Insets(10));
//            hbox.setSpacing(10);
//            HBox.setHgrow(propEditor, Priority.ALWAYS);
//            hbox.setAlignment(Pos.CENTER_LEFT);
//            children.add(hbox);
            row++;
        }
        root.setCenter(new ScrollPane(propertyPane));
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
            final String errorCode  = UUID.randomUUID().toString();
            new Alert(Alert.AlertType.ERROR, "Les préférences ne peuvent être sauvegardées. Code d'erreur : "+errorCode, ButtonType.CLOSE).showAndWait();
            Logging.getLogger(SirsPreferences.class).log(Level.SEVERE, errorCode + " : Preferences cannot be saved.", ex);
        }
    }
    
}
