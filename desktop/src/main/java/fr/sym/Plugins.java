

package fr.sym;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.ServiceRegistry;
import org.apache.sis.internal.util.UnmodifiableArrayList;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class Plugins {
    
    private static final List<Plugin> LIST;

    static {
        final Iterator<Plugin> ite = ServiceRegistry.lookupProviders(Plugin.class);

        final List<Plugin> candidates = new ArrayList<>();
        while(ite.hasNext()){
            candidates.add(ite.next());
        }
        LIST = UnmodifiableArrayList.wrap(candidates.toArray(new Plugin[candidates.size()]));
    }
    
    public static Plugin[] getPlugins(){
        return LIST.toArray(new Plugin[0]);
    }
    
}
