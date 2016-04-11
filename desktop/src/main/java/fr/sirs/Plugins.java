
package fr.sirs;

import fr.sirs.core.plugins.PluginLoader;
import fr.sirs.theme.Theme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

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
     * HACK : A conflict looks to appear on some systems. Multiple instances of
     * the same plugin seems to be present in memory, because system class loader
     * is not flagged as loaded even after application loading. We put some safety
     * here to be sure it can never happen. We'll synchronize jar loading with this
     * class to ensure unique instance of plugin data.
     */
    private static boolean ALL_LOADED = false;
    
    /**
     * Récupérer la liste des plugins.
     * 
     * @return Tableau de plugin, jamais nul.
     */
    public static Plugin[] getPlugins() {
        return getPluginMap().values().toArray(new Plugin[0]);
    }
    
    public synchronized static Map<String, Plugin> getPluginMap() {
        if (!ALL_LOADED) {
            final ClassLoader scl = ClassLoader.getSystemClassLoader();
            ALL_LOADED = (!(scl instanceof PluginLoader)) || ((PluginLoader) scl).isLoaded();
            if (REGISTERED_PLUGINS == null) {
                REGISTERED_PLUGINS = new HashMap<>();
            }

            //creation de la liste des plugins disponibles.
            final Iterator<Plugin> ite = ServiceLoader.load(Plugin.class).iterator();
            while (ite.hasNext()) {
                Plugin next = ite.next();
                REGISTERED_PLUGINS.putIfAbsent(next.name, next);
            }
        }

        return Collections.unmodifiableMap(REGISTERED_PLUGINS);
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
