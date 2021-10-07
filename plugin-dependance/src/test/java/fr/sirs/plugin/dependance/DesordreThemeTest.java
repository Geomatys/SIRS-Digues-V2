/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.plugin.dependance;

import fr.sirs.core.model.DesordreDependance;
import fr.sirs.theme.AbstractTheme;
import org.junit.Test;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class DesordreThemeTest {

    @Test
    public void generateThemeManagerTest() {
        AbstractTheme.ThemeManager tm = DesordreTheme.generateThemeManager("tabTitle1", DesordreDependance.class);

        // TODO complete by testing extractor and deletor, but need to implement first launching CouchDB for test
    }
}
