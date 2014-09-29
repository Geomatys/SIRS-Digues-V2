/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.symadrem.sirs.core.CouchDBTestCase;
import fr.symadrem.sirs.core.component.TronconRepository;
import fr.symadrem.sirs.core.model.Troncon;

/**
 *
 * @author samuel
 */
public class TronconRepositoryTest extends CouchDBTestCase {


    @Autowired
    private TronconRepository tronconRepository;
    
    /**
     * Test of getAll method, of class DigueRepository.
     */
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        for (Troncon digue : tronconRepository.getAll()) {
            System.out.println(digue.getStuctures());
        }
        
        
      
    }

}
