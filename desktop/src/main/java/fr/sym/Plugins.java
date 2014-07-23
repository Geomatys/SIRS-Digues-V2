

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
    private static final List<Theme> THEMES;

    static {
        final Iterator<Plugin> ite = ServiceRegistry.lookupProviders(Plugin.class);

        final List<Plugin> candidates = new ArrayList<>();
        while(ite.hasNext()){
            candidates.add(ite.next());
        }
        LIST = UnmodifiableArrayList.wrap(candidates.toArray(new Plugin[candidates.size()]));
        
        THEMES = new ArrayList<>();
        for(Plugin plugin : candidates){
            THEMES.addAll(plugin.getThemes());
        }
        
    }
    
    public static Plugin[] getPlugins(){
        return LIST.toArray(new Plugin[0]);
    }
    
    public static Theme[] getThemes(){
        return THEMES.toArray(new Theme[0]);
    }
    
}
