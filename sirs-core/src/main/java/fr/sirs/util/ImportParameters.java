package fr.sirs.util;

import com.healthmarketscience.jackcess.Database;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ImportParameters {

    /**
     * Input database containing object properties.
     */
    public final Database inputDb;
    /**
     * Input database containing projection and geometric information.
     */
    public final Database inputCartoDb;

    /**
     * Target database.
     */
    public final CouchDbConnector outputDb;

    /**
     * Target database projection.
     */
    public final CoordinateReferenceSystem outputCRS;

    public ImportParameters(Database inputDb, Database inputCartoDb, CouchDbConnector outputDb, CoordinateReferenceSystem outputCRS) {
        ArgumentChecks.ensureNonNull("Input database which contains properties", inputDb);
        ArgumentChecks.ensureNonNull("Input database which contains geometries", inputCartoDb);
        ArgumentChecks.ensureNonNull("Output database connection", outputDb);
        ArgumentChecks.ensureNonNull("Output projection", outputCRS);

        this.inputDb = inputDb;
        this.inputCartoDb = inputCartoDb;
        this.outputDb = outputDb;
        this.outputCRS = outputCRS;
    }


}
