
package fr.sym.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import java.util.AbstractMap;
import java.util.Map.Entry;
import javax.vecmath.Vector2d;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.Static;
import org.geotoolkit.display2d.GO2Utilities;

/**
 * Methodes de calculs utilitaire pour le référencement linéaire.
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class LinearReferencingUtilities extends Static{

    public static final class SegmentInfo{
        public int startCoordIndex;
        public double startDistance;
        public double endDistance;
        public double length;
        public Coordinate[] segmentCoords;
        public LineString geometry;
        public Vector2d forward;
        public Vector2d right;
        
        public Coordinate getPoint(double distanceAlongLinear, double distancePerpendicular){
            final Vector2d tempForward = new Vector2d();
            final Vector2d tempPerpendicular = new Vector2d();
            tempForward.scale(distanceAlongLinear, forward);
            tempPerpendicular.scale(distancePerpendicular, right);
            return new Coordinate(
                segmentCoords[0].x+tempForward.x+tempPerpendicular.x, 
                segmentCoords[0].y+tempForward.y+tempPerpendicular.y);
        }
        
    }
    
    public static final class ProjectedReference{
        public Point reference;
        public SegmentInfo segment;
        public Coordinate[] nearests;
        public double distancePerpendicular;
        public double distanceAlongLinear;
    }
    
    /**
     * Calcul de coordonée en fonction d'une position relative
     * 
     * @param linear geometrie du tronçon
     * @param reference position de la borne
     * @param distanceAlongLinear distance le long du troncon 
     * @param distancePerpendicular distance par rapport au troncon
     * @return position 
     */
    public static Point calculateCoordinate(LineString linear, Point reference, 
            double distanceAlongLinear, double distancePerpendicular){
        ArgumentChecks.ensureNonNull("linear", linear);
        ArgumentChecks.ensureNonNull("reference", reference);
                
        //project reference 
        final SegmentInfo[] segments = buildSegments(linear);
        final ProjectedReference projection = projectReference(segments, reference);
                
        //find segment at given distance
        final double distanceFinal = projection.distanceAlongLinear + distanceAlongLinear;
        final SegmentInfo segment = getSegment(segments, distanceFinal);
        
        return GO2Utilities.JTS_FACTORY.createPoint(segment.getPoint(
                distanceFinal-segment.startDistance, distancePerpendicular));
    }
    
    /**
     * 
     * @param linear geometrie du tronçon
     * @param references position des bornes
     * @param position position géographique
     * @return Entry : index de la reference la plus proche 
     *       double[0] = distance le long du linéaire
     *       double[1] = distance sur le coté du linéaire
     */
    public static Entry<Integer,double[]> calculateRelative(LineString linear, Point[] references, Point position){
        ArgumentChecks.ensureNonNull("linear", linear);
        ArgumentChecks.ensureNonNull("position", position);
        ArgumentChecks.ensureNonNull("references", references);
        ArgumentChecks.ensurePositive("references", references.length);
        
        final SegmentInfo[] segments = buildSegments(linear);
        
        //project target
        final ProjectedReference positionProj = projectReference(segments, position);
        
        //project references and find nearest
        double distanceAlongLinear = Double.MAX_VALUE;
        int index = 0;
        
        for(int i=0;i<references.length;i++){
            final ProjectedReference projection = projectReference(segments, references[i]);
            final double candidateDistance = positionProj.distanceAlongLinear-projection.distanceAlongLinear;
            if(Math.abs(candidateDistance) < Math.abs(distanceAlongLinear)){
                index = i;
                distanceAlongLinear = candidateDistance;
            }
        }
        
        return new AbstractMap.SimpleImmutableEntry<>(index, new double[]{
            distanceAlongLinear, positionProj.distancePerpendicular});        
    }
    
    /**
     * Projection d'une borne sur le troncon et calcule de diverses informations.
     * 
     * @param segments segment du tronçon
     * @param reference position de la borne
     * @return ProjectedReference
     */
    public static ProjectedReference projectReference(SegmentInfo[] segments, Point reference){
        final ProjectedReference projection = new ProjectedReference();
        projection.reference = reference;
        
        //find the nearest segment
        projection.distancePerpendicular = Double.MAX_VALUE;
        
        for(SegmentInfo segment : segments){
            
            final Coordinate[] candidateNearests = DistanceOp.nearestPoints(segment.geometry, reference);
            final double candidateDistance = candidateNearests[0].distance(candidateNearests[1]);
            if(candidateDistance<projection.distancePerpendicular){
                final double side = -lineSide(segment, candidateNearests[1]);
                projection.distancePerpendicular = candidateDistance * Math.signum(side);
                projection.nearests = candidateNearests;
                projection.segment = segment;
                projection.distanceAlongLinear = segment.startDistance + 
                        segment.segmentCoords[0].distance(candidateNearests[0]);
            }
        }
        
        return projection;
    }
    
    /**
     * Analyze segment a segment du lineaire.
     * 
     * @param linear geometry du troncon
     * @return SegmentInfo[] segments du troncon
     */
    private static SegmentInfo[] buildSegments(final LineString linear){
        
        //find the nearest segment
        final Coordinate[] coords = linear.getCoordinates();
        final SegmentInfo[] segments = new SegmentInfo[coords.length-1];
                
        double cumulativeDistance = 0;
        
        for(int i=0;i<coords.length-1;i++){
            
            final SegmentInfo segment = new SegmentInfo();
            segment.startCoordIndex = i;
            segment.segmentCoords = new Coordinate[]{coords[i],coords[i+1]};
            segment.geometry = GO2Utilities.JTS_FACTORY.createLineString(segment.segmentCoords);
            segment.length = segment.segmentCoords[0].distance(segment.segmentCoords[1]);
            segment.startDistance = cumulativeDistance;
            cumulativeDistance += segment.length;
            segment.endDistance = cumulativeDistance;
            
            //calculate direction vectors
            segment.forward = new Vector2d(
                    segment.segmentCoords[1].x-segment.segmentCoords[0].x, 
                    segment.segmentCoords[1].y-segment.segmentCoords[0].y);
            segment.forward.normalize();
            segment.right = new Vector2d(segment.forward.y,-segment.forward.x);
            
            segments[i] = segment;
        }
                
        return segments;
    }
    
    /**
     * Trouve le segment qui contient ou est le plus proche de la distance donnée.
     * 
     * @param segments
     * @param distance
     * @return SegmentInfo
     */
    private static SegmentInfo getSegment(SegmentInfo[] segments, double distance){
        SegmentInfo segment = segments[0];
        for(int i=1;i<segments.length;i++){
            
            if(segments[i].startDistance < distance){
                segment = segments[i];
            }else{
                break;
            }
        }
        return segment;
    }
    
    /**
     * Test the side of a point compare to a line.
     *
     * @param SegmentInfo segment
     * @param c to test
     * @return > 0 if point is on the left side
     *          = 0 if point is on the line
     *          < 0 if point is on the right side
     */
    private static double lineSide(SegmentInfo segment, Coordinate c) {
        return (segment.segmentCoords[1].x-segment.segmentCoords[0].x) * (c.y-segment.segmentCoords[0].y) - 
               (c.x-segment.segmentCoords[0].x) * (segment.segmentCoords[1].y-segment.segmentCoords[0].y);
    }
    
}
