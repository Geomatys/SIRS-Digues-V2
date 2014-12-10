
package fr.sirs.core;

import fr.sirs.util.json.GeometryDeserializer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Objet;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.vecmath.Vector2d;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.Static;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.primitive.jts.JTSLineIterator;
import org.geotoolkit.display2d.style.j2d.PathWalker;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.math.XMath;

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
        public double distancePerpendicularAbs;
        public double distancePerpendicular;
        public double distanceAlongLinear;
    }
    
    /**
     * Calcul de coordonée en fonction d'une position relative
     * 
     * @param geom geometrie du tronçon
     * @param reference position de la borne
     * @param distanceAlongLinear distance le long du troncon 
     * @param distancePerpendicular distance par rapport au troncon
     * @return position 
     */
    public static Point calculateCoordinate(Geometry geom, Point reference, 
            double distanceAlongLinear, double distancePerpendicular){
        ArgumentChecks.ensureNonNull("linear", geom);
        ArgumentChecks.ensureNonNull("reference", reference);
                
        //project reference 
        final LineString linear = asLineString(geom);
        final SegmentInfo[] segments = buildSegments(linear);
        final ProjectedReference projection = projectReference(segments, reference);
                
        //find segment at given distance
        final double distanceFinal = projection.distanceAlongLinear + distanceAlongLinear;
        final SegmentInfo segment = getSegment(segments, distanceFinal);
        
        final Point pt = GO2Utilities.JTS_FACTORY.createPoint(segment.getPoint(
                distanceFinal-segment.startDistance, distancePerpendicular));
        pt.setSRID(geom.getSRID());
        pt.setUserData(geom.getUserData());
        return pt;
    }
    
    /**
     * 
     * @param geom geometrie du tronçon
     * @param references position des bornes
     * @param position position géographique
     * @return Entry : index de la reference la plus proche 
     *       double[0] = distance le long du linéaire
     *       double[1] = distance sur le coté du linéaire
     */
    public static Entry<Integer,double[]> calculateRelative(Geometry geom, Point[] references, Point position){
        ArgumentChecks.ensureNonNull("linear", geom);
        ArgumentChecks.ensureNonNull("position", position);
        ArgumentChecks.ensureNonNull("references", references);
        ArgumentChecks.ensurePositive("references", references.length);
        
        final LineString linear = asLineString(geom);
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
        projection.distancePerpendicularAbs = Double.MAX_VALUE;
        projection.distancePerpendicular = Double.MAX_VALUE;
        
        for(SegmentInfo segment : segments){
            
            final Coordinate[] candidateNearests = DistanceOp.nearestPoints(segment.geometry, reference);
            final double candidateDistance = candidateNearests[0].distance(candidateNearests[1]);
            if(candidateDistance<projection.distancePerpendicularAbs){
                final double side = -lineSide(segment, candidateNearests[1]);
                projection.distancePerpendicularAbs = candidateDistance;
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
    public static SegmentInfo[] buildSegments(final LineString linear){
        
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
    
    public static LineString buildGeometry(Geometry tronconGeom, Objet structure, BorneDigueRepository repo){
        
        final LineString troncon = asLineString(tronconGeom);        
        
        if(structure.getPositionDebut()!=null){
            //reconstruction a partir de 2 points géographique
            //TODO
        }else{
            //reconstruction a partir de bornes et de distances
            BorneDigue borneDebut = (structure.getBorneDebutId()!=null) ? repo.get(structure.getBorneDebutId()) : null;
            BorneDigue borneFin = (structure.getBorneFinId()!=null) ? repo.get(structure.getBorneFinId()) : null;
            if(borneDebut==null && borneFin==null){
                //aucune borne définie, on ne peut pas calculer la géométrie
                return null;
            }
            
            //il peut y avoir qu'une seule borne définie dans le cas d'un ponctuel
            if(borneDebut==null) borneDebut = borneFin;
            if(borneFin==null) borneFin = borneDebut;
            
            double distanceDebut = structure.getBorne_debut_distance();
            double distanceFin = structure.getBorne_fin_distance();
            //on considére que les troncons sont numérisé dans le sens amont vers aval.
            if(!structure.getBorne_debut_aval()) distanceDebut *= -1.0;
            if(!structure.getBorne_fin_aval()) distanceFin *= -1.0;
            
            //calculate de la distance des bornes
            final Point borneDebutGeom = borneDebut.getGeometry();
            final Point borneFinGeom = borneFin.getGeometry();
            
            final Point tronconStart = GO2Utilities.JTS_FACTORY.createPoint(troncon.getCoordinates()[0]);
            final double borneDebutDistance = calculateRelative(troncon, new Point[]{tronconStart}, borneDebutGeom).getValue()[0];
            final double borneFinDistance = calculateRelative(troncon, new Point[]{tronconStart}, borneFinGeom).getValue()[0];
            
            //conversion des distances au borne en distance par rapport au debut du troncon
            distanceDebut += borneDebutDistance;
            distanceFin += borneFinDistance;
            
            //on s'assure de l'ordre croissant des positions
            if(distanceDebut>distanceFin){
                double temp = distanceDebut;
                distanceDebut = distanceFin;
                distanceFin = temp;
            }
            
            //on s"assure de ne pas sortir du troncon
            final double tronconLength = troncon.getLength();
            distanceDebut = XMath.clamp(distanceDebut, 0, tronconLength);
            distanceFin = XMath.clamp(distanceFin, 0, tronconLength);
            
            //create du tracé de la structure le long du troncon
            final PathIterator ite = new JTSLineIterator(troncon, null);
            final PathWalker walker = new PathWalker(ite);
            walker.walk((float)distanceDebut);
            float remain = (float) (distanceFin-distanceDebut);
            
            final List<Coordinate> structureCoords = new ArrayList<>();
            Point2D point = walker.getPosition(null);
            structureCoords.add(new Coordinate(point.getX(), point.getY()));
            
            while(!walker.isFinished() && remain>0){
                final float advance = Math.min(walker.getSegmentLengthRemaining(), remain);
                remain -= advance;
                walker.walk(advance);
                point = walker.getPosition(point);
                structureCoords.add(new Coordinate(point.getX(), point.getY()));
            }
            
            if(structureCoords.size()==1){
                //point unique, on le duplique pour obtenir on moins un segment
                structureCoords.add(new Coordinate(structureCoords.get(0)));
            }
            
            final LineString geom = GO2Utilities.JTS_FACTORY.createLineString(structureCoords.toArray(new Coordinate[structureCoords.size()]));
            JTS.setCRS(geom, GeometryDeserializer.PROJECTION);
            
            return geom;
        }
        
        return null;
    }
    
    public static LineString asLineString(Geometry tronconGeom) {
        final LineString troncon;
        if (tronconGeom instanceof LineString) {
            troncon = (LineString) tronconGeom;
        } else if (tronconGeom instanceof MultiLineString) {
            final MultiLineString mls = (MultiLineString) tronconGeom;
            if (mls.getNumGeometries() != 1) {
                throw new IllegalArgumentException("Geometry must be a LineString or a MultilineString with 1 sub-geometry.");
            }
            troncon = (LineString) mls.getGeometryN(0);
        } else {
            throw new IllegalArgumentException("Geometry must be a LineString or a MultilineString with 1 sub-geometry.");
        }
        return troncon;
    }
    
}
