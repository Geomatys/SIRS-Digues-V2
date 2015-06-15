
package fr.sirs.core;

import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.model.BorneDigue;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Positionable;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.primitive.jts.JTSLineIterator;
import org.geotoolkit.display2d.style.j2d.PathWalker;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.math.XMath;
import org.geotoolkit.referencing.LinearReferencing;

/**
 * Methodes de calculs utilitaire pour le référencement linéaire.
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class LinearReferencingUtilities extends LinearReferencing {
    
    /**
     * Create a JTS geometry for the input {@link Positionable}. Generated geometry
     * is a line string along an input geometry, whose beginning and end are defined
     * by geographic begin and end position in the {@link Positionable}. If no 
     * valid point can be found, we will use its start and end {@link BorneDigue}.
     * @param tronconGeom The source geometry to follow when creating the new one.
     * @param structure The object to generate a geometry for.
     * @param repo The {@link BorneDigueRepository} to use to retrieve input {@link Positionable} bornes.
     * @return A line string for the given structure. Never null.
     */
    public static LineString buildGeometry(Geometry tronconGeom, Positionable structure, AbstractSIRSRepository<BorneDigue> repo) {
        
        final LineString troncon = asLineString(tronconGeom);
        SegmentInfo[] segments = buildSegments(troncon);
        
        Point positionDebut = structure.getPositionDebut();
        Point positionFin = structure.getPositionFin();
        if (positionDebut != null || positionFin != null) {
            ProjectedPoint refDebut = null, refFin = null;
            if (positionDebut != null) refDebut = projectReference(segments, positionDebut);
            if (positionFin != null) refFin = projectReference(segments, positionFin);
            if (refDebut == null) refDebut = refFin;
            else if (refFin == null) refFin = refDebut;
            
            return cut(troncon, refDebut.distanceAlongLinear, refFin.distanceAlongLinear);
            
        } else {
            //reconstruction a partir de bornes et de distances
            final BorneDigue borneDebut = (structure.getBorneDebutId()!=null) ? repo.get(structure.getBorneDebutId()) : null;
            final BorneDigue borneFin = (structure.getBorneFinId()!=null) ? repo.get(structure.getBorneFinId()) : null;
            if(borneDebut==null && borneFin==null){
                //aucune borne définie, on ne peut pas calculer la géométrie
                return null;
            }
            
            double distanceDebut = structure.getBorne_debut_distance();
            double distanceFin = structure.getBorne_fin_distance();
            //on considére que les troncons sont numérisé dans le sens amont vers aval.
            if(structure.getBorne_debut_aval()) distanceDebut *= -1.0;
            if(structure.getBorne_fin_aval()) distanceFin *= -1.0;
            
            //calculate de la distance des bornes. Il peut y avoir qu'une seule borne définie dans le cas d'un ponctuel.
            final Point tronconStart = GO2Utilities.JTS_FACTORY.createPoint(troncon.getCoordinates()[0]);
            if (borneDebut != null) {
                final Point borneDebutGeom = borneDebut.getGeometry();
                final double borneDebutDistance = computeRelative(segments, new Point[]{tronconStart}, borneDebutGeom).getValue();
                //conversion des distances au borne en distance par rapport au debut du troncon
                distanceDebut += borneDebutDistance;
            }
            
            if (borneFin != null) {
                final Point borneFinGeom = borneFin.getGeometry();
                final double borneFinDistance = computeRelative(segments, new Point[]{tronconStart}, borneFinGeom).getValue();
                distanceFin += borneFinDistance;
            }
            
            if (borneDebut == null) {
                distanceDebut = distanceFin;
            } else if (borneFin == null) {
                distanceFin = distanceDebut;
            }
                        
            return cut(troncon, StrictMath.min(distanceDebut, distanceFin), StrictMath.max(distanceDebut, distanceFin));
        }
    }
    /**
     * Create a line string which begins on input line, from a certain distance after its beginning
     * to another further away.
     * @param linear The input {@link LineString} we want to extract a piece from.
     * @param distanceDebut Distance from the start of input line for the beginning of the new geometry.
     * @param distanceFin Distance from the start of input line for the end of the new geometry.
     * @return A line string, never null.
     */
    public static LineString cut(LineString linear, double distanceDebut, double distanceFin) {        
        //on s"assure de ne pas sortir du troncon
        final double tronconLength = linear.getLength();
        distanceDebut = XMath.clamp(Math.min(distanceDebut, distanceFin), 0, tronconLength);
        distanceFin = XMath.clamp(Math.max(distanceDebut, distanceFin), 0, tronconLength);

        //create du tracé de la structure le long du troncon
        final PathIterator ite = new JTSLineIterator(linear, null);
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
        JTS.setCRS(geom, InjectorCore.getBean(SessionCore.class).getProjection());
        
        return geom;
    }
    
    /**
     * Search which bornes of the given SR are enclosing given point. Input SR 
     * bornes are projected on given linear for the analysis.
     * 
     * @param sourceLinear The set of segments composing reference linear.
     * @param toGetBundaryFor The point for which we want enclosing bornes.
     * @param possibleBornes List of points in which we'll pick bounding bornes.
     */
    public void getBoundingBornes(SegmentInfo[] sourceLinear, final Point toGetBundaryFor, final Point... possibleBornes) {
        ProjectedPoint projectedPoint = projectReference(sourceLinear, toGetBundaryFor);
        if (projectedPoint.segment == null) throw new RuntimeException("Cannot project point on linear."); // TODO : better exception
        // We'll try to find bornes on the nearest possible segment.
        if (projectedPoint.segmentIndex < 0) throw new RuntimeException("Cannot project point on linear."); // TODO : better exception       
        for (final Point borne : possibleBornes) {
            ProjectedPoint projBorne = projectReference(sourceLinear, borne);
            if (projBorne.segmentIndex < 0) continue;
        }
        
    }
}
