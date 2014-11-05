

package fr.sym.theme;

import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.OuvrageRevanche;
import fr.symadrem.sirs.core.model.TalusDigue;
import fr.symadrem.sirs.core.model.TalusRisberme;
import fr.symadrem.sirs.core.model.SommetRisberme;
import fr.symadrem.sirs.core.model.PiedDigue;
import fr.symadrem.sirs.core.model.Fondation;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class StructuresTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Crête", Crete.class,               
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof Crete),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Ouvrage de revanche", OuvrageRevanche.class,       
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof OuvrageRevanche),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Talus de digue", TalusDigue.class,    
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof TalusDigue),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Sommet Risberme", SommetRisberme.class,         
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof SommetRisberme),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Talus Risberme", TalusRisberme.class,         
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof TalusRisberme),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP6 = new ThemeGroup("Pied de digue", PiedDigue.class,         
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof PiedDigue),
            (TronconDigue t, Object c) -> t.structures.remove(c));    
    private static final ThemeGroup GROUP7 = new ThemeGroup("Fondation", Fondation.class,        
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof Fondation),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    
    
    public StructuresTheme() {
        super("Structure", GROUP1,GROUP2,GROUP3,GROUP4,GROUP5,GROUP6,GROUP7);
    }
    
}
