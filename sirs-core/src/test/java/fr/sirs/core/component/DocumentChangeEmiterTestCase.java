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
package fr.sirs.core.component;


import fr.sirs.core.CouchDBTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;

public class DocumentChangeEmiterTestCase extends CouchDBTestCase implements DocumentListener {

    private static final String FIRST_COMMENT = "my comment";
    private static final String SECOND_COMMENT = "zozo";

    @Autowired
    private DocumentChangeEmiter documentChangeEmiter;

    @Autowired
    private DigueRepository digueRepository;

    private String digueId;

    @Test
    public void testListen() throws InterruptedException {
        // Test creation listening
        documentChangeEmiter.addListener(this);
        Digue digue = ElementCreator.createAnonymValidElement(Digue.class);
        digue.setCommentaire(FIRST_COMMENT);
        digueRepository.add(digue);
        digueId = digue.getId();

        synchronized (this) {
            wait(1000);
        }

        // test document update
        digue.setCommentaire(SECOND_COMMENT);
        digueRepository.update(digue);

        synchronized (this) {
            wait(1000);
        }

        // test document remove
        digueRepository.remove(digue);

        synchronized (this) {
            wait(1000);
        }
    }

    @Override
    public void documentDeleted(Set<String> deleted) {
        Assert.assertNotNull("No deleted element found !", deleted);
        Assert.assertFalse("No deleted element found !", deleted.isEmpty());
        Assert.assertTrue("Deleted element is not expected one", deleted.contains(digueId));
        
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void documentChanged(Map<Class, List<Element>> element) {
        List<Element> change = element.get(Digue.class);
        Assert.assertNotNull("No updated element found !", change);
        Assert.assertFalse("No updated element found !", change.isEmpty());
        Assert.assertEquals("Updated element is not expected one", SECOND_COMMENT, ((Digue)change.get(0)).getCommentaire());

        synchronized (this) {
            notify();
        }
    }

    @Override
    public void documentCreated(Map<Class, List<Element>> element) {
        List<Element> created = element.get(Digue.class);
        Assert.assertNotNull("No created element found !", created);
        Assert.assertFalse("No created element found !", created.isEmpty());
        Assert.assertEquals("Created element is not expected one", FIRST_COMMENT, ((Digue)created.get(0)).getCommentaire());

        synchronized (this) {
            notify();
        }
    }
}
