

package fr.sirs.theme;

import fr.sirs.core.model.Structure;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.LaisseCrue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MesureEvenementsTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Laisse de crue",               LaisseCrue.class,
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof LaisseCrue), 
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Montée des eaux(hydrogramme)", MonteeEaux.class,
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof MonteeEaux),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Ligne d'eau",                  LigneEau.class,  
            (TronconDigue t) -> t.structures.filtered((Structure t1) -> t1 instanceof LigneEau),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    
    public MesureEvenementsTheme() {
        super("Mesures d'événements", GROUP1,GROUP2,GROUP3);
    }
    
}
