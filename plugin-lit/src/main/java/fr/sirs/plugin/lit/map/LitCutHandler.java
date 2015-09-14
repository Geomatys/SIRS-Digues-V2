
package fr.sirs.plugin.lit.map;

import fr.sirs.map.TronconCutHandler;
import fr.sirs.plugin.lit.PluginLit;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class LitCutHandler extends TronconCutHandler {
    
    public LitCutHandler(FXMap map) {
        super(map);
    }
    
    @Override
    protected void init() {
        this.layerName = PluginLit.LAYER_NAME;
        this.typeName = "lit";
        this.maleGender = false;
    }
}
