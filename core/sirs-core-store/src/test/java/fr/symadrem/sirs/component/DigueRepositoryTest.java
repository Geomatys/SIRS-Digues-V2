/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import fr.symadrem.sirs.core.CouchDBTestCase;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
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
//@Ignore
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
        digue.setCommentaire("Un commentaire de digue.");
        digue.setLibelle("Un libellé de digue.");
        try { 
            String target = "Thu Sep 28 20:29:30 JST 2000";
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.FRANCE);
            Date dateMaj =  df.parse(target);
            digue.setDateMaj(dateMaj);
        } catch (ParseException ex) {
            Logger.getLogger(DigueRepositoryTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        instance.add(digue);
    }
    
}
