

package fr.sirs.theme;

import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Deversoir;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class StructureTheme extends AbstractTronconTheme {

//    private static final ThemeGroup GROUP1 = new ThemeGroup("Crêtes", "Tableau des crêtes", Crete.class,               
//            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof Crete),
//            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP1 = new ThemeGroup("Crêtes", "Tableau des crêtes", Crete.class,               
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Ouvrages de revanche", "Tableau des ouvrages de revanche", OuvrageRevanche.class,       
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Talus de digue", "Tableau des talus de digue", TalusDigue.class,    
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Sommets de risberme", "Tableau des sommets de risberme", SommetRisberme.class,         
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Talus de risberme", "Tableau des talus de risberme", TalusRisberme.class,         
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP6 = new ThemeGroup("Pieds de digue", "Tableau des pieds de digue", PiedDigue.class,         
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP7 = new ThemeGroup("Fondations", "Tableau des fondations", Fondation.class,        
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP8 = new ThemeGroup("Epis", "Tableau des épis", Epi.class,        
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP9 = new ThemeGroup("Déversoirs", "Tableau des déversoirs", Deversoir.class,        
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    
    
    public StructureTheme() {
        super("Structure", GROUP1,GROUP2,GROUP3,GROUP4,GROUP5,GROUP6,GROUP7, GROUP8, GROUP9);
    }
    
}
