
package fr.sym;

import fr.sym.digue.Injector;
import fr.sym.theme.Theme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.geotoolkit.map.MapItem;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class Plugin {
    
    protected final SimpleStringProperty loadingMessage = new SimpleStringProperty("");
    
    protected final List<Theme> themes = new ArrayList<>();
        
    public Session getSession(){
        return Injector.getBean(Session.class);
    }
    
    /**
     * Get the map layers to display in the main application frame.
     * 
     * @return MapItem list, never null
     */
    public List<MapItem> getMapItems(){
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Get a property which text is updated in the plugin initialize phase.
     * @return SimpleStringProperty
     */
    public ReadOnlyStringProperty getLoadingMessage(){
        return loadingMessage;
    }
    
    /**
     * Get declared themes, themes are attached to sections.
     * @return 
     */
    public List<Theme> getThemes(){
        return themes;
    }
    
    /**
     * Load the plugin.
     */
    public void load() throws Exception {
        
    }
       
}
