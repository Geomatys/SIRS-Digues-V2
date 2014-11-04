
package fr.sym.digue;

import fr.sym.map.FXMapTab;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DiguesTab extends Tab {
    
    private final TabPane tabs;
    private final DiguesController ctrl;

    public DiguesTab(TabPane tabs) {
        this.tabs = tabs;
        this.ctrl = new DiguesController();
        setText("Digues");
        setContent(ctrl);
    }    
    
    public synchronized void show(){
        int index = 0;
        if(!tabs.getTabs().contains(this)){
            //on place l'onglet toujours apres la carte si possible
            for(Tab t : tabs.getTabs()){
                if(t instanceof FXMapTab){
                    index = tabs.getTabs().indexOf(t)+1;
                }
            }
            tabs.getTabs().add(index,this);
        }
        tabs.getSelectionModel().clearAndSelect(tabs.getTabs().indexOf(this));
    }
    
    public synchronized DiguesController getDiguesController(){return this.ctrl;}
}
