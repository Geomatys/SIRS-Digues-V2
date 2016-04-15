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
