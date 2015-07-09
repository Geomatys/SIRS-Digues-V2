package fr.sirs.core;

import fr.sirs.core.authentication.SIRSAuthenticator;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.SirsDBInfoRepository;
import fr.sirs.util.SystemProxySelector;
import java.io.IOException;
import java.net.Authenticator;
import java.net.ProxySelector;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;


import org.ektorp.CouchDbConnector;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/test/test-context.xml")
public abstract class CouchDBTestCase {

    private static String DB_NAME;

    @Autowired
    protected CouchDbConnector connector;

    @Autowired
    private SirsDBInfoRepository sirsDBInfoRepository;

    @BeforeClass
    public static void initWire() {
        ProxySelector.setDefault(new SystemProxySelector());
        Authenticator.setDefault(new SIRSAuthenticator());
    }

    @Before
    public void initTestDatabase() {
        connector.createDatabaseIfNotExists();
        Optional<SirsDBInfo> init = sirsDBInfoRepository.get();
        if (!init.isPresent()) {
            sirsDBInfoRepository.setSRID("EPSG:2154");
        }
    }

    @Autowired
    void setDatabaseName(final CouchDbConnector dbConnector) {
        DB_NAME = dbConnector.getDatabaseName();
    }

    @AfterClass
    public static void clearDatabase() throws IOException {
        if (DB_NAME != null)
            new DatabaseRegistry().dropDatabase(DB_NAME);
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
