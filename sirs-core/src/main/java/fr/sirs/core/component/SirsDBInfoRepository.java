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
    private SirsDBInfo info;

    @Autowired
    protected SirsDBInfoRepository(CouchDbConnector db) {
        this.db = db;
    }

    public Optional<SirsDBInfo> get() {
        try {
            info = db.get(SirsDBInfo.class, "$sirs");
            return Optional.of(info);
        } catch (DocumentNotFoundException e) {
            return Optional.empty();
        }
    }

    public SirsDBInfo setRemoteDatabase(String remoteDatabase) {
        return set(null, remoteDatabase);
    }
    
    public SirsDBInfo setSRID(String epsgCode) {
        return set(epsgCode, null);
    }
    
    public SirsDBInfo set(String epsgCode, String remoteDatabase) {
        Optional<SirsDBInfo> optInfo = get();
        if (!optInfo.isPresent()) {
            info = new SirsDBInfo();
            info.setVersion(SirsCore.getVersion());
            info.setUuid(UUID.randomUUID().toString());
        }
        
        if (epsgCode != null && !epsgCode.isEmpty()) {
            if (info.getEpsgCode() != null && !info.getEpsgCode().equals(epsgCode)) {
                // TODO : If no remote is present, we could setup a reprojection process instead of an exception ?
                throw new IllegalStateException("Database SRID cannot be modified after creation !");
            }
            info.setEpsgCode(epsgCode);
        }
        
        if (remoteDatabase != null && !remoteDatabase.isEmpty()) {
            info.setRemoteDatabase(remoteDatabase);
        }
        
        if (optInfo.isPresent()) {
            db.update(info);
        } else {
            db.create("$sirs", info);
        }
        
        return info;
    }

}
