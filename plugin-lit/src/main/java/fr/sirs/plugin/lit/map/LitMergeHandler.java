
package fr.sirs.plugin.lit.map;

import fr.sirs.map.TronconMergeHandler;
import fr.sirs.plugin.lit.PluginLit;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class LitMergeHandler extends TronconMergeHandler {
    
    public LitMergeHandler(FXMap map) {
        super(map);
    }
    
    protected void init() {
        this.layerName = PluginLit.LAYER_NAME;
        this.typeName = "tronçon de lit";
        this.maleGender = true;
    }
}
