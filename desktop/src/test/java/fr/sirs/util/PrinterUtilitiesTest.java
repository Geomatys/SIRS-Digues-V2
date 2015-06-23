package fr.sirs.util;

import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.TronconDigue;

import java.lang.reflect.Method;
import java.time.LocalDate;

import org.ektorp.CouchDbConnector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test-context.xml")
public class PrinterUtilitiesTest {

    @Autowired
    @Qualifier("sirsCouchDB")
    private CouchDbConnector connector;
    
    public PrinterUtilitiesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of print method for Digue, of class PrinterUtilities.
     * @throws java.lang.Exception
     */
    @Test
    @Ignore
    public void testPrintDigue() throws Exception {
        System.out.println("Test print Digue.");
        final DigueRepository digueRepository = new DigueRepository(connector);
        final Digue digue = digueRepository.getAll().get(0);
//        print(digue, null);    
    }
    
    /**
     * Test of print method for TronconGestionDigue, of class PrinterUtilities.
     * @throws java.lang.Exception
     */
    @Test
    @Ignore
    public void testPrintTronconGestionDigue() throws Exception {
        System.out.println("Test print TronconGestionDigue.");
        final TronconDigueRepository tronconRepository = new TronconDigueRepository(connector);
        final TronconDigue tronconGestionDigue = tronconRepository.getAll().get(0);
//        print(tronconGestionDigue, null); 
    }
    
    /**
     * Test of print method for BorneDigue, of class PrinterUtilities.
     * @throws java.lang.Exception
     */
    @Test 
    @Ignore
    public void testPrintBorneDigue() throws Exception {
        System.out.println("Test print BorneDigue.");
        final BorneDigue borneDigue = ElementCreator.createAnonymValidElement(BorneDigue.class);
        //borneDigue.setIdBorne(1);
        borneDigue.setCommentaire("Cette borne n'est pas une borne fictive.");
        borneDigue.setDate_debut(LocalDate.now());
        borneDigue.setDate_fin(LocalDate.now());
        borneDigue.setFictive(false);
        //borneDigue.setIdTronconGestion(2);
        borneDigue.setLibelle("Borne principale");
        //borneDigue.setXPoint(1.3);
        //borneDigue.setYPoint(1.4);
        //borneDigue.setZPoint(1.5);
        //borneDigue.setXPointOrigine(1.6);
        //borneDigue.setYPointOrigine(1.7);
         
//        print(borneDigue, null); 
    }

    /**
     * Test of isGetter method, of class PrinterUtilities.
     */
    @Test @Ignore
    public void testIsGetter() {
        System.out.println("isGetter");
        Method method = null;
        boolean expResult = false;
        boolean result = PrinterUtilities.isGetter(method);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of isSetter method, of class PrinterUtilities.
     */
    @Test @Ignore
    public void testIsSetter() {
        System.out.println("isSetter");
        Method method = null;
        boolean expResult = false;
        boolean result = PrinterUtilities.isSetter(method);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
