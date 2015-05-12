

package fr.sirs.theme;

import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.LaisseCrue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MesureEvenementTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Laisse de crue",               LaisseCrue.class,
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;}, 
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Montée des eaux (hydrogramme)", MonteeEaux.class,
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Ligne d'eau",                  LigneEau.class,  
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
    
    public MesureEvenementTheme() {
        super("Mesures d'événements hydrauliques", GROUP1,GROUP2,GROUP3);
    }
    
}
