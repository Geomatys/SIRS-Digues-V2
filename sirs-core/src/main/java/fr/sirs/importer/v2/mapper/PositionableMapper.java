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
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.util.Optional;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PositionableMapper extends AbstractMapper<Positionable> {

    protected AbstractImporter<BorneDigue> borneImporter;
    protected AbstractImporter<SystemeReperage> srImporter;

    private enum Columns {
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        ID_SYSTEME_REP,
        ID_BORNEREF_DEBUT,
        AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN
    }

    private PositionableMapper(final Table t) {
        super(t);
        borneImporter = context.importers.get(BorneDigue.class);
        if (borneImporter == null) {
            throw new IllegalStateException("Cannot retrieve needed BorneDigue importer for position imports.");
        }

        srImporter = context.importers.get(SystemeReperage.class);
        if (srImporter == null) {
            throw new IllegalStateException("Cannot retrieve needed SystemeReperage importer for position imports.");
        }
    }

    @Override
    public void map(Row row, Positionable output) throws IllegalStateException, IOException, AccessDbImporterException {
        // GEOGRAPHIC POSITIONING
        try {
            context.setGeoPositions(row, output);
        } catch (TransformException ex) {
            context.reportError(new ErrorReport(ex, row, tableName, null, output, null, "Cannnot set geographic position.", CorruptionLevel.FIELD));
        }

        // LINEAR POSITIONING
        // START
        /* Invalid values appears in databases, so we have to control borne id validity (id > 0)
         * WORSE ! Borne ids are defined with integers in BORNE_DIGUE table, but other tables as
         * ELEMENT_RESEAU use a double field as foreign key, what force us to convert read data.
         */
        Object startId = row.get(Columns.ID_BORNEREF_DEBUT.toString());
        boolean isNumber = startId instanceof Number;
        if (isNumber) {
            startId = ((Number)startId).intValue();
        }
        if (startId != null && (!isNumber || ((Number)startId).intValue() > 0)) {
            final String bId = borneImporter.getImportedId(startId);
            if (bId != null) {
                output.setBorneDebutId(bId);
            } else {
                context.reportError(new ErrorReport(null, row, tableName, Columns.ID_BORNEREF_DEBUT.name(), output, "borneDebutId", "Cannot set linear referencing. No borne imported for ID : " + startId, CorruptionLevel.FIELD));
            }
        }

        final Double startDistance = row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString());
        if (startDistance != null) {
            output.setBorne_debut_distance(startDistance.floatValue());
        }
        final Double startPr = row.getDouble(Columns.PR_DEBUT_CALCULE.toString());
        if (startPr != null) {
            output.setPrDebut(startPr.floatValue());
        }

        output.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

        // END
        /* Invalid values appears in databases, so we have to control borne id validity (id > 0)
         * WORSE ! Borne ids are defined with integers in BORNE_DIGUE table, but other tables as
         * ELEMENT_RESEAU use a double field as foreign key, what force us to convert read data.
         */
        Object endId = row.get(Columns.ID_BORNEREF_FIN.toString());
        isNumber = endId instanceof Number;
        if (isNumber) {
            endId = ((Number)endId).intValue();
        }
        if (endId != null && (!isNumber || ((Number)endId).intValue() > 0)) {
            final String bId = borneImporter.getImportedId(endId);
            if (bId != null) {
                output.setBorneFinId(bId);
            } else {
                context.reportError(new ErrorReport(null, row, tableName, Columns.ID_BORNEREF_FIN.name(), output, "borneFinId", "Cannot set linear referencing. No borne imported for ID : " + endId, CorruptionLevel.FIELD));
            }
        }

        final Double endDistance = row.getDouble(Columns.DIST_BORNEREF_FIN.toString());
        if (endDistance != null) {
            output.setBorne_fin_distance(endDistance.floatValue());
        }
        final Double endPr = row.getDouble(Columns.PR_FIN_CALCULE.toString());
        if (endPr != null) {
            output.setPrFin(endPr.floatValue());
        }

        output.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

        // SR
        final Integer srid = row.getInt(Columns.ID_SYSTEME_REP.toString());
        if (srid != null) {
            output.setSystemeRepId(srImporter.getImportedId(srid));
        }

    }

    @Override
    public void close() throws IOException {
        borneImporter = null;
        srImporter = null;
    }

    @Component
    public static class Spi implements MapperSpi<Positionable> {

        @Override
        public Optional<Mapper<Positionable>> configureInput(Table inputType) throws IllegalStateException {
            Columns[] expected = Columns.values();
            if (inputType.getColumnCount() < expected.length)
                return Optional.empty();
            for (final Columns c : expected) {
                if (!ImportContext.columnExists(inputType, c.name()))
                    return Optional.empty();
            }

            return Optional.of(new PositionableMapper(inputType));
        }

        @Override
        public Class<Positionable> getOutputClass() {
            return Positionable.class;
        }
    }

}
