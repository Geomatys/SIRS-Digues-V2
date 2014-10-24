

package fr.sym.theme;

import fr.symadrem.sirs.core.model.Desordre;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DesordreTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Voie d'accès", Desordre.class, 
            (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof Desordre),
            (TronconDigue t, Object c) -> t.stuctures.remove(c));
        
    public DesordreTheme() {
        super("Désordres", GROUP1);
    }
    
}
