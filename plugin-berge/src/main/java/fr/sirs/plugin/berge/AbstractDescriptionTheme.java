
package fr.sirs.plugin.berge;

import fr.sirs.core.model.Berge;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractDescriptionTheme extends AbstractPluginsButtonTheme {
    
    public AbstractDescriptionTheme(String name, String description) {
        super(name, description, null);
    }
    
    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();
        final HBox hbox = new HBox();
        final Label label = new Label("Berge");
        label.setStyle("label-text");
        label.setPadding(new Insets(0, 20, 0, 10));
        
        final ComboBox<Berge> bergeBox = new ComboBox<>();
        hbox.getChildren().add(label);
        hbox.getChildren().add(bergeBox);
        
        hbox.setStyle("buttonbar");
        
        borderPane.getChildren().add(hbox);
        return borderPane;
    }
}
