
package fr.sirs.plugin.lit.map;

import fr.sirs.map.BorneEditHandler;
import fr.sirs.plugin.lit.PluginLit;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class LitBorneEditHandler extends BorneEditHandler {
    
    public LitBorneEditHandler(FXMap map) {
        super(map);
    }
    
    @Override
    protected void init() {
        this.layerName = PluginLit.LAYER_NAME;
        this.typeName = "berge";
    }
}
