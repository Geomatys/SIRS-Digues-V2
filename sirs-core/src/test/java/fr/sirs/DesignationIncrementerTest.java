/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs;

import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.util.DesignationIncrementer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Phaser;
import javafx.concurrent.Task;
import org.apache.sis.test.DependsOnMethod;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DesignationIncrementerTest extends CouchDBTestCase {

    @Autowired
    DesignationIncrementer operator;

    @Autowired(required = false)
    DocumentChangeEmiter docChangeEmiter;

    /**
     * A simple test with an empty database, to verify our component can make
     * proper auto-increment without any existing data.
     */
    @Test
    public void testEmptyDb() throws Exception {
        for (int i = 1 ; i < 1000 ; i++) {
            final Task<Integer> t = operator.nextDesignation(Desordre.class);
            t.run();
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
            final Task<Integer> t = operator.nextDesignation(Crete.class);
            t.run();
            Assert.assertEquals("Designation increment has failed !", i, t.get().intValue());
        }
    }

    @DependsOnMethod("testFromDatabase")
    @Test
    public void testEventUpdate() throws Exception {
        Assume.assumeNotNull(docChangeEmiter);

        Task<Integer> nextDesignation = operator.nextDesignation(Crete.class);
        nextDesignation.run();
        int higher = nextDesignation.get();

        /*
         * A simple lock. The aim is to ensure document change events are finished
         * bedore proceeding with the test.
         */
        final Phaser trigger = new Phaser(2);
        final DocumentListener listener = new DocumentListener() {
            @Override
            public void documentCreated(Map<Class, List<Element>> added) {
                trigger.arrive();
            }

            @Override
            public void documentChanged(Map<Class, List<Element>> changed) {
                trigger.arrive();
            }

            @Override
            public void documentDeleted(Set<String> deleted) {
                // nothing
            }
        };
        docChangeEmiter.addListener(listener);

        final AbstractSIRSRepository<Crete> repo = session.getRepositoryForClass(Crete.class);
        final Crete crete = repo.create();
        crete.setDesignation(String.valueOf(higher - 1));
        repo.executeBulk(crete);

        trigger.arriveAndAwaitAdvance();

        nextDesignation = operator.nextDesignation(Crete.class);
        nextDesignation.run();
        Assert.assertEquals("Increment has changed unexpectedly", higher + 1, nextDesignation.get().intValue());

        final int highest  = higher + 100;
        crete.setDesignation(String.valueOf(highest));
        repo.executeBulk(crete);

        trigger.arriveAndAwaitAdvance();

        nextDesignation = operator.nextDesignation(Crete.class);
        nextDesignation.run();
        Assert.assertEquals("Increment has not been updated according to newly added element.", highest + 1, nextDesignation.get().intValue());
    }
}
