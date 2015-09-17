package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.XYZLeveProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.PROFIL_EN_TRAVERS_XYZ;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class ProfilTraversPointXYZImporter extends DocumentImporter {

    private Map<Integer, XYZLeveProfilTravers> points = null;
    private Map<Integer, List<XYZLeveProfilTravers>> pointsByLeve = null;

    ProfilTraversPointXYZImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public Map<Integer, XYZLeveProfilTravers> getLeveePoints() throws IOException, AccessDbImporterException{
        if(points==null) compute();
        return points;
    }

    public Map<Integer, List<XYZLeveProfilTravers>> getLeveePointByLeveId() throws IOException, AccessDbImporterException{
        if(pointsByLeve==null) compute();
        return pointsByLeve;
    }

    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_POINT,
        X,
        Y,
        Z,
//        DATE_DERNIERE_MAJ // Pas dans le nouveau modèle
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return PROFIL_EN_TRAVERS_XYZ.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        points = new HashMap<>();
        pointsByLeve = new HashMap<>();

        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final XYZLeveProfilTravers levePoint = createAnonymValidElement(XYZLeveProfilTravers.class);

            final Double pointX = row.getDouble(Columns.X.toString());
            final Double pointY = row.getDouble(Columns.Y.toString());
            if (pointX != null && pointY != null) {

                final double[] point = new double[]{pointX, pointY};

                try {
                    context.geoTransform.transform(point, 0, point, 0, 1);
                    levePoint.setX(point[0]);
                    levePoint.setY(point[1]);

                } catch (MismatchedDimensionException | TransformException ex) {
                    context.reportError(getTableName(), row, ex, "Cannot affect a point due to bad transformation");
                }
            }

            if (row.getDouble(Columns.Z.toString()) != null) {
                levePoint.setZ(row.getDouble(Columns.Z.toString()));
            }

            levePoint.setDesignation(String.valueOf(row.getInt(Columns.ID_POINT.toString())));

            points.put(row.getInt(Columns.ID_POINT.toString()), levePoint);

            List<XYZLeveProfilTravers> listByLeve = pointsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
            if (listByLeve == null) {
                listByLeve = new ArrayList<>();
                pointsByLeve.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), listByLeve);
            }
            listByLeve.add(levePoint);
        }
    }
}
