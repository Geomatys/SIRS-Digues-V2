/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import fr.symadrem.sirs.core.CouchDBTestCase;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author samuel
 */
@Ignore
public class DigueRepositoryTest extends CouchDBTestCase {
    
    @Autowired
    private DigueRepository instance;
  
    /**
     * Test of getAll method, of class DigueRepository.
     */
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        List<Digue> expResult = new ArrayList<>();
        List<Digue> result = instance.getAll();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
        
        Digue digue = new Digue();
      
        digue.setDigueId("id8");
        digue.setCommune("Montferrier");
        digue.setLongueur(18);
        
        instance.add(digue);
    }
    
}
