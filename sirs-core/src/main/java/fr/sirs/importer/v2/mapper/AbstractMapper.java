/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.InjectorCore;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import org.apache.sis.util.ArgumentChecks;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for {@link Mapper interface}.
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class AbstractMapper<T> implements Mapper<T> {

    protected final Table table;
    /**
     * Name of the table configured for mapping.
     */
    protected final String tableName;

    @Autowired
    protected ImportContext context;

    protected AbstractMapper(final Table table) {
        ArgumentChecks.ensureNonNull("Input table to work with", table);
        InjectorCore.injectDependencies(this);
        this.table = table;
        this.tableName = table.getName();
    }

    @Override
    public void close() throws IOException {}
}
