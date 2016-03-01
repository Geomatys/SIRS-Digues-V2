/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
