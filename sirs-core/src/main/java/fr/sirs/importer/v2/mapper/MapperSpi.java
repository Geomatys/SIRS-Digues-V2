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

import com.healthmarketscience.jackcess.Table;
import fr.sirs.importer.v2.ImportContext;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Managed output type.
 */
public interface MapperSpi<T> {

    /**
     * Ask SPI to prepare a mapper to work with a given table. If table format is not
     * compatible with available mapper capabilities, no mapper will be provided.
     *
     * @param inputType The table which must serve as enry point for the mapping.
     * @return True if current mapper can extract information from given table. False otherwise.
     */
    Optional<Mapper<T>> configureInput(final Table inputType) throws IllegalStateException;

    /**
     * @return type of object managed by this mapper.
     */
    Class<T> getOutputClass();

    public static String[] getEnumNames(final Enum[] source) {
        final String[] values = new String[source.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = source[i].name();
        }

        return values;
    }

    /**
     * Ensure that all column given as argument are present in given table
     *
     * @param source The table to test.
     * @param expected Column names we expect to find in input table.
     * @return True if all given column names have been found, false otherwise.
     */
    public static boolean checkColumns(final Table source, final Enum[] expected) {
        return checkColumns(source, getEnumNames(expected));
    }

    /**
     * Ensure that all column given as argument are present in given table
     *
     * @param source The table to test.
     * @param expected Column names we expect to find in input table.
     * @return True if all given column names have been found, false otherwise.
     */
    public static boolean checkColumns(final Table source, final String[] expected) {
        for (final String str : expected) {
            if (!ImportContext.columnExists(source, str)) {
                return false;
            }
        }
        return true;
    }
}
