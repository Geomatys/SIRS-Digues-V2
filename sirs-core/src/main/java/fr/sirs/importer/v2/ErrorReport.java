package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;

/**
 * A container to keep a log about an error happened while importing data.
 *
 * @author Alexis Manin (Geomatys)
 */
public class ErrorReport {

    Exception error;

    Row sourceData;
    String sourceTableName;
    String sourceColumnName;

    Object target;
    String targetFieldName;

    String customErrorMsg;

    CorruptionLevel corruptionLevel;

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
}
