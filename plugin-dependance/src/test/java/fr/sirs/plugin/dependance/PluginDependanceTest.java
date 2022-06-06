/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.plugin.dependance;

import java.io.IOException;
import java.util.Collections;
import org.junit.Test;
import org.junit.Assert;
import fr.sirs.core.CouchDBTestCase;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class PluginDependanceTest extends CouchDBTestCase {

    @Test
    public void integrityTest() throws IOException {
        PluginDependance plugin = new PluginDependance();

        Assert.assertEquals(plugin.getTitle(), "Module dépendance et AH");
        Assert.assertEquals(plugin.getImage(), null);
        Assert.assertEquals(plugin.getLoadingMessage().get(), "module dépendance et AH");
        Assert.assertEquals(plugin.getMapActions(null), Collections.EMPTY_LIST);
        Assert.assertNotNull(plugin.getModelImage().get());
        Assert.assertEquals(plugin.getThemes().size(), 2);

        //Must initialize database first
        // TODO once a database as been created for the tests, uncomment the two lines below
//        Assert.assertEquals(plugin.getMapItems().size(), 1);
//        Assert.assertEquals(plugin.getMapItems().get(0).items().size(), 11);
    }
}
