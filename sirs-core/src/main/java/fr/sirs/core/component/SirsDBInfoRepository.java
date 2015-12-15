package fr.sirs.core.component;

import fr.sirs.core.ModuleDescription;
import java.util.Optional;
import java.util.UUID;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sirs.core.SirsCore;
import static fr.sirs.core.SirsCore.INFO_DOCUMENT_ID;
import fr.sirs.core.SirsDBInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class SirsDBInfoRepository {

    private CouchDbConnector db;

    @Autowired
    protected SirsDBInfoRepository(CouchDbConnector db) {
        this.db = db;
    }

    public Optional<SirsDBInfo> get() {
        try {
            final SirsDBInfo info = db.get(SirsDBInfo.class, INFO_DOCUMENT_ID);
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

    public SirsDBInfo updateModuleDescriptions(final Map<String, ModuleDescription> toSet) {
        return set(null, null, toSet);
    }

    public SirsDBInfo updateModuleDescriptions(final Collection<ModuleDescription> toSet) {
        if (toSet == null || toSet.isEmpty()) {
            return set(null, null, null);
        } else {
            final HashMap<String, ModuleDescription> map = new HashMap<>(toSet.size());
            for (final ModuleDescription desc : toSet) {
                map.put(desc.getName(), desc);
            }
            return set(null, null, map);
        }
    }

    public SirsDBInfo set(String epsgCode, String remoteDatabase) {
        return set(epsgCode, remoteDatabase, null);
    }

    private SirsDBInfo set(String epsgCode, String remoteDatabase, final Map<String, ModuleDescription> moduleDescriptions) {
        SirsDBInfo info;
        Optional<SirsDBInfo> optInfo = get();
        if (optInfo.isPresent()) {
            info = optInfo.get();
        } else {
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

        if (moduleDescriptions != null && !moduleDescriptions.isEmpty()) {
            info.addModuleDescriptions(moduleDescriptions);
        }

        if (optInfo.isPresent()) {
            db.update(info);
        } else {
            db.create(INFO_DOCUMENT_ID, info);
        }

        return info;
    }
}
