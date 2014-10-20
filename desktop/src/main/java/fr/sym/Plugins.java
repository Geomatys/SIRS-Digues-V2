

package fr.sym;

import fr.sym.theme.Theme;
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
    private static List<Theme> THEMES;

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
    
    public static synchronized Theme[] getThemes(){
        if(THEMES==null){
            THEMES = new ArrayList<>();
            for(Plugin plugin : getPlugins()){
                THEMES.addAll(plugin.getThemes());
            }
        }
        return THEMES.toArray(new Theme[0]);
    }
    
}
