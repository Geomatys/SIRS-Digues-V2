
package fr.sirs;

import fr.sirs.theme.Theme;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.ServiceRegistry;
import org.apache.sis.internal.util.UnmodifiableArrayList;

/**
 * Classe utilitaire de chargement des plugins.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class Plugins {
    
    private static final List<Plugin> LIST;
    private static List<Theme> THEMES;

    static {
        //creation de la liste des plugins disponibles.
        final Iterator<Plugin> ite = ServiceRegistry.lookupProviders(Plugin.class);
        final List<Plugin> candidates = new ArrayList<>();
        while(ite.hasNext()){
            candidates.add(ite.next());
        }
        LIST = UnmodifiableArrayList.wrap(candidates.toArray(new Plugin[candidates.size()]));
    }
    
    /**
     * Récupérer la liste des plugins.
     * 
     * @return Tableau de plugin, jamais nulle.
     */
    public static Plugin[] getPlugins(){
        return LIST.toArray(new Plugin[0]);
    }
    
    /**
     * Récupérer la liste des thèmes.
     * 
     * @return Tableau de thème, jamais nulle.
     */
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
