
package fr.sirs;

import fr.sirs.core.InjectorCore;
import org.springframework.stereotype.Component;

@Component
public class Injector extends InjectorCore {
    
    public static Session getSession() {
        return getBean(Session.class);
    }
}
