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
        ImportContext.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);
        this.table = table;
        this.tableName = table.getName();
    }

    @Override
    public void close() throws IOException {}
}
