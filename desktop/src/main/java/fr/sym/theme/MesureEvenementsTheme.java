

package fr.sym.theme;

import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;
import fr.symadrem.sirs.core.model.LigneEau;
import fr.symadrem.sirs.core.model.MonteeEaux;
import fr.symadrem.sirs.core.model.LaisseCrue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MesureEvenementsTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Laisse de crue",               LaisseCrue.class,(TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof LaisseCrue));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Montée des eaux(hydrogramme)", MonteeEaux.class,(TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof MonteeEaux));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Ligne d'eau",                  LigneEau.class,  (TronconDigue t) -> t.stuctures.filtered((Structure t1) -> t1 instanceof LigneEau));
    
    public MesureEvenementsTheme() {
        super("Mesures d'événements", GROUP1,GROUP2,GROUP3);
    }
    
}
