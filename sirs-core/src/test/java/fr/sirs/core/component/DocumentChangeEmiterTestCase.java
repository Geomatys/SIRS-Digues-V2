package fr.sirs.core.component;

import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.core.component.DocumentListener;

import javax.annotation.PostConstruct;

import org.ektorp.CouchDbConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.component.DigueRepository;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test/test-context.xml")
public class DocumentChangeEmiterTestCase implements DocumentListener {

    @Autowired
    private CouchDbConnector couchDbConnector;

    private DocumentChangeEmiter documentChangeEmiter;

    @Autowired
    private DigueRepository digueRepository;

    private Thread thread;

    @PostConstruct
    public void init() {
        documentChangeEmiter = new DocumentChangeEmiter(couchDbConnector);
        documentChangeEmiter.addListener(this);
        thread = documentChangeEmiter.start();
    }

    @Test
    public void testListen() throws InterruptedException {

        Digue digue = new Digue();
        digue.setCommentaire("my comment");
        digueRepository.add(digue);
        digue.setCommentaire("zozo");
        digueRepository.update(digue);
        
        Thread.sleep(2000);
        
        digueRepository.remove(digue);
       // thread.join();
    }

    @Override
    public void documentDeleted(Map<Class, List<Element>> element) {
        info("documentDeleted(" + element + ")");
    }

    @Override
    public void documentChanged(Map<Class, List<Element>> element) {
        info("documentChanged(" + element + ")");
    }

    @Override
    public void documentCreated(Map<Class, List<Element>> changed) {
        info("documentCreated(" + changed + ")");
    }

    private void info(String message) {
        SirsCore.LOGGER.info(message);
        System.out.println(message);
    }
}
