package fr.sirs.core.component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.component.DatabaseRegistry;

//@Ignore
public class DatabaseRegistryTestCase extends CouchDBTestCase {

    @Test
    public void databaseList() throws MalformedURLException {
        List<String> listDatabase = DatabaseRegistry.listSirsDatabase(new URL(
                "http://geouser:geopw@127.0.0.1:5984/"));
        for (String database : listDatabase) {
            log(database);
        }
    }

    @Test
    public void initDatabase() throws MalformedURLException {
        DatabaseRegistry.newLocalDB("sirs_dup");
    }

    String src = "http://geouser:geopw@127.0.0.1:5984/sirs-test/";
    String dst = "http://geouser:geopw@127.0.0.1:5984/sirs-test-dup/";

    @Test
    public void dropDatabase() throws MalformedURLException {
        DatabaseRegistry.dropLocalDB("sirs_dup");
    }

    @Test
    public void initDatabaseFromRemote() throws MalformedURLException {
        DatabaseRegistry.newLocalDBFromRemote(src, dst, true);
    }

    @Test
    public void getReplicationsTask() {
        DatabaseRegistry.getReplicationTasks().forEach(
                t -> System.out.println(t));
    }

    @Test
    public void getReplicationTasksByTarget() {
        DatabaseRegistry.getReplicationTasksBySourceOrTarget(dst).forEach(
                t -> System.out.println(t.get("replication_id").asText()));

    }

    @Test
    public void cancelReplication() throws MalformedURLException {
        DatabaseRegistry.cancelReplication(connector);
    }

    @Test
    public void startReplication() throws MalformedURLException {
        DatabaseRegistry.startReplication(connector, dst, true);
    }

    @Test
    public void replicate() throws MalformedURLException {
        DatabaseRegistry.startReplication(connector, dst, false);
    }

    @Test
    public void replicateWihoutRemote() throws MalformedURLException {
        DatabaseRegistry.startReplication(connector, false);
    }

}
