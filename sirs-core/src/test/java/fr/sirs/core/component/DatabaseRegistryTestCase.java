package fr.sirs.core.component;

import org.junit.Test;

import fr.sirs.core.CouchDBTestCase;
import java.io.IOException;
import org.apache.sis.test.DependsOnMethod;
import org.junit.Ignore;

//@Ignore
public class DatabaseRegistryTestCase extends CouchDBTestCase {

    private static String REPLICATION_SOURCE = "http://geouser:geopw@127.0.0.1:5984/sirs-test/";
    private static String REPLICATION_DEST = "http://geouser:geopw@127.0.0.1:5984/sirs-test-dup/";
    
    @Test
    public void databaseList() throws IOException {
        final DatabaseRegistry registry = new DatabaseRegistry("http://geouser:geopw@127.0.0.1:5984/");
        for (String database : registry.listSirsDatabases()) {
            log(database);
        }
    }

    @Test
    public void initDatabase() throws IOException {
        new DatabaseRegistry().connectToSirsDatabase("sirs_dup", true, true, true);
    }

    @DependsOnMethod("initDatabase")
    @Test
    public void dropDatabase() throws IOException {
        new DatabaseRegistry().dropDatabase("sirs_dup");
    }

    @Test
    public void initDatabaseFromRemote() throws IOException {
        new DatabaseRegistry().synchronizeSirsDatabases(REPLICATION_SOURCE, REPLICATION_DEST, true);
    }

    @Test
    public void getReplicationTasks() throws IOException {
        new DatabaseRegistry().getReplicationTasks().forEach(
                t -> System.out.println(t));
    }

    @Test
    public void getReplicationTasksByTarget() throws IOException {
        new DatabaseRegistry().getReplicationTasksBySourceOrTarget(REPLICATION_DEST).forEach(
                t -> System.out.println(t.getReplicationId()));

    }
}
