package fr.sirs.core.component;

import java.util.UUID;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sirs.core.SirsDBInfo;

@Component
public class SirsDBInfoRepository {

	@Autowired
	protected SirsDBInfoRepository(CouchDbConnector db) {
		try {
			SirsDBInfo sirsDBInfo = db.get(SirsDBInfo.class, "$sirs");
		} catch (DocumentNotFoundException e) {
			SirsDBInfo sirsDBInfo = new SirsDBInfo();
			sirsDBInfo.setVersion("1.0.0");
			sirsDBInfo.setUuid(UUID.randomUUID().toString());
			db.create("$sirs", sirsDBInfo);
		}
		
	}

}
