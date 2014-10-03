/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.Fondation;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.time.Instant;

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
    @Qualifier("symadremChouchDB")
    private CouchDbConnector connector;

    @Autowired
    private DigueRepository digueRepository;

    @Autowired
    private TronconDigueRepository tronconRepository;

    public void test() {
        System.out.println(connector.getAllDocIds());
    }
    
    public void removeDigues() {
        List<Digue> digues = digueRepository.getAll();
        for(Digue digue : digues) digueRepository.remove(digue);
    }
    
    public void removeTronconsDigue() {
        final List<TronconDigue> troncons = tronconRepository.getAll();
        troncons.stream().forEach((troncon) -> {
            tronconRepository.remove(troncon);
        });
    }
    
    public void insertDigues() {
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
            digue.setTronconsIds(new ArrayList<>());
            digue.setDate_maj(Instant.now());
            digueRepository.add(digue);
        }
    }

    public void insertTronconsDigue() {
        
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
            tron.setDate_debut(Instant.now());
            tron.setDate_fin(Instant.now());
            tron.setDate_maj(Instant.now());
            tronconRepository.add(tron);
        }
    }
    
    public void linkTronconsToDigues(){
        final List<Digue> digues = this.digueRepository.getAll();
        final List<TronconDigue> troncons = this.tronconRepository.getAll();
        final int nbDigues = digues.size();
        
        int i=0;
        for(final TronconDigue troncon : troncons){
            final Digue digue = digues.get(i);
            troncon.setDigue(digue.getId());
            List<String> tronconsIds = digue.getTronconsIds();
            tronconsIds.add(troncon.getId());
            digue.setTronconsIds(tronconsIds);
            i++;
            if(i==nbDigues) i=0;
            this.digueRepository.update(digue);
            this.tronconRepository.update(troncon);
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
        List<Digue> expResult = new ArrayList<>();
        List<Digue> result = digueRepository.getAll();
        for (Digue digue : result) {
            System.out.println(digue);
        }

        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to
        // fail.
        Digue digue = new Digue();

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

            List<Structure> stuctures = new ArrayList<>();
            stuctures.add(ecluse);
            troncon.setStuctures(stuctures);

            troncon.setCommentaire("Traoncon2");

            tronconRepository.add(troncon);

            set.add(troncon.getId());

        }

        digue.setTronconsIds(set);

        digueRepository.add(digue);
    }

}
