/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.plugin.dependance;

import fr.sirs.PluginInfo;
import java.io.IOException;
import java.util.Collections;
import org.junit.Test;
import org.junit.Assert;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class PluginDependanceTest {

    @Test
    public void integrityTest() throws IOException {
        PluginDependance plugin = new PluginDependance();

        Assert.assertEquals(plugin.getTitle(), "Module dépendance");
        Assert.assertEquals(plugin.getImage(), null);
        Assert.assertEquals(plugin.getLoadingMessage().get(), "module dépendance");
        Assert.assertEquals(plugin.getMapActions(null), Collections.EMPTY_LIST);
        Assert.assertNotNull(plugin.getModelImage().get());
        Assert.assertEquals(plugin.getThemes().size(), 2);

        //Must initialize database first
//        Assert.assertEquals(plugin.getMapItems().size(), 1);
//        Assert.assertEquals(plugin.getMapItems().get(0).items().size(), 10);
    }

    @Test
    public void configurationTest() {
        final PluginDependance plugin = new PluginDependance();
        final PluginInfo configuration = plugin.getConfiguration();

        Assert.assertEquals("Plugin dépendance", configuration.getTitle());
        Assert.assertEquals("plugin-dependance", configuration.getName());
        Assert.assertEquals(9, configuration.getVersionMinor());
        Assert.assertEquals(1, configuration.getVersionMajor());
        Assert.assertEquals(-1, configuration.getAppVersionMax());
        Assert.assertEquals(25, configuration.getAppVersionMin());
        Assert.assertEquals("Gestion de dépendances", configuration.getDescription());
        Assert.assertEquals("http://sirs-digues.info/wp-content/updates/plugin-dependance-1.9-plugin-package.zip", configuration.getDownloadURL());
    }
}
