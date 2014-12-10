
package fr.sirs.util;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXFreeTab extends Tab{

    public FXFreeTab() {
        this("");
    }
    
    public FXFreeTab(String text) {
        super(text);
        
        final MenuItem item = new MenuItem("Détacher");
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                final TabPane tabs = FXFreeTab.this.tabPaneProperty().get();
                tabs.getTabs().remove(FXFreeTab.this);
                
                final Stage stage = new Stage();
                stage.setTitle(getText());
                
                final Node content = getContent();
                setContent(null);
                
                final BorderPane pane = new BorderPane(content);                
                final Scene scene = new Scene(pane);
                stage.setScene(scene);
                stage.setOnHidden(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        setContent(content);
                    }
                });
                
                stage.show();
            }
        });
        
        final ContextMenu menu = new ContextMenu(item);
        setContextMenu(menu);
    }
    
    
    
}
