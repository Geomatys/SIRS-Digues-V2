/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.symadrem.sirs.core.CouchDBTestCase;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.Fondation;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;

/**
 *
 * @author samuel
 */

public class DigueRepositoryTest extends CouchDBTestCase {

    @Autowired
    private DigueRepository instance;

    @Autowired
    private TronconDigueRepository tronconRepository;
    
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

        digue.setLibelle("une digue");

        digue.setDate_maj(LocalDateTime.now());
        
        instance.add(digue);
        
        {
            TronconDigue troncon = new TronconDigue();
            troncon.setCommentaire("Traoncon1");
            troncon.setDigueAssociee(digue.getId());
            tronconRepository.add(troncon);

        }
        {
            TronconDigue troncon = new TronconDigue();
            
            Fondation ecluse = new Fondation();
            ecluse.setCommentaire("Fondation");
            
            List<Structure> stuctures = new ArrayList<>();
            stuctures.add(ecluse);
            troncon.setStuctures(stuctures);
            
            troncon.setCommentaire("Traoncon2");
            troncon.setDigueAssociee(digue.getId());

            tronconRepository.add(troncon);
            

            
            
            
            
        }
        
        
        
        

        
    }

}
