package fr.sirs;

import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Desordre;
import fr.sirs.util.DesignationIncrementer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.sis.test.DependsOnMethod;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DesignationIncrementerTest extends CouchDBTestCase {

    @Autowired
    DesignationIncrementer operator;

    /**
     * A simple test with an empty database, to verify our component can make
     * proper auto-increment without any existing data.
     */
    @Test
    public void testEmptyDb() throws Exception {
        for (int i = 1 ; i < 1000 ; i++) {
            final Task<Integer> t = operator.getNextDesignation(Desordre.class);
            Platform.runLater(t);
            Assert.assertEquals("Designation increment has failed !", i, t.get().intValue());
        }
    }

    /**
     * Check that increment operator will start to count after the upper
     * designation found in database. To test its robustness, we'll check that
     * it ignores / not crash on non numeric designation, even with special
     * characters.
     */
    @DependsOnMethod("testEmptyDb")
    @Test
    public void testFromDatabase() throws Exception {

        final int maxDesignation = 69;

        // Create test set
        final AbstractSIRSRepository<Crete> repo = session.getRepositoryForClass(Crete.class);
        final Crete firstNumeric = repo.create();
        firstNumeric.setDesignation(String.valueOf(maxDesignation));
        final Crete secondNumeric = repo.create();
        secondNumeric.setDesignation(String.valueOf(7));
        final Crete thirdNumeric = repo.create();
        thirdNumeric.setDesignation(String.valueOf(19));
        final Crete nonNumeric = repo.create();
        nonNumeric.setDesignation("I'm not a number");
        final Crete weirdo = repo.create();
        weirdo.setDesignation(" #\"\"  @ 1à& \n é SELECT * FROM \"TronconDigue\"  )çè &) ! §");

        repo.executeBulk(firstNumeric, secondNumeric, thirdNumeric, nonNumeric, weirdo);

        for (int i = maxDesignation + 1 ; i < maxDesignation + 10 ; i++) {
            final Task<Integer> t = operator.getNextDesignation(Crete.class);
            Platform.runLater(t);
            Assert.assertEquals("Designation increment has failed !", i, t.get().intValue());
        }
    }
}
