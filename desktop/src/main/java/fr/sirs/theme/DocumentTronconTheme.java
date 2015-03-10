

package fr.sirs.theme;

import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DocumentTronconTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Document-tronçons", DocumentTroncon.class, 
            (TronconDigue t) -> t.documentTroncon,
            (TronconDigue t, Object c) -> t.documentTroncon.remove(c));
    
    public DocumentTronconTheme() {
        super("Document-tronçons", GROUP1);
    }
    
}
