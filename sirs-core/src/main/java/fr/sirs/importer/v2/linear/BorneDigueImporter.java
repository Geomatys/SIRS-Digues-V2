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
import java.util.ArrayList;
import java.util.List;
import org.geotoolkit.display2d.GO2Utilities;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class BorneDigueImporter extends AbstractImporter<BorneDigue> {

    // TODO : package-private
    public enum Columns {
        ID_BORNE,
        ID_TRONCON_GESTION,
        NOM_BORNE,
        X_POINT,
        Y_POINT,
        Z_POINT,
        FICTIVE,
//        X_POINT_ORIGINE,
//        Y_POINT_ORIGINE,
    };

    @Override
    protected Class getElementClass() {
        return BorneDigue.class;
    }

    @Override
    public BorneDigue importRow(Row row, BorneDigue output) throws IOException, AccessDbImporterException {
        output = super.importRow(row, output);
        output.setLibelle(row.getString(Columns.NOM_BORNE.toString()));

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

        return output;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_BORNE.name();
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
