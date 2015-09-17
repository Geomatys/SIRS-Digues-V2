/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.IOException;
import java.util.Optional;
import org.opengis.referencing.operation.TransformException;

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
        // We're forced to get a double as idd, because of input database definition.
        final Double startId = row.getDouble(Columns.ID_BORNEREF_DEBUT.toString());
        if (startId != null) {
            final String bId = borneImporter.getImportedId(startId.intValue());
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

        // END
        final Double endId = row.getDouble(Columns.ID_BORNEREF_FIN.toString());
        if (endId != null) {
            final String bId = borneImporter.getImportedId(endId.intValue());
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

        // SR
        final Integer srid = row.getInt(Columns.ID_SYSTEME_REP.toString());
        if (srid != null) {
            output.setSystemeRepId(srImporter.getImportedId(srid));
        }

        output.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));
        output.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
    }

    @Override
    public void close() throws IOException {
        borneImporter = null;
        srImporter = null;
    }

    public static class Spi implements MapperSpi<Positionable> {

        @Override
        public Optional<Mapper<Positionable>> configureInput(Table inputType) throws IllegalStateException {
            Columns[] expected = Columns.values();
            if (inputType.getColumnCount() < expected.length)
                return Optional.empty();
            for (final Columns c : expected) {
                if (inputType.getColumn(c.name()) == null)
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
