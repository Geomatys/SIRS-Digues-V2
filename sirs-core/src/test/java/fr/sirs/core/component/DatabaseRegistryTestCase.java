package fr.sirs.core.component;

import java.net.MalformedURLException;

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
        new DatabaseRegistry().connectToDatabase("sirs_dup", true);
    }

    @DependsOnMethod("initDatabase")
    @Test
    public void dropDatabase() throws IOException {
        new DatabaseRegistry().dropDatabase("sirs_dup");
    }

    @Test
    public void initDatabaseFromRemote() throws IOException {
        new DatabaseRegistry().synchronizeDatabases(REPLICATION_SOURCE, REPLICATION_DEST, true);
    }

    @Test
    public void getReplicationTasks() throws IOException {
        new DatabaseRegistry().getReplicationTasks().forEach(
                t -> System.out.println(t));
    }

    @Test
    public void getReplicationTasksByTarget() throws IOException {
        new DatabaseRegistry().getReplicationTasksBySourceOrTarget(REPLICATION_DEST).forEach(
                t -> System.out.println(t.get("replication_id").asText()));

    }

    @DependsOnMethod("startReplication")
    @Test
    public void cancelReplication() throws IOException {
        new DatabaseRegistry().cancelReplication(connector);
    }

    @Test
    public void startReplication() throws IOException {
        new DatabaseRegistry().startReplication(connector, REPLICATION_DEST, true);
        // TODO : check content
    }

    @Test
    public void replicate() throws IOException {
        new DatabaseRegistry().startReplication(connector, REPLICATION_DEST, false);
        // TODO : check content
    }

    @Ignore
    public void replicateWihoutRemote() throws IOException {
        new DatabaseRegistry().startReplication(connector, false);
        // TODO : check content
    }

}
