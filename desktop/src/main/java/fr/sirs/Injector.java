
package fr.sirs;

import fr.sirs.core.InjectorCore;
import org.springframework.stereotype.Component;

/**
 *
 * @author Olivier Nouguier (Géomatys)
 * @author Samuel Andrés (Géomatys)
 */
@Component
public class Injector extends InjectorCore {
    
    public static Session getSession() {
        return getBean(Session.class);
    }
}
