package fr.symadrem.sirs.core.component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.Options;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.stereotype.Component;

import fr.symadrem.sirs.core.SirsDBInfo;

@Component
public class DatabaseRegistry {

	public List<String> listSirsDatabase(URL base) {

		HttpClient client = new StdHttpClient.Builder().url(base).build();
		CouchDbInstance dbInstance = new StdCouchDbInstance(client);

		List<String> res = new ArrayList<>();

		for (String db : dbInstance.getAllDatabases()) {
			if(db.startsWith("_"))
				continue;
			CouchDbConnector connector = dbInstance.createConnector(db, false);
			try {
				SirsDBInfo sirs = connector.get(SirsDBInfo.class, "$sirs");
				System.out.println(sirs.getVersion());
				res.add(db);
			} catch (Exception e) {
			}

		}
		return res;
	}

}
