package fr.sirs.importer.v2.linear;

import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.BORNE_DIGUE;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ErrorReport;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.geotoolkit.display2d.GO2Utilities;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class BorneDigueImporter extends AbstractImporter<BorneDigue> {

    // TODO : delete
    private Map<Integer, List<BorneDigue>> bornesDigueByTronconId = null;

    // TODO : package-private
    public enum Columns {
        ID_BORNE,
        ID_TRONCON_GESTION,
        NOM_BORNE,
        X_POINT,
        Y_POINT,
        Z_POINT,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        FICTIVE,
//        X_POINT_ORIGINE,
//        Y_POINT_ORIGINE,
        COMMENTAIRE_BORNE,
        DATE_DERNIERE_MAJ
    };

    @Override
    protected Class getDocumentClass() {
        return BorneDigue.class;
    }

    @Override
    public BorneDigue importRow(Row row, BorneDigue output) throws IOException, AccessDbImporterException {
        output = super.importRow(row, output);
        output.setLibelle(row.getString(Columns.NOM_BORNE.toString()));
        output.setCommentaire(row.getString(Columns.COMMENTAIRE_BORNE.toString()));

        final Date dateMaj = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());
        if (dateMaj != null) {
            output.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        final Date dateDebut = row.getDate(Columns.DATE_DEBUT_VAL.toString());
        if (dateDebut != null) {
            output.setDate_debut(context.convertData(dateDebut, LocalDate.class));
        }

        final Date dateFin = row.getDate(Columns.DATE_FIN_VAL.toString());
        if (dateFin != null) {
            output.setDate_fin(context.convertData(dateFin, LocalDate.class));
        }

        output.setFictive(row.getBoolean(Columns.FICTIVE.toString()));

        try {
            final Double pointZ = row.getDouble(Columns.Z_POINT.toString());
            final Point point;
            if (pointZ != null) {
                final double[] coord = new double[]{
                    row.getDouble(Columns.X_POINT.toString()),
                    row.getDouble(Columns.Y_POINT.toString()),
                    pointZ
                };
                context.geoTransform.transform(coord, 0, coord, 0, 1);
                point = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(coord[0], coord[1], coord[2]));
            } else {

                final double[] coord = new double[]{
                    row.getDouble(Columns.X_POINT.toString()),
                    row.getDouble(Columns.Y_POINT.toString())
                };
                context.geoTransform.transform(coord, 0, coord, 0, 1);
                point = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(coord[0], coord[1]));
            }
            output.setGeometry(point);
        } catch (TransformException ex) {
            context.reportError(new ErrorReport(ex, row, getTableName(), null, output, "geometry", "Cannot set position of a borne.", CorruptionLevel.FIELD));
        }

        output.setDesignation(String.valueOf(row.getInt(Columns.ID_BORNE.toString())));

        // Set the list ByTronconId
        List<BorneDigue> listByTronconId = bornesDigueByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        if (listByTronconId == null) {
            listByTronconId = new ArrayList<>();
            bornesDigueByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
        }

        listByTronconId.add(output);
        bornesDigueByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);

        return output;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_BORNE.name();
    }

    /**
     *
     * @return A map containing the BorneDigue lists referenced by the internal
     * database TronconDigue idenfifier.
     * @throws IOException
     */
    public Map<Integer, List<BorneDigue>> getBorneDigueByTronconId() throws IOException, AccessDbImporterException {
        if(bornesDigueByTronconId==null) compute();
        return bornesDigueByTronconId;
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for(Columns c : Columns.values())
            columns.add(c.toString());
        return columns;
    }

    @Override
    public String getTableName() {
        return BORNE_DIGUE.toString();
    }
}
