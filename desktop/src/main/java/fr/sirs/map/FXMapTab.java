
package fr.sirs.map;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXMapTab extends Tab {
    
    private final TabPane tabs;
    private final FXMapPane map;

    public FXMapTab(TabPane tabs) {
        this.tabs = tabs;
        this.map = new FXMapPane();
        setText("Carte");
        setContent(map);
    }    

    public FXMapPane getMap() {
        return map;
    }
    
    public synchronized void show(){
        if(!tabs.getTabs().contains(this)){
            tabs.getTabs().add(0,this);
        }
        final int index = tabs.getTabs().indexOf(this);
        tabs.getSelectionModel().clearAndSelect(index);
    }
    
}
