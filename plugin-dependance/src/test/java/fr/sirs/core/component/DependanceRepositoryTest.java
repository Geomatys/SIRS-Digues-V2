/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.core.component;

import com.sun.tools.doclint.Entity;
import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.PrestationAmenagementHydraulique;
import java.util.List;
import org.ektorp.DocumentNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author maximegavens
 */
public class DependanceRepositoryTest extends CouchDBTestCase {

    @Autowired
    PrestationAmenagementHydrauliqueRepository prestationRepository;

    @Test
    public void crudAndViewTest() {

        // Create
        final PrestationAmenagementHydraulique prestation = ElementCreator.createAnonymValidElement(PrestationAmenagementHydraulique.class);
        prestation.setLibelle("libellePrestation1");
        prestation.setDesignation("designationPrestation1");
        prestation.setAuthor("maxime");
        prestation.setCoutGlobal(10000);
        prestation.setAmenagementHydrauliqueId("1234567890");
        prestationRepository.add(prestation);

        // Read
        final PrestationAmenagementHydraulique get = prestationRepository.get(prestation.getId());
        Assert.assertEquals(prestation, get);
        Assert.assertEquals(10000, get.getCoutGlobal(), 1E-6);

        // Update
        get.setCoutGlobal(50000);
        prestationRepository.update(get);
        final PrestationAmenagementHydraulique get2 = prestationRepository.get(prestation.getId());
        Assert.assertEquals("libellePrestation1", get2.getLibelle());
        Assert.assertEquals(50000, get2.getCoutGlobal(), 1E-6);

        // View
        final List<PrestationAmenagementHydraulique> oneResult = prestationRepository.getByAmenagementHydrauliqueId("1234567890");
        Assert.assertEquals(1, oneResult.size());
        Assert.assertEquals(prestation, oneResult.get(0));

        // Delete
        prestationRepository.remove(get2);
        try {
            prestationRepository.get(get2.getId());
            Assert.assertTrue(false);
        } catch (DocumentNotFoundException ex) {
            Assert.assertTrue(true);
        }
    }
}
