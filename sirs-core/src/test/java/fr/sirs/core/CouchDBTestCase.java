package fr.sirs.core;

import fr.sirs.core.authentication.SIRSAuthenticator;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.SirsDBInfoRepository;
import fr.sirs.util.SystemProxySelector;
import java.io.IOException;
import java.net.Authenticator;
import java.net.ProxySelector;
import java.util.HashSet;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import javafx.embed.swing.JFXPanel;
import org.apache.sis.test.TestCase;


import org.ektorp.CouchDbConnector;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

public abstract class CouchDBTestCase extends TestCase {

    /**
     * A list of databases to delete at the end of test class.
     */
    private static final HashSet<String> DBS_TO_DELETE = new HashSet<>();

    /**
     * Default test database name.
     */
    protected static String DB_NAME;

    /**
     * Application context associated with default test database.
     */
    private static ConfigurableApplicationContext APP_CTX;

    /**
     * Local service registry.
     */
    protected static DatabaseRegistry REGISTRY;

    /**
     * Session asociated with default test database.
     */
    @Autowired
    protected SessionCore session;

    @BeforeClass
    public static void initEnvironment() throws IOException {
        // Set http proxy / authentication managers
        ProxySelector.setDefault(new SystemProxySelector());
        Authenticator.setDefault(new SIRSAuthenticator());
        // initialize fx toolkit
        new JFXPanel();
        // Create / connect to sirs database.
        DB_NAME = "sirs-test".concat(UUID.randomUUID().toString());
        DBS_TO_DELETE.add(DB_NAME);
        REGISTRY = new DatabaseRegistry();
        APP_CTX = REGISTRY.connectToSirsDatabase(DB_NAME, true, true, true);
        final SirsDBInfoRepository sirsDBInfoRepository = APP_CTX.getBean(SirsDBInfoRepository.class);
        Optional<SirsDBInfo> init = sirsDBInfoRepository.get();
        if (!init.isPresent()) {
            sirsDBInfoRepository.setSRID("EPSG:2154");
        }
    }

    @Before
    public void initTestDatabase() {
        APP_CTX.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @AfterClass
    public static void clearDatabases() throws IOException {
        for (final String db : DBS_TO_DELETE) {
            try {
                REGISTRY.dropDatabase(db);
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, "A database cannot be deleted after tests !", e);
            }
        }
    }

    /**
     * Mark given database to be deleted after this class tests have been performed.
     * @param dbName Name of the database to delete.
     */
    protected static void deleteAfterClass(final String dbName) {
        DBS_TO_DELETE.add(dbName);
    }

    @Component
    private static class MockSession extends SessionCore {

        @Autowired
        public MockSession(CouchDbConnector couchDbConnector) {
            super(couchDbConnector);
        }
    }

    @Component
    private static class MockInjector extends InjectorCore {}
}
