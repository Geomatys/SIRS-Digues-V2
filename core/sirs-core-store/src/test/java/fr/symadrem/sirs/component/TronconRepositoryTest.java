/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.symadrem.sirs.core.CouchDBTestCase;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.TronconDigue;
import org.junit.Ignore;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TronconRepositoryTest extends CouchDBTestCase {


    @Autowired
    private TronconDigueRepository tronconRepository;
    
    /**
     * Test of getAll method, of class TronconDigueRepository.
     */
    @Ignore
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        for (TronconDigue troncon : tronconRepository.getAll()) {
            System.out.println(troncon.getNom());
        }
    }
}
