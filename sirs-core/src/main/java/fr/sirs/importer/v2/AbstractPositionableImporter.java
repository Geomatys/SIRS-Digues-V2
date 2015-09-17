/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import org.opengis.referencing.operation.TransformException;

/**
 * IMPORTANT : WE SET GEOGRPHIC START AND END POINT AS LINEAR REFERENCING, BUT WE
 * CANNOT SET GEOMETRY HERE, BECAUSE WE DO NOT KNOW TRONCON ID YET.
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class AbstractPositionableImporter<T extends Positionable> extends AbstractImporter<T> {

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

    @Override
    protected void postCompute() {
        borneImporter = null;
        srImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        borneImporter = context.importers.get(BorneDigue.class);
        if (borneImporter == null) {
            throw new AccessDbImporterException("Cannot retrieve needed BorneDigue importer for position imports.");
        }

        srImporter = context.importers.get(SystemeReperage.class);
        if (srImporter == null) {
            throw new AccessDbImporterException("Cannot retrieve needed SystemeReperage importer for position imports.");
        }
    }

    @Override
    public T importRow(Row row, T output) throws IOException, AccessDbImporterException {
        output = super.importRow(row, output);

        // GEOGRAPHIC POSITIONING
        try {
            context.setGeoPositions(row, output);
        } catch (TransformException ex) {
            context.reportError(new ErrorReport(ex, row, getTableName(), null, output, null, "Cannnot set geographic position.", CorruptionLevel.FIELD));
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
                context.reportError(new ErrorReport(null, row, getTableName(), Columns.ID_BORNEREF_DEBUT.name(), output, "borneDebutId", "Cannot set linear referencing. No borne imported for ID : "+startId, CorruptionLevel.FIELD));
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
                context.reportError(new ErrorReport(null, row, getTableName(), Columns.ID_BORNEREF_FIN.name(), output, "borneFinId", "Cannot set linear referencing. No borne imported for ID : "+endId, CorruptionLevel.FIELD));
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

        return output;
    }
}
