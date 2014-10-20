

package fr.sym.theme;

import fr.symadrem.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.symadrem.sirs.core.model.OuvrageParticulier;
import fr.symadrem.sirs.core.model.OuvrageTelecomEnergie;
import fr.symadrem.sirs.core.model.ReseauHydrauliqueFerme;
import fr.symadrem.sirs.core.model.ReseauHydroCielOuvert;
import fr.symadrem.sirs.core.model.ReseauTelecomEnergie;
import fr.symadrem.sirs.core.model.StationPompage;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ReseauxEtOuvragesTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Station de pompage",               StationPompage.class,               (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof StationPompage));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Réseau hydraulique fermé",         ReseauHydrauliqueFerme.class,       (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof ReseauHydrauliqueFerme));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Ouvrage hydro associé",            OuvrageHydrauliqueAssocie.class,    (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof OuvrageHydrauliqueAssocie));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Réseau de télécom ou d'énergie",   ReseauTelecomEnergie.class,         (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof ReseauTelecomEnergie));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Ouvrage de télécom ou énergie",    OuvrageTelecomEnergie.class,        (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof OuvrageTelecomEnergie));
    private static final ThemeGroup GROUP6 = new ThemeGroup("Réseau hydro à ciel ouvert",       ReseauHydroCielOuvert.class,        (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof ReseauHydroCielOuvert));
    private static final ThemeGroup GROUP7 = new ThemeGroup("Ouvrage particulier",              OuvrageParticulier.class,           (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof OuvrageParticulier));
    
    public ReseauxEtOuvragesTheme() {
        super("Réseaux et ouvrages", GROUP1,GROUP2,GROUP3,GROUP4,GROUP5,GROUP6,GROUP7);
    }
    
}
