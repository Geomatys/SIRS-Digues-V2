

package fr.sirs.theme;

import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ReseauDeVoirieTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Voie d'accès",             VoieAcces.class,            
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Ouvrage de franchissement",OuvrageFranchissement.class,
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Ouverture batardable",     OuvertureBatardable.class,  
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Voie sur digue",           VoieDigue.class,            
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Ouvrage voirie",           OuvrageVoirie.class,        
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    
    public ReseauDeVoirieTheme() {
        super("Réseaux de voirie", GROUP1,GROUP2,GROUP3,GROUP4,GROUP5);
    }
    
}
