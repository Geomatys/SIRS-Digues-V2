package fr.symadrem.sirs.component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.symadrem.sirs.core.CouchDBTestCase;
import fr.symadrem.sirs.core.component.DatabaseRegistry;


public class DatabaseRegistryTestCase extends CouchDBTestCase {

	@Autowired
	DatabaseRegistry databaseRegistry;
	
	@Test
	public void databaseList() throws MalformedURLException {
		List<String> listDatabase = databaseRegistry.listSirsDatabase(new URL("http://127.0.0.1:5984/"));
		for (String database : listDatabase) {
			log(database);
		}
	}
	
}
