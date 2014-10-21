/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import fr.symadrem.sirs.core.CouchDBTestCase;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.Fondation;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;

import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DigueRepositoryTest extends CouchDBTestCase {

    @Autowired
    private CouchDbConnector couchDbConnector;

    
    DigueRepository digueRepository;
    
    @Before
    public void setUp() {
    	 digueRepository = new DigueRepository(couchDbConnector);
    }
    
    /**
     * Test of getAll method, of class DigueRepository.
     */
    @Test
    public void testStoreDigueAndTroncons() {
        System.out.println("DigueRepositoryTest.testStoreDigueAndTroncons()");
        
        TronconDigueRepository tronconRepository = new TronconDigueRepository(couchDbConnector);
      
        Digue digue = new Digue();

        digue.setLibelle("une digue");

        digue.setDateMaj(LocalDateTime.now());

        digueRepository.add(digue);

        for (int i = 0; i < 1; i++) {
            TronconDigue troncon = new TronconDigue();
            troncon.setCommentaire("Traoncon1");
            troncon.setDigueId(digue.getId());
            troncon.setGeometry(createPoint());
            troncon.setDateMaj(LocalDateTime.now());

            List<Structure> stuctures = new ArrayList<Structure>();
            Fondation e = new Fondation();
            // e.setDocument(troncon);
            stuctures.add(e);
            Crete crete = new Crete();
            crete.setBorne_debut(8);
            crete.setCommentaire("Belle crete");
            stuctures.add(crete);
            
            troncon.setStuctures(stuctures);

            tronconRepository.add(troncon);
        }

        // {
        // TronconDigue troncon = new TronconDigue();
        // troncon.setGeometry(createPoint(100, 100));
        // troncon.setDate_maj(LocalDateTime.now());
        //
        // Fondation ecluse = new Fondation();
        // ecluse.setCommentaire("Fondation");
        //
        // List<Structure> stuctures = new ArrayList<>();
        // stuctures.add(ecluse);
        // troncon.setStuctures(stuctures);
        //
        // troncon.setCommentaire("Traoncon2");
        // troncon.setDigueId(digue.getId());
        //
        // tronconRepository.add(troncon);
        // }
    }

    
    @Test
    public void testGetAll() {
    	  List<Digue> result = digueRepository.getAll();
          for (Digue digue : result) {
              System.out.println(digue);
          }

    }
    
    @Test
    public void failDelete() {

        DigueRepository digueRepository = new DigueRepository(couchDbConnector);
        Digue digue = new Digue();
        digue.setLibelle("toDelete");
        digueRepository.add(digue);

        {
            Digue digue2 = digueRepository.get(digue.getId());
            digue2.setLibelle("StillToDelete");
            digueRepository.update(digue2);
        }

        digue = digueRepository.get(digue.getId());
        digueRepository.remove(digue);

    }
    
    @Test
    public void uuid() {
    	
    }

    private Point createPoint(double i, double j) {
        // TODO Auto-generated method stub
        Point pt = new GeometryFactory().createPoint(new Coordinate(i, j));
        return pt;
    }

    private Point createPoint() {
        // random coord in france in 2154
        return createPoint(Math.random() * 900000 - 100000,
                Math.random() * 1000000 + 6100000);
    }

}
