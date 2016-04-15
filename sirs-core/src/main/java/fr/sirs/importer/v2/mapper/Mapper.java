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
package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.AccessDbImporterException;
import java.io.Closeable;
import java.io.IOException;

/**
 * Describes a converter between data contained in a table (not necessaril all its columns) to any pojo of a specified type.
 * @author Alexis Manin (Geomatys)
 */
public interface Mapper<T> extends Closeable {

    /**
     * Process to the mapping from one row of the configured input to one object
     * of the configured class.
     * @param input row to extract information from.
     * @param output Pojo to put data into.
     * @throws IllegalStateException If input row is not issued from a table compatible 
     * with this mapper. Compatible tables can be checked using {@link MapperSpi#configureInput(com.healthmarketscience.jackcess.Table) }.
     * @throws java.io.IOException If an error is raised while reading in input database.
     * @throws fr.sirs.importer.AccessDbImporterException If an error occurs while mapping.
     */
    void map(final Row input, final T output) throws IllegalStateException, IOException, AccessDbImporterException;
}
