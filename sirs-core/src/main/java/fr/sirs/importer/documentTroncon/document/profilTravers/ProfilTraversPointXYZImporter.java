package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.XYZLeveProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class ProfilTraversPointXYZImporter extends GenericImporter {

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
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final XYZLeveProfilTravers levePoint = createAnonymValidElement(XYZLeveProfilTravers.class);

            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X.toString()) != null && row.getDouble(Columns.Y.toString()) != null) {
                        Point point = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X.toString()),
                                row.getDouble(Columns.Y.toString()))), lambertToRGF);
                        levePoint.setX(point.getX());
                        levePoint.setY(point.getY());
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
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
