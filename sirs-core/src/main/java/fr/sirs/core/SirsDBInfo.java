/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.support.CouchDbDocument;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

@SuppressWarnings("serial")
public class SirsDBInfo extends CouchDbDocument {

    /**
     * Application version used for this database creation.
     */
    private String version;

    /**
     * A unique identifier for the database.
     */
    private String uuid;

    /**
     * EPSG code of the {@link CoordinateReferenceSystem} defining dataset geometrys.
     */
    private String epsgCode;

    /**
     * WKT (v1 - common units) representation of the database {@link CoordinateReferenceSystem}.
     */
    private String crsWkt;

    /**
     * Proj4 representation of database {@link CoordinateReferenceSystem}.
     * Needed for mobile application.
     */
    private String proj4;

    /** URL to the database used for continuous synchronization. */
    private String remoteDatabase;

    /** Bounding box of the database dataset. */
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

    public String getCrsWkt() {
        return crsWkt;
    }

    public void setCrsWkt(final String crsWkt) {
        this.crsWkt = crsWkt;
    }

    public String getProj4() {
        return proj4;
    }

    public void setProj4(String proj4) {
        this.proj4 = proj4;
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
