
package fr.sirs.plugin.berge.map;

import fr.sirs.map.TronconCutHandler;
import fr.sirs.plugin.berge.PluginBerge;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class BergeCutHandler extends TronconCutHandler {
    
    public BergeCutHandler(FXMap map) {
        super(map);
    }
    
    protected void init() {
        this.layerName = PluginBerge.LAYER_BERGE_NAME;
        this.typeName = "berge";
        this.maleGender = false;
    }
}
