
package fr.sirs;

import fr.sirs.core.plugins.PluginLoader;
import fr.sirs.theme.Theme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.spi.ServiceRegistry;

/**
 * Classe utilitaire de chargement des plugins.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class Plugins {
    
    /**
     * List of installed plugins. To access it, use {@linkplain #getPluginMap() }.
     */
    private static Map<String, Plugin> REGISTERED_PLUGINS;
    private static List<Theme> THEMES;
    
    /**
     * Récupérer la liste des plugins.
     * 
     * @return Tableau de plugin, jamais nul.
     */
    public static Plugin[] getPlugins() {
        return getPluginMap().values().toArray(new Plugin[0]);
    }
    
    public static Map<String, Plugin> getPluginMap() {
        if (REGISTERED_PLUGINS == null) {
            //creation de la liste des plugins disponibles.
            final Iterator<Plugin> ite = ServiceRegistry.lookupProviders(Plugin.class);
            final HashMap<String, Plugin> candidates = new HashMap<>();
            while(ite.hasNext()){
                Plugin next = ite.next();
                candidates.put(next.name, next);
            }
            REGISTERED_PLUGINS = Collections.unmodifiableMap(candidates);
        }

        // If system class loader has not loaded plugins yet, we do not build a
        // definitive list of available plugins.
        final ClassLoader scl = ClassLoader.getSystemClassLoader();
        if (scl instanceof PluginLoader && !((PluginLoader) scl).isLoaded()) {
            final Map<String, Plugin> tmpCopy = REGISTERED_PLUGINS;
            REGISTERED_PLUGINS = null;
            return tmpCopy;
        } else {
            return REGISTERED_PLUGINS;
        }
    }
    
    /**
     * @param pluginName Name of the plugin to retrieve.
     * @return The registered plugin for given name, or null.
     */
    public static Plugin getPlugin(final String pluginName) {
        return getPluginMap().get(pluginName);
    }
    
    /**
     * Récupérer la liste des thèmes.
     * 
     * @return Tableau de thème, jamais nul.
     */
    public static synchronized Theme[] getThemes() {
        if(THEMES==null){
            THEMES = new ArrayList<>();
            for(Plugin plugin : getPlugins()){
                THEMES.addAll(plugin.getThemes());
            }
        }
        return THEMES.toArray(new Theme[0]);
    }

    /**
     * Vide les caches de plugins et de thèmes.
     */
    public static void clearCache() {
        THEMES = null;
        REGISTERED_PLUGINS = null;
    }
}
