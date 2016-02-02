
package fr.sirs.core;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.util.Map.Entry;
import org.geotoolkit.display2d.GO2Utilities;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class LinearReferencingUtilitiesTest {

    private static final GeometryFactory GF = GO2Utilities.JTS_FACTORY;
    private static final double DELTA = 0.0000000001;

    @Test
    public void calculateCoordinateTest(){
        
        final LineString troncon = GF.createLineString(new Coordinate[]{
            new Coordinate(0, 0),
            new Coordinate(0, 10),
            new Coordinate(100, 10),
        });
        
        //point +0 forward +0 side
        Point reference = GF.createPoint(new Coordinate(0, 0));
        Point result = LinearReferencingUtilities.computeCoordinate(troncon,reference,0,0);
        Point expected = GF.createPoint(new Coordinate(0, 0));
        assertEquals(expected, result);
        
        //point +4 forward +0 side
        reference = GF.createPoint(new Coordinate(0, 0));
        result = LinearReferencingUtilities.computeCoordinate(troncon,reference,4,0);
        expected = GF.createPoint(new Coordinate(0, 4));
        assertEquals(expected, result);
        
        //point +4 forward +3 side
        reference = GF.createPoint(new Coordinate(0, 0));
        result = LinearReferencingUtilities.computeCoordinate(troncon,reference,4,3);
        expected = GF.createPoint(new Coordinate(3, 4));
        assertEquals(expected, result);
        
        //point +4 forward +3 side
        reference = GF.createPoint(new Coordinate(80, 12));
        //should be projected on linear as 80,10
        result = LinearReferencingUtilities.computeCoordinate(troncon,reference,-2,+4);
        expected = GF.createPoint(new Coordinate(78, 6));
        assertEquals(expected, result);
        
        //point before first segment and reference
        reference = GF.createPoint(new Coordinate(-2, 3));
        result = LinearReferencingUtilities.computeCoordinate(troncon,reference,-20,-6);
        expected = GF.createPoint(new Coordinate(-6, -17));
        assertEquals(expected, result);
        
    }
    
    @Test
    public void calculateRelativeTest(){
        final LineString troncon = GF.createLineString(new Coordinate[]{
            new Coordinate(0, 0),
            new Coordinate(0, 10),
            new Coordinate(100, 10),
        });
        
        
        final Point[] references = {
            GF.createPoint(new Coordinate(0, 0)),
            GF.createPoint(new Coordinate(5, 23)),
            GF.createPoint(new Coordinate(80, 3))
        };
        
        Entry<Integer, Double> result = LinearReferencingUtilities.computeRelative(
                troncon, references, GF.createPoint(new Coordinate(0, 0)));
        assertEquals(0, (int)result.getKey());
        assertEquals(0d, result.getValue(), DELTA);
        
        
        result = LinearReferencingUtilities.computeRelative(
                troncon, references, GF.createPoint(new Coordinate(3, 5)));
        assertEquals(0, (int)result.getKey());
        assertEquals(5, result.getValue(), DELTA);
        
        
        result = LinearReferencingUtilities.computeRelative(
                troncon, references, GF.createPoint(new Coordinate(-2, 8)));
        assertEquals(1, (int)result.getKey());
        assertEquals(-7, result.getValue(), DELTA);
        
        
        result = LinearReferencingUtilities.computeRelative(
                troncon, references, GF.createPoint(new Coordinate(32, 60)));
        assertEquals(1, (int)result.getKey());
        assertEquals(27, result.getValue(), DELTA);
        
        
        result = LinearReferencingUtilities.computeRelative(
                troncon, references, GF.createPoint(new Coordinate(42, -12)));
        assertEquals(1, (int)result.getKey());
        assertEquals(37, result.getValue(), DELTA);
        
        
        result = LinearReferencingUtilities.computeRelative(
                troncon, references, GF.createPoint(new Coordinate(43, 13)));
        assertEquals(2, (int)result.getKey());
        assertEquals(-37, result.getValue(), DELTA);
    }
    
    
}
