
package fr.sirs.plugin.lit.map;

import fr.sirs.core.model.Lit;
import fr.sirs.map.PointCalculatorHandler;
import fr.sirs.plugin.lit.PluginLit;

/**
 *
 * @author guilhem
 */
public class LitPointCalculatorHandler extends PointCalculatorHandler {
    
    @Override
    protected void init() {
        this.layerName = PluginLit.LAYER_NAME;
        this.typeClass = Lit.class;
    }
}
