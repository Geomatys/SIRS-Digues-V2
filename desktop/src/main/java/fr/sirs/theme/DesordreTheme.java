package fr.sirs.theme;

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DesordreTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Désordres", Desordre.class, 
            (TronconDigue t) -> {new UnsupportedOperationException("Implémenter"); return null;},
            (TronconDigue t, Object c) -> new UnsupportedOperationException("Implémenter"));
        
    public DesordreTheme() {
        super("Désordres", GROUP1);
    }
    
}
