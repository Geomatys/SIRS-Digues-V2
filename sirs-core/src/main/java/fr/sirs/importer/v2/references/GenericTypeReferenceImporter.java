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
package fr.sirs.importer.v2.references;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;

/**
 * Base class for {@link ReferenceType} object import.
 * @author Samuel Andrés (Geomatys)
 * @param <T> Managed implementation.
 */
public abstract class GenericTypeReferenceImporter<T extends ReferenceType> extends AbstractImporter<T> {

    private static final String ID_PREFIX = "ID";
    private static final String LIBELLE_PREFIX = "LIBELLE";
    private static final String ABREGE_PREFIX = "ABREGE";
    private static final String DATE_COLUMN = "DATE_DERNIERE_MAJ";

    /**
     * Name of the columns to import. Their order is important for import
     * process : 0 : id 1 : libelle 2 : abrege 3 : date.
     */
    private final String[] columns;

    private Method setId;
    private Method setAbrege;

    public GenericTypeReferenceImporter() {
        super();
        columns = new String[4];
    }

    @PostConstruct
    private void indexColumns() {
        final Table table;
        try {
            table = context.inputDb.getTable(getTableName());
        } catch (IOException ex) {
            throw new SirsCoreRuntimeException("Cannot create any importer for a reference type !", ex);
        }

        try {
            List<? extends Index.Column> primaryKey = table.getPrimaryKeyIndex().getColumns();
            if (!primaryKey.isEmpty()) {
                columns[0] = primaryKey.get(0).getName();
            }
        } catch (IllegalArgumentException e) {
            SirsCore.LOGGER.log(Level.FINE, "No primary key defined for reference table " + table.getName(), e);
        }

        String name;
        String start;
        String fallbackId = null;
        for (final Column col : table.getColumns()) {
            name = col.getName();
            start = name.substring(0, name.indexOf('_')).toUpperCase();
            switch (start) {
                case ID_PREFIX:
                    if (columns[0] != null)
                        break;
                    if (name.equals(ID_PREFIX+'_'+getTableName()))
                        columns[0] = name;
                    else
                        fallbackId = name;
                    break;
                case LIBELLE_PREFIX:
                    columns[1] = name;
                    break;
                case ABREGE_PREFIX:
                    columns[2] = name;
                    break;
            }
        }

        if (columns[0] == null) {
            if (fallbackId != null)
                columns[0] = fallbackId;
            else
                throw new IllegalStateException("Not any id column found for table "+getTableName());
        }
        columns[3] = DATE_COLUMN;
    }

    @Override
    public List<String> getUsedColumns() {
        return Arrays.asList(columns);
    }

    @Override
    public String getRowIdFieldName() {
        return columns[0];
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();

        try {
            final Class<T> outputClass = getElementClass();
            setId = outputClass.getMethod("setId", String.class);
            setId.setAccessible(true);
            // Some reference table don't have an "Abrege" column.
            if (columns[2] != null) {
                setAbrege = outputClass.getMethod("setAbrege", String.class);
            }
            setId.setAccessible(true);
        } catch (Exception e) {
            throw new AccessDbImporterException("A required method cannot be found / accessed.", e);
        }
    }

    @Override
    protected void postCompute() {
        super.postCompute();

        setId = null;
        setAbrege = null;
    }

    @Override
    public T importRow(Row row, T output) throws IOException, AccessDbImporterException {
        final Object refId = row.get(columns[0]);

        try {
            setId.invoke(output, output.getClass().getSimpleName() + ":" + refId);
            output.setLibelle(row.getString(columns[1]));
            if (setAbrege != null)
                setAbrege.invoke(output, row.getString(columns[2]));
        } catch (Exception e) {
            throw new SirsCoreRuntimeException("Cannot set reference attributes !", e);
        }

        final Date dateMaj = row.getDate(columns[3]);
        if (dateMaj != null) {
            output.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        return output;
    }
}
