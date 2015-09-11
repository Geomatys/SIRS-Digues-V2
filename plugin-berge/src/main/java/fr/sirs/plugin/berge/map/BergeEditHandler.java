
package fr.sirs.plugin.berge.map;

import fr.sirs.SIRS;
import fr.sirs.core.model.Berge;
import fr.sirs.map.TronconEditHandler;
import fr.sirs.plugin.berge.PluginBerge;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class BergeEditHandler extends TronconEditHandler {

    public BergeEditHandler(FXMap map) {
        super(map);
    }
    
    protected void init() {
        this.layerName = PluginBerge.LAYER_NAME;
        this.tronconClass = Berge.class;
        try {
            this.style = PluginBerge.createBergeStyle();
        } catch (URISyntaxException | CQLException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        this.typeName = "berge";
        this.maleGender = false;
    }
    
}
