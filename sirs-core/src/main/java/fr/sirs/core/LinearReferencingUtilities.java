
package fr.sirs.core;

import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Objet;
import fr.sirs.util.json.GeometryDeserializer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
public final class LinearReferencingUtilities extends LinearReferencing{
    
    public static LineString buildGeometry(Geometry tronconGeom, Objet structure, BorneDigueRepository repo){
        
        final LineString troncon = asLineString(tronconGeom);
        SegmentInfo[] segments = buildSegments(troncon);
        
        Point positionDebut = structure.getPositionDebut();
        Point positionFin = structure.getPositionFin();
        if (positionDebut != null || positionFin != null) {
            ProjectedReference refDebut = null, refFin = null;
            if (positionDebut != null) refDebut = projectReference(segments, positionDebut);
            if (positionFin != null) refFin = projectReference(segments, positionFin);
            if (refDebut == null) refDebut = refFin;
            else if (refFin == null) refFin = refDebut;
            
            return cut(troncon, refDebut.distanceAlongLinear, refFin.distanceAlongLinear);
            
        } else {
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
            final double borneDebutDistance = calculateRelative(segments, new Point[]{tronconStart}, borneDebutGeom).getValue()[0];
            final double borneFinDistance = calculateRelative(segments, new Point[]{tronconStart}, borneFinGeom).getValue()[0];
            
            //conversion des distances au borne en distance par rapport au debut du troncon
            distanceDebut += borneDebutDistance;
            distanceFin += borneFinDistance;
            
            //on s'assure de l'ordre croissant des positions
            if(distanceDebut>distanceFin){
                double temp = distanceDebut;
                distanceDebut = distanceFin;
                distanceFin = temp;
            }
            
            return cut(troncon, distanceDebut, distanceFin);
        }
    }
        
    public static LineString cut(LineString linear, double distanceDebut, double distanceFin){
        
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
        JTS.setCRS(geom, GeometryDeserializer.PROJECTION);
        return geom;
    }
    
}
