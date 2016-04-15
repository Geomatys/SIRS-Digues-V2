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
package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * A container to keep a log about an error happened while importing data.
 *
 * @author Alexis Manin (Geomatys)
 */
public class ErrorReport implements Serializable {

    public Exception error;

    public Row sourceData;
    public String sourceTableName;
    public String sourceColumnName;

    public Object target;
    public String targetFieldName;

    public String customErrorMsg;

    public CorruptionLevel corruptionLevel;

    public ErrorReport() {}

    public ErrorReport(Exception error, Row sourceData, String sourceTableName) {
        this.error = error;
        this.sourceData = sourceData;
        this.sourceTableName = sourceTableName;
    }

    public ErrorReport(Exception error, Row sourceData, String sourceTableName, String sourceColumnName, Object target, String targetFieldName, String customErrorMsg, CorruptionLevel corruptionLevel) {
        this.error = error;
        this.sourceData = sourceData;
        this.sourceTableName = sourceTableName;
        this.sourceColumnName = sourceColumnName;
        this.target = target;
        this.targetFieldName = targetFieldName;
        this.customErrorMsg = customErrorMsg;
        this.corruptionLevel = corruptionLevel;
    }

    /**
     * Check all attributes which could cause problem at serialisation, and remove
     * them if they're actually problematic.
     */
    void setSerializable() {
        try (
                final ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
                final ObjectOutputStream out = new ObjectOutputStream(tmpStream)) {

            try {
                out.writeObject(sourceData);
            } catch (NotSerializableException e) {
                sourceData = null;
            }

            try {
                out.writeObject(target);
            } catch (NotSerializableException e) {
                target = null;
            }

        } catch (IOException e) {
            SirsCore.LOGGER.log(Level.FINE, "Cannot serialize an error report.", e);
        }
    }
}
