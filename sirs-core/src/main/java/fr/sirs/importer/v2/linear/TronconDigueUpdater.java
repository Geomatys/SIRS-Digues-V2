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
package fr.sirs.importer.v2.linear;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ExtraOperator;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Update CouchDB {@link TronconDigue} by adding id of their default
 * {@link SystemeReperage}, as the list of their bound {@link BorneDigue}
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class TronconDigueUpdater implements ExtraOperator {

    /**
     * The importer used for computing documents we want to update here.
     */
    protected AbstractImporter masterImporter;

    /**
     * Repository used for getting documents we want to update.
     */
    protected AbstractSIRSRepository<TronconDigue> masterRepository;

    @Autowired
    private ImportContext context;

    @Autowired
    private SessionCore session;

    private AbstractImporter<SystemeReperage> srImporter;
    private AbstractImporter<BorneDigue> borneImporter;
    private Column borneFilterColumn;
    private Cursor borneCursor;

    private boolean finished = false;

    public Class<TronconDigue> getElementClass() {
        return TronconDigue.class;
    }

    public String getTableName() {
        return DbImporter.TableName.TRONCON_GESTION_DIGUE.name();
    }

    public String getRowIdFieldName() {
        return TronconGestionDigueImporter.Columns.ID_TRONCON_GESTION.name();
    }

    protected void preCompute() throws AccessDbImporterException {
        srImporter = context.importers.get(SystemeReperage.class);
        borneImporter = context.importers.get(BorneDigue.class);

        try {
            final Table borneTable = context.inputDb.getTable(borneImporter.getTableName());
            borneFilterColumn = borneTable.getColumn(BorneDigueImporter.Columns.ID_TRONCON_GESTION.name());
            borneCursor = borneTable.newCursor().beforeFirst().toCursor();
        } catch (IOException e) {
            throw new AccessDbImporterException("Cannot find borne table.", e);
        }

        masterImporter = context.importers.get(TronconDigue.class);
        if (masterImporter == null) {
            throw new IllegalStateException("Cannot find any importer for type : " + TronconDigue.class);
        }

        masterRepository = session.getRepositoryForClass(TronconDigue.class);
        if (masterRepository == null) {
            throw new IllegalStateException("No repository found to read elements of type : " + TronconDigue.class);
        }
    }

    protected void postCompute() {
        srImporter = null;
        borneImporter = null;
        borneFilterColumn = null;
        borneCursor = null;
        masterImporter = null;
        masterRepository = null;
    }

    public TronconDigue importRow(Row row, TronconDigue output) throws IOException, AccessDbImporterException {
        Integer srid = row.getInt(TronconGestionDigueImporter.Columns.ID_SYSTEME_REP_DEFAUT.name());
        if (srid != null) {
            output.setSystemeRepDefautId(srImporter.getImportedId(srid));
        }

        final Object tronconId = row.get(getRowIdFieldName());
        if (tronconId == null) {
            throw new AccessDbImporterException("Troncon to update has no Id in origin database !");
        }

        borneCursor.beforeFirst();
        while (borneCursor.findNextRow(borneFilterColumn, tronconId)) {
            Integer borneId = borneCursor.getCurrentRow().getInt(borneImporter.getRowIdFieldName());
            output.getBorneIds().add(borneImporter.getImportedId(borneId));
        }

        return output;
    }

    private TronconDigue getElement(final Row input) {
        final Object rowId = input.get(masterImporter.getRowIdFieldName());
        if (rowId == null) {
            throw new IllegalStateException("Input has no valid ID.");
        }

        final String elementId;
        try {
            elementId = masterImporter.getImportedId(rowId);
        } catch (IOException | AccessDbImporterException ex) {
            throw new IllegalStateException("Import of table "+masterImporter.getTableName()+" failed.", ex);
        }

        return masterRepository.get(elementId);
    }

    @Override
    public synchronized void compute() throws AccessDbImporterException {
        if (finished) {
            return;
        }

        preCompute();
        try {
            final Iterator<Row> iterator;
            try {
                iterator = context.inputDb.getTable(getTableName()).iterator();
            } catch (IOException ex) {
                throw new AccessDbImporterException("Cannot connect to ms-access table " + getTableName(), ex);
            }

            while (iterator.hasNext()) {
                int bulkCount = context.bulkLimit;
                final HashSet toUpdate = new HashSet();
                while (iterator.hasNext() && bulkCount-- > 0) {
                    try {
                        Row current = iterator.next();
                        toUpdate.add(importRow(current, getElement(current)));
                    } catch (IOException ex) {
                        throw new AccessDbImporterException("Failed to update a document of type " + TronconDigue.class, ex);
                    }
                }

                context.outputDb.executeBulk(toUpdate);
            }

            finished = true;
        } finally {
            postCompute();
        }
    }
}
