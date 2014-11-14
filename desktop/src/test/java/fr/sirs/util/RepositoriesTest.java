/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;

import java.time.Instant;
import java.time.LocalDateTime;

import org.ektorp.CouchDbConnector;
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
public class RepositoriesTest {

    @Autowired
    @Qualifier("sirsChouchDB")
    private CouchDbConnector connector;

    public void test() {
        System.out.println(connector.getAllDocIds());
    }
    
    public void removeDigues() {
        final DigueRepository digueRepository = new DigueRepository(connector);
        List<Digue> digues = digueRepository.getAll();
        for(Digue digue : digues) digueRepository.remove(digue);
    }
    
    public void removeTronconsDigue() {
        final TronconDigueRepository tronconRepository = new TronconDigueRepository(connector);
        final List<TronconDigue> troncons = tronconRepository.getAll();
        troncons.stream().forEach((troncon) -> {
            tronconRepository.remove(troncon);
        });
    }
    
    public void insertDigues() {
        final DigueRepository digueRepository = new DigueRepository(connector);
        final int nbDigues = 10;
        for (int i = 0; i < nbDigues; i++) {
            final Digue digue = new Digue();
            digue.setLibelle("La digue " + i);
            digue.setCommentaire("<html><body><u>Digue "+ i + " :</u> Lorem ipsum dolor sit amet, consectetur "
                    + "adipiscing elit. <b>Sed non risus.</b> Suspendisse <i>lectus</i> "
                    + "tortor, <span style=\"color: red;\">dignissim sit amet</span>, adipiscing nec, ultricies "
                    + "sed, dolor. Cras elementum ultrices diam. Maecenas "
                    + "ligula massa, varius a, semper congue, euismod non, "
                    + "mi. Proin porttitor, orci nec nonummy molestie, enim "
                    + "<ul><li>coco</li><li>jojo</li></ul>"
                    + "<ol><li>coco</li><li>jojo</li></ol>"
                    + "est eleifend mi, non fermentum diam nisl sit amet "
                    + "erat. Duis semper. Duis arcu massa, scelerisque "
                    + "vitae, consequat in, pretium a, enim. Pellentesque "
                    + "congue. Ut in risus volutpat libero pharetra tempor. "
                    + "Cras vestibulum bibendum augue. Praesent egestas leo "
                    + "in pede. Praesent blandit odio eu enim. Pellentesque "
                    + "sed dui ut augue blandit sodales. Vestibulum ante "
                    + "ipsum primis in faucibus orci luctus et ultrices "
                    + "posuere cubilia Curae; Aliquam nibh. Mauris ac mauris "
                    + "sed pede pellentesque fermentum. Maecenas adipiscing "
                    + "ante non diam sodales hendrerit.</body></html>");
            digue.setDateMaj(LocalDateTime.now());
            digueRepository.add(digue);
        }
    }

    public void insertTronconsDigue() {
        
        final TronconDigueRepository tronconRepository = new TronconDigueRepository(connector);
        final int nbTroncons = 30;
        for (int i = 0; i < nbTroncons; i++) {
            final TronconDigue tron = new TronconDigue();
            tron.setLibelle("Le tronçon " + i);
            tron.setCommentaire("<html><body><b>Tronçon " + i + " :</b> Lorem ipsum dolor sit amet, consectetur "
                    + "adipiscing elit. Sed non risus. Suspendisse lectus "
                    + "tortor, dignissim sit amet, adipiscing nec, ultricies "
                    + "sed, dolor. Cras elementum ultrices diam. Maecenas "
                    + "ligula massa, varius a, semper congue, euismod non, "
                    + "mi. Proin porttitor, orci nec nonummy molestie, enim "
                    + "est eleifend mi, non fermentum diam nisl sit amet "
                    + "erat. Duis semper. Duis arcu massa, scelerisque "
                    + "vitae, consequat in, pretium a, enim. Pellentesque "
                    + "congue. Ut in risus volutpat libero pharetra tempor. "
                    + "Cras vestibulum bibendum augue. Praesent egestas leo "
                    + "in pede. Praesent blandit odio eu enim. Pellentesque "
                    + "sed dui ut augue blandit sodales. Vestibulum ante "
                    + "ipsum primis in faucibus orci luctus et ultrices "
                    + "posuere cubilia Curae; Aliquam nibh. Mauris ac mauris "
                    + "sed pede pellentesque fermentum. Maecenas adipiscing "
                    + "ante non diam sodales hendrerit.</body></html>");
            tron.setDate_debut(LocalDateTime.now());
            tron.setDate_fin(LocalDateTime.now());
            tron.setDateMaj(LocalDateTime.now());
            
            tron.setGeometry(createPoint());
            
            Fondation ecluse = new Fondation();
            ecluse.setCommentaire("Fondation");
            
            List<Objet> stuctures = new ArrayList<>();
            stuctures.add(ecluse);
            tron.setStructures(stuctures);
            tronconRepository.add(tron);
        }
        
        
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
    
    public void linkTronconsToDigues(){
        final DigueRepository digueRepository = new DigueRepository(connector);
        final TronconDigueRepository tronconRepository = new TronconDigueRepository(connector);
        final List<Digue> digues = digueRepository.getAll();
        final List<TronconDigue> troncons = tronconRepository.getAll();
        final int nbDigues = digues.size();
        
        int i=0;
        for(final TronconDigue troncon : troncons){
            final Digue digue = digues.get(i);
            troncon.setDigueId(digue.getId());
            i++;
            if(i==nbDigues) i=0;
            digueRepository.update(digue);
            tronconRepository.update(troncon);
        }
    }
    
    @Test
    public void testBase(){
        this.removeDigues();
        this.removeTronconsDigue();
        this.insertDigues();
        this.insertTronconsDigue();
        this.linkTronconsToDigues();
    }

    /**
     * Test of getAll method, of class DigueRepository.
     */
    @Ignore
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        final DigueRepository digueRepository = new DigueRepository(connector);
        final TronconDigueRepository tronconRepository = new TronconDigueRepository(connector);
        final List<Digue> expResult = new ArrayList<>();
        final List<Digue> result = digueRepository.getAll();
        for (Digue digue : result) {
            System.out.println(digue);
        }

        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to
        // fail.
        final Digue digue = new Digue();

        digue.setLibelle("une digue");

        List<String> set = new ArrayList<>();
        {
            TronconDigue troncon = new TronconDigue();
            troncon.setCommentaire("Traoncon1");
            tronconRepository.add(troncon);
            set.add(troncon.getId());
        }
        {
            TronconDigue troncon = new TronconDigue();

            Fondation ecluse = new Fondation();
            ecluse.setCommentaire("Fondation");

            List<Objet> stuctures = new ArrayList<>();
            stuctures.add(ecluse);
            troncon.setStructures(stuctures);

            troncon.setCommentaire("Traoncon2");

            tronconRepository.add(troncon);

            set.add(troncon.getId());

        }
        digueRepository.add(digue);
    }
}
