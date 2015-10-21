
package fr.sirs.map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
        
        selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(Boolean.TRUE.equals(newValue)){
                    if(getContent()!=null){
                        getContent().requestFocus();
                    }
                }
            }
        });
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
