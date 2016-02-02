package fr.sirs.core.component;

import org.junit.Test;

import fr.sirs.core.CouchDBTestCase;
import java.io.IOException;
import java.util.UUID;
import org.apache.sis.test.DependsOnMethod;
import org.junit.Assert;
import org.junit.BeforeClass;

public class DatabaseRegistryTestCase extends CouchDBTestCase {

    private static String REPLICATION_DEST;

    @BeforeClass
    public static void init() {
        REPLICATION_DEST = "sirs-test-dup".concat(UUID.randomUUID().toString());
        deleteAfterClass(REPLICATION_DEST);
    }

    @Test
    public void databaseList() throws IOException {
        Assert.assertTrue(
                "Database list does not contain target databasse !",
                REGISTRY.listSirsDatabases().contains(DB_NAME));
    }

    @DependsOnMethod("databaseList")
    @Test
    public void initDatabase() throws IOException {
        REGISTRY.connectToSirsDatabase(REPLICATION_DEST, true, true, true);
        Assert.assertTrue(
                "Database list does not contain target databasse !",
                REGISTRY.listSirsDatabases().contains(REPLICATION_DEST));
    }

    @DependsOnMethod("initDatabase")
    @Test
    public void dropDatabase() throws IOException {
        REGISTRY.dropDatabase(REPLICATION_DEST);
        Assert.assertFalse(
                "Database list contains a deleted database !",
                REGISTRY.listSirsDatabases().contains(REPLICATION_DEST));
    }

    @DependsOnMethod("dropDatabase")
    @Test
    public void initDatabaseFromRemote() throws IOException {
        REGISTRY.synchronizeSirsDatabases(DB_NAME, REPLICATION_DEST, true);
    }

    @DependsOnMethod("initDatabaseFromRemote")
    @Test
    public void getReplicationTasks() throws IOException {
        Assert.assertFalse("No replication task found !", REGISTRY.getReplicationTasks().isEmpty());
    }

    @DependsOnMethod("getReplicationTasks")
    @Test
    public void getSynchronizationTask() throws IOException {
        Assert.assertTrue(REGISTRY.getSynchronizationTasks(REPLICATION_DEST).count() > 0);
    }
}
