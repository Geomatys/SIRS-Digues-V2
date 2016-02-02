package fr.sirs.core.component;


import fr.sirs.core.CouchDBTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import java.util.List;
import java.util.Map;
import org.junit.Assert;

public class DocumentChangeEmiterTestCase extends CouchDBTestCase implements DocumentListener {

    private static final String FIRST_COMMENT = "my comment";
    private static final String SECOND_COMMENT = "zozo";

    @Autowired
    private DocumentChangeEmiter documentChangeEmiter;

    @Autowired
    private DigueRepository digueRepository;

    @Test
    public void testListen() throws InterruptedException {
        // Test creation listening
        documentChangeEmiter.addListener(this);
        Digue digue = ElementCreator.createAnonymValidElement(Digue.class);
        digue.setCommentaire(FIRST_COMMENT);
        digueRepository.add(digue);

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
    public void documentDeleted(Map<Class, List<Element>> element) {
        List<Element> deleted = element.get(Digue.class);
        Assert.assertNotNull("No deleted element found !", deleted);
        Assert.assertFalse("No deleted element found !", deleted.isEmpty());
        Assert.assertEquals("Deleted element is not expected one", SECOND_COMMENT, ((Digue)deleted.get(0)).getCommentaire());
        
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
