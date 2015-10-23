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
