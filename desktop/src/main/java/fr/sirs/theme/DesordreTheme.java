

package fr.sirs.theme;

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DesordreTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Voie d'accès", Desordre.class, 
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof Desordre),
            (TronconDigue t, Object c) -> t.structures.remove(c));
        
    public DesordreTheme() {
        super("Désordres", GROUP1);
    }
    
}
