package fr.sirs.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.support.CouchDbDocument;

@SuppressWarnings("serial")
public class SirsDBInfo extends CouchDbDocument {

    private String version;

    private String uuid;

    private String epsgCode;

    private String remoteDatabase;
    
    private String envelope;

    /**
     * No remove method allowed, because even if an application remove a plugin,
     * database should still contains related data.
     */
    private Map<String, ModuleDescription> moduleDescriptions;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getEpsgCode() {
        return epsgCode;
    }

    public void setEpsgCode(String epsgCode) {
        this.epsgCode = epsgCode;
    }

    public String getRemoteDatabase() {
        return remoteDatabase;
    }

    public void setRemoteDatabase(String remoteDatabase) {
        this.remoteDatabase = remoteDatabase;
    }

    public String getEnvelope() {
        return envelope;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }
    
    public Map<String, ModuleDescription> getModuleDescriptions() {
        return moduleDescriptions;
    }

    public void setModuleDescriptions(Map<String, ModuleDescription> moduleDescriptions) {
        this.moduleDescriptions = moduleDescriptions;
    }

    public void addModuleDescriptions(final Collection<ModuleDescription> modules) {
        ArgumentChecks.ensureNonNull("module descriptions to add", modules);
        if (moduleDescriptions == null) {
            moduleDescriptions = new HashMap<>(modules.size());
        }
        for (final ModuleDescription module : modules) {
            moduleDescriptions.put(module.getName(), module);
        }
    }

    public void addModuleDescriptions(final Map<String, ModuleDescription> modules) {
        ArgumentChecks.ensureNonNull("module descriptions to add", modules);
        if (moduleDescriptions == null) {
            moduleDescriptions = new HashMap<>(modules.size());
        }
        moduleDescriptions.putAll(modules);
    }
}
