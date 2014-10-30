package fr.symadrem.sirs.component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import fr.symadrem.sirs.core.CouchDBTestCase;
import fr.symadrem.sirs.core.DatabaseRegistry;

@Ignore
public class DatabaseRegistryTestCase extends CouchDBTestCase {

	@Test
	public void databaseList() throws MalformedURLException {
		List<String> listDatabase = DatabaseRegistry.listSirsDatabase(new URL(
				"http://127.0.0.1:5984/"));
		for (String database : listDatabase) {
			log(database);
		}
	}

	@Test
	public void initDatabase() throws MalformedURLException {
		DatabaseRegistry.newLocalDB("symadrem_dup");
	}

	String src = "http://geouser:geopw@127.0.0.1:5984/symadrem/";
	String dst = "http://geouser:geopw@127.0.0.1:5984/symadrem_dup/";

	@Test
	public void dropDatabase() throws MalformedURLException {
		DatabaseRegistry.dropLocalDB("symadrem_dup");
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
		DatabaseRegistry.getReplicationTasksByTarget(dst).forEach(
				t -> System.out.println(t.get("replication_id").asText()));

	}

	@Test
	public void cancelReplication() throws MalformedURLException {
		String src = "http://geouser:geopw@127.0.0.1:5984/symadrem/";
		String dst = "http://geouser:geopw@127.0.0.1:5984/zozo2/";
		DatabaseRegistry.cancelReplication(dst);
	}

}
