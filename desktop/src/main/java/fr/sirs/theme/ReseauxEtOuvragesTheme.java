

package fr.sirs.theme;

import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauHydroCielOuvert;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.Structure;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ReseauxEtOuvragesTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Station de pompage",               StationPompage.class,               
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof StationPompage),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Réseau hydraulique fermé",         ReseauHydrauliqueFerme.class,       
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof ReseauHydrauliqueFerme),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Ouvrage hydro associé",            OuvrageHydrauliqueAssocie.class,    
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof OuvrageHydrauliqueAssocie),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Réseau de télécom ou d'énergie",   ReseauTelecomEnergie.class,         
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof ReseauTelecomEnergie),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Ouvrage de télécom ou énergie",    OuvrageTelecomEnergie.class,        
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof OuvrageTelecomEnergie),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP6 = new ThemeGroup("Réseau hydro à ciel ouvert",       ReseauHydroCielOuvert.class,        
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof ReseauHydroCielOuvert),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP7 = new ThemeGroup("Ouvrage particulier",              OuvrageParticulier.class,           
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof OuvrageParticulier),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    
    public ReseauxEtOuvragesTheme() {
        super("Réseaux et ouvrages", GROUP1,GROUP2,GROUP3,GROUP4,GROUP5,GROUP6,GROUP7);
    }
    
}
