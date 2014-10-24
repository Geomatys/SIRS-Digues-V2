

package fr.sym.theme;

import fr.symadrem.sirs.core.model.Prestation;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PrestationsTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Prestations", Prestation.class, 
            (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof Prestation),
            (TronconDigue t, Object c) -> t.stuctures.remove(c));
        
    public PrestationsTheme() {
        super("Prestations", GROUP1);
    }
        
}
