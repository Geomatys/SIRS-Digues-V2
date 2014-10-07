/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import fr.symadrem.sirs.core.CouchDBTestCase;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
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
        
        for(int i=0;i<100;i++){
            TronconDigue troncon = new TronconDigue();
            troncon.setCommentaire("Traoncon1");
            troncon.setDigueId(digue.getId());
            troncon.setGeometry(createPoint());
            troncon.setDate_maj(LocalDateTime.now());
            
            tronconRepository.add(troncon);
        }
        
//        {
//            TronconDigue troncon = new TronconDigue();
//            troncon.setGeometry(createPoint(100, 100));
//            troncon.setDate_maj(LocalDateTime.now());
//            
//            Fondation ecluse = new Fondation();
//            ecluse.setCommentaire("Fondation");
//            
//            List<Structure> stuctures = new ArrayList<>();
//            stuctures.add(ecluse);
//            troncon.setStuctures(stuctures);
//            
//            troncon.setCommentaire("Traoncon2");
//            troncon.setDigueId(digue.getId());
//
//            tronconRepository.add(troncon);
//        }
        
        
        
        

        
    }

    private Point createPoint(double i, double j) {
        // TODO Auto-generated method stub
        Point pt = new GeometryFactory().createPoint(new Coordinate(i, j));
        return pt;
    }

    private Point createPoint() {
        //random coord in france in 2154
        return createPoint(
                Math.random()*900000 - 100000, 
                Math.random()*1000000 + 6100000);
    }

}
