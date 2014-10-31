package fr.sym;

import fr.sym.digue.DiguesTab;
import fr.sym.map.FXMapTab;
import fr.sym.theme.Theme;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class MainFrame extends BorderPane {

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

    private FXMapTab mapTab;
    private DiguesTab diguesTab;

    public MainFrame() {
        Symadrem.loadFXML(this);
        
        // Load themes
        final Theme[] themes = Plugins.getThemes();
        for(final Theme theme : themes){
            if(theme.getType().equals(Theme.Type.STANDARD)){
                uiThemes.getItems().add(toMenuItem(theme));
            }
            else{
                uiPlugins.getItems().add(toMenuItem(theme));
            }
        }
    }
    
    public TabPane getUiTabs() {
        return uiTabs;
    }

    public synchronized FXMapTab getMapTab() {
        if(mapTab==null){
            mapTab = new FXMapTab(uiTabs);
        }
        return mapTab;
    }
    
    public synchronized DiguesTab getDiguesTab() {
        if(diguesTab==null){
            diguesTab = new DiguesTab(uiTabs);
        }
        return diguesTab;
    }
        
    public synchronized void addTab(Tab tab){
        uiTabs.getTabs().add(tab);
        final int index = uiTabs.getTabs().indexOf(tab);
        uiTabs.getSelectionModel().clearAndSelect(index);
    }
    
    private MenuItem toMenuItem(final Theme theme){
        
        final List<Theme> subs = theme.getSubThemes();
        final MenuItem item;
        
        if(subs.isEmpty()){
            item = new MenuItem(theme.getName());
        }
        else{
            item = new Menu(theme.getName());
            for(final Theme sub : subs){
                ((Menu) item).getItems().add(toMenuItem(sub));
            }
        }
        
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Tab tab = new Tab();
                tab.setText(theme.getName());
                tab.setContent(theme.createPane());
                addTab(tab);
            }
        });
        
        return item;
    }
  
    @FXML
    void openMap(ActionEvent event) {
        getMapTab().show();
    }

    @FXML
    void openDigueTab(ActionEvent event) {
        getDiguesTab().show();
    }

    @FXML
    void openPref(ActionEvent event) {
        System.out.println("TODO");
    }

    @FXML
    void exit(ActionEvent event) {
        System.exit(0);
    }

}
