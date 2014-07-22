

package fr.sym;

import fr.sym.map.FXMapPane;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainFrameController extends Stage{

    @FXML
    private MenuItem uiPref;
    @FXML
    private TabPane uiTabs;
    @FXML
    private MenuItem uiExit;
    @FXML
    private Button uiMapButton;

    public TabPane getUiTabs() {
        return uiTabs;
    }

    private void init() {
        setTitle("Symadrem");
    }

    @FXML
    void openMap(ActionEvent event) {
        
        final FXMapPane fxmap = new FXMapPane();
        
        final Tab tab = new Tab();
        tab.setText("Map");
        tab.setContent(fxmap);
        
        uiTabs.getTabs().add(tab);
        
    }

    @FXML
    void openPref(ActionEvent event) {
        System.out.println("TODO");
    }

    @FXML
    void exit(ActionEvent event) {
        System.exit(0);
    }

    public static MainFrameController create() throws IOException{
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource("/fr/sym/mainframe.fxml"));
        final Parent root = loader.load();
        final MainFrameController controller = loader.getController();
        controller.init();
        
        final Scene scene = new Scene(root);
        controller.setScene(scene);
        return controller;
    }
    
}
