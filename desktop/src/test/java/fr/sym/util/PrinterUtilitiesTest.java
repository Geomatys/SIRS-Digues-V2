package fr.sym.util;

import static fr.sym.util.PrinterUtilities.print;
import fr.symadrem.sirs.model.Digue;
import fr.symadrem.sirs.model.TronconGestionDigue;
import java.lang.reflect.Method;
import java.util.Calendar;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrinterUtilitiesTest {
    
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
    public void testPrintDigue() throws Exception {
        System.out.println("Test print Digue.");
        Digue digue = new Digue();
        digue.setIdDigue(0);
        digue.setLibelleDigue("Grande Digue");
        digue.setCommentaireDigue("Cette digue est en mauvais état et présente "
                 + "des signes évidents de vétusté et de délabrement avancé. Des"
                 + "travaux urgents s'imposent faute de quoi d'importants riques"
                 + "de rupture sont à prévoir.");
        digue.setDateDerniereMaj(Calendar.getInstance());
        
        print(digue);    
    }
    
    /**
     * Test of print method for TronconGestionDigue, of class PrinterUtilities.
     * @throws java.lang.Exception
     */
    @Test
    public void testPrintTronconGestionDigue() throws Exception {
        System.out.println("Test print TronconGestionDigue.");
        TronconGestionDigue tronconGestionDigue = new TronconGestionDigue();
        tronconGestionDigue.setIdDigue(Long.valueOf(0));
        tronconGestionDigue.setCommentaireTroncon("Ceci est un tronçon de la digue.");
        tronconGestionDigue.setDateDebutValGestionnaireD(Calendar.getInstance());
        tronconGestionDigue.setDateDebutValTroncon(Calendar.getInstance());
        tronconGestionDigue.setDateDerniereMaj(Calendar.getInstance());
        tronconGestionDigue.setDateFinValGestionnaireD(Calendar.getInstance());
        tronconGestionDigue.setDateFinValTroncon(Calendar.getInstance());
        tronconGestionDigue.setIdOrgGestionnaire(null);
        tronconGestionDigue.setIdSystemeRepDefaut(null);
        tronconGestionDigue.setIdTronconGestion(null);
        tronconGestionDigue.setIdTypeRive(0);
        tronconGestionDigue.setLibelleTronconGestion("Tronçon principal de la Grande Digue");
        tronconGestionDigue.setNomTronconGestion("Tronçon du moulin");
         
        print(tronconGestionDigue); 
    }

    /**
     * Test of isGetter method, of class PrinterUtilities.
     */
    @Test
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
    @Test
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
