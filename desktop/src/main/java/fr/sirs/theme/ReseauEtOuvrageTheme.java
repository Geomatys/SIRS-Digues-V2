

package fr.sirs.theme;

import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ReseauEtOuvrageTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Station de pompage",               StationPompage.class,               
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Réseau hydraulique fermé",         ReseauHydrauliqueFerme.class,       
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Ouvrage hydraulique associé",            OuvrageHydrauliqueAssocie.class,    
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Réseau de télécom ou d'énergie",   ReseauTelecomEnergie.class,         
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Ouvrage de télécom ou d'énergie",    OuvrageTelecomEnergie.class,        
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP6 = new ThemeGroup("Réseau hydraulique à ciel ouvert",       ReseauHydrauliqueCielOuvert.class,        
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP7 = new ThemeGroup("Ouvrage particulier",              OuvrageParticulier.class,           
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP8 = new ThemeGroup("Échelle limnimetrique",              EchelleLimnimetrique.class,           
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    
    public ReseauEtOuvrageTheme() {
        super("Réseaux et ouvrages", GROUP1,GROUP2,GROUP3,GROUP4,GROUP5,GROUP6,GROUP7, GROUP8);
    }
    
}
