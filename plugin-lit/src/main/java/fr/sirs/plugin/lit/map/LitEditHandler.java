
package fr.sirs.plugin.lit.map;

import fr.sirs.SIRS;
import fr.sirs.core.model.Lit;
import fr.sirs.core.model.TronconLit;
import fr.sirs.map.TronconEditHandler;
import fr.sirs.plugin.lit.PluginLit;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class LitEditHandler extends TronconEditHandler {

    public LitEditHandler(FXMap map) {
        super(map);
    }
    
    @Override
    protected void init() {
        this.layerName = PluginLit.LAYER_NAME;
        this.tronconClass = TronconLit.class;
        try {
            this.style = PluginLit.createLitStyle();
        } catch (URISyntaxException | CQLException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        this.typeName = "tronçon de lit";
        this.maleGender = true;
        this.parentClass = Lit.class;
        this.showRive = false;
        this.parentLabel = "au lit";
    }
    
}
