

package fr.sym.theme;

import fr.symadrem.sirs.core.model.OuvertureBatardable;
import fr.symadrem.sirs.core.model.OuvrageFranchissement;
import fr.symadrem.sirs.core.model.OuvrageVoirie;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;
import fr.symadrem.sirs.core.model.VoieAcces;
import fr.symadrem.sirs.core.model.VoieDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ReseauxDeVoirieTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Voie d'accès",             VoieAcces.class,            
            (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof VoieAcces),
            (TronconDigue t, Object c) -> t.stuctures.remove(c));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Ouvrage de franchissement",OuvrageFranchissement.class,
            (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof OuvrageFranchissement),
            (TronconDigue t, Object c) -> t.stuctures.remove(c));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Ouverture batardable",     OuvertureBatardable.class,  
            (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof OuvertureBatardable),
            (TronconDigue t, Object c) -> t.stuctures.remove(c));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Voie sur digue",           VoieDigue.class,            
            (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof VoieDigue),
            (TronconDigue t, Object c) -> t.stuctures.remove(c));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Ouvrage voirie",           OuvrageVoirie.class,        
            (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof OuvrageVoirie),
            (TronconDigue t, Object c) -> t.stuctures.remove(c));
    
    public ReseauxDeVoirieTheme() {
        super("Réseaux de voirie", GROUP1,GROUP2,GROUP3,GROUP4,GROUP5);
    }
    
}
