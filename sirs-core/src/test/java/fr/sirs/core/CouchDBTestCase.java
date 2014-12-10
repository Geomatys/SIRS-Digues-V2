package fr.sirs.core;

import fr.sirs.core.SirsCore;
import fr.sirs.core.component.SirsDBInfoRepository;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.PostConstruct;

import org.ektorp.CouchDbConnector;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test/test-context.xml")
public abstract class CouchDBTestCase {

    @Autowired
    protected CouchDbConnector connector;

    @Autowired
    private SirsDBInfoRepository sirsDBInfoRepository;

    @PostConstruct
    public void init() {
        sirsDBInfoRepository.init().orElse(
                sirsDBInfoRepository.create("1.0.1", "EPSG:2154"));
    }

    public void test() {
        System.out.println(connector.getAllDocIds());
    }

    protected void dumpAll(List<?> list) {
        if (list != null) {
            for (Object object : list) {
                System.out.println(object);
            }
        }
    }

    protected void log(String message) {
        SirsCore.LOGGER.log(Level.INFO, message);
    }

}
