
package fr.sirs.plugin.lit.map;

import fr.sirs.core.model.Lit;
import fr.sirs.core.model.TronconLit;
import fr.sirs.map.ConvertGeomToTronconHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class ConvertGeomToLitHandler extends ConvertGeomToTronconHandler {
    
    public ConvertGeomToLitHandler(FXMap map) {
        super(map);
    }
    
    protected void init() {
        this.typeClass = TronconLit.class;
        this.typeName = "tronçon de lit";
        this.maleGender = true;
        this.parentClass = Lit.class;
        this.showRive = false;
        this.parentLabel = "au lit";
    }
}
