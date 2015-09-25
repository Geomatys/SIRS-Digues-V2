
package fr.sirs.plugin.berge.map;

import fr.sirs.core.model.Berge;
import fr.sirs.map.PointCalculatorHandler;
import fr.sirs.plugin.berge.PluginBerge;

/**
 *
 * @author guilhem
 */
public class BergePointCalculatorHandler extends PointCalculatorHandler {
    
    protected void init() {
        this.layerName = PluginBerge.LAYER_BERGE_NAME;
        this.typeClass = Berge.class;
    }
}
