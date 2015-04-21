package fr.sirs.core.component;

import java.util.Optional;
import java.util.UUID;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsDBInfo;

@Component
public class SirsDBInfoRepository {

    
    private CouchDbConnector db;

    @Autowired
    protected SirsDBInfoRepository(CouchDbConnector db) {
        this.db = db;
    }

    public Optional<SirsDBInfo> init() {
        try {
            SirsDBInfo sirsDBInfo = db.get(SirsDBInfo.class, "$sirs");
            SirsCore.setEpsgCode(sirsDBInfo.getEpsgCode());
            return Optional.of(sirsDBInfo);
        } catch (DocumentNotFoundException e) {
            return Optional.empty();
        }
    }

    public SirsDBInfo create(String epsgCode) {
        SirsDBInfo sirsDBInfo = new SirsDBInfo();
        sirsDBInfo.setVersion(SirsCore.getVersion());
        sirsDBInfo.setEpsgCode(epsgCode);
        sirsDBInfo.setUuid(UUID.randomUUID().toString());
        db.create("$sirs", sirsDBInfo);
        SirsCore.setEpsgCode(epsgCode);
        return sirsDBInfo;
    }

}
