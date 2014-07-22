
package fr.sym;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.geotoolkit.map.MapItem;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class Plugin {
    
    protected final SimpleStringProperty loadingMessage = new SimpleStringProperty("");
    
    
    /**
     * Get the map layers to display in the main application frame.
     * 
     * @return MapItem or null 
     */
    public MapItem getMapItem(){
        return null;
    }
    
    /**
     * Get a property which text is updated in the plugin initialize phase.
     * @return SimpleStringProperty
     */
    public ReadOnlyStringProperty getLoadingMessage(){
        return loadingMessage;
    }
    
    /**
     * Load the plugin.
     */
    public void load() throws Exception {
        
    }
       
}
