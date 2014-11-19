
package fr.sirs.maj;

import java.net.URL;

/**
 *
 */
public class PluginInstaller {
    
    /**
     * Plugin correspondant au desktop et au launcher.
     */
    public static final String PLUGIN_CORE = "core";
    
    public static PluginList listLocalPlugins(){
        final PluginList list = new PluginList();
        
        return list;
    }
    
    public static PluginList listDistantPlugins(URL serverUrl){
        final PluginList list = new PluginList();
        
        return list;
    }
        
    public static void install(URL serverUrl, PluginInfo pluginInfo){
        
    }
    
}
