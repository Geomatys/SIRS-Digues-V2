
package fr.sirs.plugin.berge.map;

import fr.sirs.map.BorneEditHandler;
import fr.sirs.map.TronconMergeHandler;
import fr.sirs.plugin.berge.PluginBerge;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class BergeBorneEditHandler extends BorneEditHandler {
    
    public BergeBorneEditHandler(FXMap map) {
        super(map);
    }
    
    protected void init() {
        this.layerName = PluginBerge.LAYER_NAME;
        this.typeName = "berge";
    }
}
