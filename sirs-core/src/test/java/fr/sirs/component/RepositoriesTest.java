package fr.sirs.component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.CouchDBTestCase;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;

import java.time.LocalDate;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test-context.xml")
public class RepositoriesTest extends CouchDBTestCase {

    @Autowired
    DigueRepository digueRepository;

    @Autowired
    TronconDigueRepository tronconRepository;

    public void test() {
        System.out.println(connector.getAllDocIds());
    }

    public void removeDigues() {
        digueRepository.executeBulkDelete(digueRepository.getAllStreaming());
    }

    public void removeTronconsDigue() {
        tronconRepository.executeBulkDelete(tronconRepository.getAllStreaming());
    }

    public void insertDigues() {
        final int nbDigues = 10;
        for (int i = 0; i < nbDigues; i++) {
            final Digue digue = ElementCreator.createAnonymValidElement(Digue.class);
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
            digue.setDateMaj(LocalDate.now());
            digueRepository.add(digue);
        }
    }

    public void insertTronconsDigue() {
        final int nbTroncons = 30;
        for (int i = 0; i < nbTroncons; i++) {
            final TronconDigue tron = ElementCreator.createAnonymValidElement(TronconDigue.class);
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
            tron.setDate_debut(LocalDate.now());
            tron.setDate_fin(LocalDate.now());
            tron.setDateMaj(LocalDate.now());

            tron.setGeometry(createPoint());
            tronconRepository.add(tron);

            Fondation ecluse = ElementCreator.createAnonymValidElement(Fondation.class);
            ecluse.setCommentaire("Fondation");
            ecluse.setLinearId(tron.getId());
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
        final List<Digue> digues = digueRepository.getAll();
        final Iterable<TronconDigue> troncons = tronconRepository.getAllStreaming();
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
    @Ignore
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
    @Test
    @Ignore
    public void testGetAll() {
        System.out.println("getAll");
        final List<Digue> expResult = new ArrayList<>();
        final Iterable<Digue> result = digueRepository.getAllStreaming();
        for (Digue digue : result) {
            System.out.println(digue);
        }

        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to
        // fail.
        final Digue digue = ElementCreator.createAnonymValidElement(Digue.class);

        digue.setLibelle("une digue");

        List<String> set = new ArrayList<>();
        {
            TronconDigue troncon = ElementCreator.createAnonymValidElement(TronconDigue.class);
            troncon.setCommentaire("Traoncon1");
            tronconRepository.add(troncon);
            set.add(troncon.getId());
        }
        {
            TronconDigue troncon = ElementCreator.createAnonymValidElement(TronconDigue.class);

            Fondation ecluse = ElementCreator.createAnonymValidElement(Fondation.class);
            ecluse.setCommentaire("Fondation");

            List<Objet> stuctures = new ArrayList<>();
            stuctures.add(ecluse);
//            troncon.setStructures(stuctures);

            troncon.setCommentaire("Traoncon2");

            tronconRepository.add(troncon);

            set.add(troncon.getId());

        }
        digueRepository.add(digue);
    }
}
