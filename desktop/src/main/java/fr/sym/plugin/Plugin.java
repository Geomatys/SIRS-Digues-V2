
package fr.sym.plugin;

import org.geotoolkit.map.MapItem;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface Plugin {
    
    /**
     * Get the map layers to display in the main application frame.
     * 
     * @return MapItem or null 
     */
    MapItem getMapItem();
    
    
    
}
