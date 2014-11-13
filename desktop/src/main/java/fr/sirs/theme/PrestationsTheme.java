

package fr.sirs.theme;

import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PrestationsTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Prestations", Prestation.class, 
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof Prestation),
            (TronconDigue t, Object c) -> t.structures.remove(c));
        
    public PrestationsTheme() {
        super("Prestations", GROUP1);
    }
        
}
