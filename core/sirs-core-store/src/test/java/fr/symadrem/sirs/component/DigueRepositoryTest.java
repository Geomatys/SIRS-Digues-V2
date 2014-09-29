/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.symadrem.sirs.core.CouchDBTestCase;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.TronconRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.Ecluse;
import fr.symadrem.sirs.core.model.NewEClass6;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.Troncon;

/**
 *
 * @author samuel
 */

public class DigueRepositoryTest extends CouchDBTestCase {

    @Autowired
    private DigueRepository instance;

    @Autowired
    private TronconRepository tronconRepository;
    
    /**
     * Test of getAll method, of class DigueRepository.
     */
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        List<Digue> expResult = new ArrayList<>();
        List<Digue> result = instance.getAll();
        for (Digue digue : result) {
            System.out.println(digue);
        }
        
        
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to
        // fail.

        Digue digue = new Digue();

        digue.setLabel("une digue");

        List<String> set = new ArrayList<>();
        {
            Troncon troncon = new Troncon();
            troncon.setDesignation("Traoncon1");
            tronconRepository.add(troncon);
            set.add(troncon.getId());
        }
        {
            Troncon troncon = new Troncon();
            
            Ecluse ecluse = new Ecluse();
            ecluse.setDesignation("Ecluse");
            
            List<Structure> stuctures = new ArrayList<>();
            stuctures.add(ecluse);
            troncon.setStuctures(stuctures);
            
            troncon.setDesignation("Traoncon2");
            troncon.setNewEReference(new NewEClass6());

            tronconRepository.add(troncon);
            
            set.add(troncon.getId());
            
            
            
            
        }
        
        digue.setTronconsIds(set);
        
        

        instance.add(digue);
    }

}
