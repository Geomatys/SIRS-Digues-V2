package fr.sym;

import fr.sym.digue.DiguesController;
import fr.sym.map.FXMapPane;
import fr.sym.theme.Theme;
import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainFrameController extends Stage {

    @FXML
    private MenuItem uiPref;
    @FXML
    private MenuButton uiThemes;
    @FXML
    private MenuButton uiPlugins;
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
        
        //load themes
        final Theme[] themes = Plugins.getThemes();
        for(Theme theme : themes){
            if(theme.getType().equals(Theme.Type.STANDARD)){
                uiThemes.getItems().add(toMenuItem(theme));
            }else{
                uiPlugins.getItems().add(toMenuItem(theme));
            }
        }
    }
    
    private MenuItem toMenuItem(final Theme theme){
        final List<Theme> subs = theme.getSubThemes();
        final MenuItem item;
        if(subs.isEmpty()){
            item = new MenuItem(theme.getName());
        }else{
            item = new Menu(theme.getName());
            for(Theme sub : subs){
                ((Menu)item).getItems().add(toMenuItem(sub));
            }
        }
        
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Tab tab = new Tab();
                tab.setText(theme.getName());
                tab.setContent(theme.createPane());
                uiTabs.getTabs().add(tab);
                uiTabs.getSelectionModel().clearAndSelect(uiTabs.getTabs().size()-1);
            }
        });
        
        return item;
    }
    

    @FXML
    void openMap(ActionEvent event) {

        final FXMapPane fxmap = new FXMapPane();

        final Tab tab = new Tab();
        tab.setText("Map");
        tab.setContent(fxmap);
        
        uiTabs.getTabs().add(tab);
        uiTabs.getSelectionModel().clearAndSelect(uiTabs.getTabs().indexOf(tab));
    }

    @FXML
    void openDigueTab(ActionEvent event) {
        
        final DiguesController digueController = DiguesController.create();
        
        final Tab tab = new Tab();
        tab.setText("Digues");
        tab.setContent(new BorderPane(digueController.root));
        
        uiTabs.getTabs().add(tab);
        uiTabs.getSelectionModel().clearAndSelect(uiTabs.getTabs().indexOf(tab));
    }

    @FXML
    void openPref(ActionEvent event) {
        System.out.println("TODO");
    }

    @FXML
    void exit(ActionEvent event) {
        System.exit(0);
    }

    public static MainFrameController create() throws IOException {
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource("/fr/sym/mainframe.fxml"));
        final Parent root = loader.load();
        final MainFrameController controller = loader.getController();
        controller.init();
        final Scene scene = new Scene(root);
        controller.setScene(scene);
        return controller;
    }

}
