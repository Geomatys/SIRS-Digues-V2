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
package fr.sirs.core.component;

import com.vividsolutions.jts.geom.Geometry;
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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.sis.geometry.GeneralEnvelope;
import org.geotoolkit.geometry.GeometricUtilities;
import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;

@Component
public class SirsDBInfoRepository {

    private final CouchDbConnector db;

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
        return set(null, null, toSet, null);
    }

    public SirsDBInfo updateModuleDescriptions(final Collection<ModuleDescription> toSet) {
        if (toSet == null || toSet.isEmpty()) {
            return set(null, null, null, null);
        } else {
            final HashMap<String, ModuleDescription> map = new HashMap<>(toSet.size());
            for (final ModuleDescription desc : toSet) {
                map.put(desc.getName(), desc);
            }
            return set(null, null, map, null);
        }
    }

    public SirsDBInfo set(String epsgCode, String remoteDatabase) {
        return set(epsgCode, remoteDatabase, null, null);
    }

    private SirsDBInfo set(String epsgCode, String remoteDatabase, final Map<String, ModuleDescription> moduleDescriptions, final String envelope) {
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
                throw new IllegalArgumentException("Database SRID cannot be modified after creation !");
            }
            info.setEpsgCode(epsgCode);
            // Initialize WKT
            try {
                info.setCrsWkt(SirsCore.getWkt1Common(epsgCode));
            } catch (FactoryException ex) {
                SirsCore.LOGGER.log(Level.WARNING, "No WKT can be created for given code : ".concat(epsgCode), ex);
            }
            try {
                // Initialize Proj4
                SirsCore.getProj4(epsgCode).ifPresent(value -> info.setProj4(value));
            } catch (IOException ex) {
                SirsCore.LOGGER.log(Level.WARNING, "No Proj4 representation can be found for code ".concat(epsgCode), ex);
            }
        }

        if (remoteDatabase != null && !remoteDatabase.isEmpty()) {
            info.setRemoteDatabase(remoteDatabase);
        }

        if (moduleDescriptions != null && !moduleDescriptions.isEmpty()) {
            info.addModuleDescriptions(moduleDescriptions);
        }

        if (envelope != null && !envelope.isEmpty()) {
            info.setEnvelope(envelope);
        }

        if (optInfo.isPresent()) {
            db.update(info);
        } else {
            db.create(INFO_DOCUMENT_ID, info);
        }

        return info;
    }

    public void setEnvelope(Envelope bounds) {
        if (bounds != null) {
            final GeneralEnvelope env = new GeneralEnvelope(bounds);
            if (!env.isEmpty()) {
                final Geometry jtsGeom = GeometricUtilities.toJTSGeometry(bounds, GeometricUtilities.WrapResolution.NONE);
                set(null, null, null, jtsGeom.toText());
            }
        }
    }
}
