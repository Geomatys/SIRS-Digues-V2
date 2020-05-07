
package fr.sirs;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotoolkit.filter.function.AbstractFunction;
import org.geotoolkit.geometry.jts.JTS;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 * Try to convert to Points feature's parameters in a {@link LineString}.
 * If only 1 point is non-null return this point.
 *
 *
 *
 * @author Matthieu Bastianelli (Geomatys)
 * @throw {@link IllegalArgumentException} if one of the parameter is missing.
 * @throw {@link RuntimeException} if fail to convert the 2nd point in the 1st point CRS.
 */
public class PointsToLine extends AbstractFunction {

    private static final GeometryFactory GF = new GeometryFactory();

    public PointsToLine(final Expression expr1, final Expression expr2) {
        super("PointsToLines", new Expression[] {expr1,expr2}, null);
    }

    @Override
    public Object evaluate(final Object feature) {
        final Geometry geom1;

        final Geometry geom2;

        try {
            geom1 = parameters.get(0).evaluate(feature, Geometry.class);
            geom2 = parameters.get(1).evaluate(feature, Geometry.class);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid function parameter." + parameters.get(0) + " " + parameters.get(1), e);
        }

        final Point pt1 = getPoint(geom1);
        final Point pt2 = getPoint(geom2);

        if (pt1 == null) {
            if(pt2 == null) {
                return null;
            } else {
                return pt2;
            }
        } else if (pt2 == null) {
            return pt1;
        }

        final int srid = geom1.getSRID();
        final Object userData = geom1.getUserData();
        pt1.setSRID(srid);
        pt1.setUserData(userData);
        CoordinateReferenceSystem startCRS = null;
        try {
            startCRS = JTS.findCoordinateReferenceSystem(geom1);
            JTS.convertToCRS(pt2, startCRS);
        } catch (MismatchedDimensionException | TransformException | FactoryException e) {
            throw new RuntimeException("Fail to convert the 2nd geometry " + parameters.get(1) + "In the same CRS than the 1st one "+parameters.get(0)+" ;\n srid = "+srid+"\n CRS = "+startCRS, e);
        }
//        pt2.setSRID(geom2.getSRID());
//        pt2.setUserData(geom2.getUserData());

        final LineString lineString = GF.createLineString(new Coordinate[]{
            pt1.getCoordinate(),
            pt2.getCoordinate()
        });
        lineString.setSRID(srid);
        lineString.setUserData(userData);

        return lineString;
    }

    private static Point getPoint(final Geometry geom){
        if(geom == null) {
            return null;
        }
        if(geom instanceof LineString){
            return ((LineString)geom).getStartPoint();
        }else if(geom instanceof Point){
            return (Point) ((Point)geom).clone();
        }else if(geom instanceof Polygon){
            return getPoint( ((Polygon)geom).getExteriorRing());
        }else if(geom instanceof GeometryCollection){
            final int nb = ((GeometryCollection)geom).getNumGeometries();
            if(nb!=0){
                return getPoint(((GeometryCollection)geom).getGeometryN(0));
            }else{
                 return null;
            }
        }else{
            return null;
        }
    }

}

