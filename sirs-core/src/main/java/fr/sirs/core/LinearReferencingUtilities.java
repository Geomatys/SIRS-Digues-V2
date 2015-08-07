package fr.sirs.core;

import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.model.BorneDigue;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
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

    public static Comparator<SystemeReperageBorne> SR_BORNES_COMPARATOR = (SystemeReperageBorne b1, SystemeReperageBorne b2) -> {
        if (b1.getValeurPR() > b2.getValeurPR()) {
            return 1;
        } else if (b1.getValeurPR() < b2.getValeurPR()) {
            return -1;
        } else {
            return 0;
        }
    };

    /**
     * Builds a geometry for a target positionable that has prDebut, prFin and an SRId.
     * 
     * The result geometry is built using the geometry of a reference positionable.
     *
     * @param refPositionable The reference positionable (must have a geometry).
     * @param target The positionable to build a geometry for (must have a
     * prDebut, prFin and SRId (to make sense to PRs values). Note the PRs have
     * to be included in the reference positionable geometry !
     * @param repo
     * @param srRepo
     * @return
     */
    public static LineString buildSubGeometry(final Positionable refPositionable,
            final Positionable target,
            final AbstractSIRSRepository<BorneDigue> repo,
            final AbstractSIRSRepository<SystemeReperage> srRepo) {

        /*
         Situation exemple.
        
         À partir d'une géométrie de référence (issue du positionable de 
         référence), on souhaite construire une nouvelle géométrie délimitée par
         deux PRs.
        
        *
        *    segment 1   |          segment 2        |  3   | segment 4 |segment 5
        * ===============|===========================|======|===========|=========
        *
        *                  prDebut                               prFin
        *                     x----------------------|------|------x
        *
        */
        final LineString refPositionableLineString = asLineString(refPositionable.getGeometry());
        final float prDebut = target.getPrDebut();
        final float prFin = target.getPrFin();
        final String srId = target.getSystemeRepId();
        final SystemeReperage targetSR = srRepo.get(srId);

        final LinearReferencing.SegmentInfo[] segments = buildSegments(refPositionableLineString);

        // Le segment de début est le segment de géométrie sur lequel se trouve le point de début
        LinearReferencing.SegmentInfo segmentDebut = null, segmentFin = null;
        double prDebutSegmentDebut = 0.;
        double prFinSegmentDebut = 0.;
        double prDebutSegmentFin = 0.;
        double prFinSegmentFin = 0.;
        double distanceAlongLinear0 = 0.;
        double distanceAlongLinear1 = 0.;

        for (final LinearReferencing.SegmentInfo segment : segments) {

            final Point ptDebutSegment = GO2Utilities.JTS_FACTORY.createPoint(segment.segmentCoords[0]);
            final double prDebutSegment = TronconUtils.computePR(segments, targetSR, ptDebutSegment, repo);
            final Point ptFinSegment = GO2Utilities.JTS_FACTORY.createPoint(segment.segmentCoords[1]);
            final double prFinSegment = TronconUtils.computePR(segments, targetSR, ptFinSegment, repo);

            /*
             Si on n'a toujours pas trouvé le segment sur lequel se trouve le point de début :
             */
            if (segmentDebut == null) {

                // On vérifie si le segment courant contient le point de début
                // 1- Si oui, on le garde en mémoire, ainsi que ses PRs de début et de fin.
                if (((prDebut >= prDebutSegment && prDebut <= prFinSegment)
                        || (prDebut <= prDebutSegment && prDebut >= prFinSegment))) {
                    segmentDebut = segment;
                    prDebutSegmentDebut = prDebutSegment;
                    prFinSegmentDebut = prFinSegment;
                } // 2- Sinon, on incrémente la distance
                else {
                    distanceAlongLinear0 += segment.length;
                }
            }

            // Idem pour le segment sur lequel se trouve le point de fin.
            if (segmentFin == null) {

                if (((prFin >= prDebutSegment && prFin <= prFinSegment)
                        || (prFin <= prDebutSegment && prFin >= prFinSegment))) {
                    segmentFin = segment;
                    prDebutSegmentFin = prDebutSegment;
                    prFinSegmentFin = prFinSegment;
                } else {
                    distanceAlongLinear1 += segment.length;
                }
            }

            /*
             Si on a trouvé les segments sur lesquels se trouvent les points de
             début et de fin de la géométrie à construire, on peut sortir de la 
             boucle.
             */
            if (segmentFin != null && segmentDebut != null) {
                break;
            }
        }

        /*
         Si l'un des segments de début ou de fin est null en sortie de boucle,
         c'est que la géométrie à construire n'est pas incluse dans la géométrie
         de référence. On lance donc une exception pour le signaler.
         */
        if (segmentDebut == null || segmentFin == null) {
            throw new IllegalArgumentException("The geometry of the reference positionable must include the geometry that have to be built from the prs of the target positionable.");
        }

        /*
         À la fin de la boucle, on doit avoir les valeurs suivantes pour
         distanceAlongLinear0 et distanceAlongLinear1 :
        
        * 
        *    segment 1   |          segment 2        |  3   | segment 4 |segment 5
        * ===============|====X======================|======|======X====|=========
        *                  prDebut                               prFin
        * -------------->| distanceAlongLinear0
        * ------------------------------------------------->| distanceAlongLinear1
        *
         ________________________________________________________________________
        
         On doit également a voir les valeurs suivantes pour les PRs de début
         et de fin des segments de début et de fin :
        
         segmentDebut : segment2
         segmentFin   : segment4
        
        
        *
        *                |        segmentDebut       |      | segmentFin| 
        * ===============|===========================|======|===========|=========
        *                |                           |      |           |
        *                |                           |      |           |
        *                |                           |      |           x prFinSegmentFin
        *                |                           |      x prDebutSegmentFin
        *                |                           |
        *                |                           x prFinSegmentDebut
        *                x prDebutSegmentDebut  
        *
         ________________________________________________________________________
        
         Il faut maintenant mettre à jour les distancesAlongLinear de manière
        à les ajuster aux points des pr de début et de fin du positionable pour
        lequel on veut construire la géométrie.
        
        *
        *    segment 1   |          segment 2        |  3   | segment 4 |segment 5
        * ===============|====X======================|======|======X====|=========
        *                  prDebut                               prFin
        * ------------------->| distanceAlongLinear0
        * -------------------------------------------------------->| distanceAlongLinear1
        * 
        
        */
        
        
        
        distanceAlongLinear0 += (prDebut - prDebutSegmentDebut) / (prFinSegmentDebut - prDebutSegmentDebut);
        distanceAlongLinear1 += (prFin - prDebutSegmentFin) / (prFinSegmentFin - prDebutSegmentFin);

        /*
        Enfin, on découpe la géométrie du positionable de référence pour obtenir la nouvelle géométrie.
        
        *
        *    segment 1   |          segment 2        |  3   | segment 4 |segment 5
        * ===============|====X======================|======|======X====|=========
        *                  prDebut                               prFin
        * ------------------->| distanceAlongLinear0
        * -------------------------------------------------------->| distanceAlongLinear1
        *                     |                                    |
        *                     |                                    |
        *                     |                                    |
        * géométrie résultat: x======================|======|======x
        
        */
        return cut(refPositionableLineString, distanceAlongLinear0, distanceAlongLinear1);

    }

    /**
     * Create a JTS geometry for the input {@link Positionable}. Generated
     * geometry is a line string along an input geometry, whose beginning and
     * end are defined by geographic begin and end position in the
     * {@link Positionable}. If no valid point can be found, we will use its
     * start and end {@link BorneDigue}.
     *
     * @param tronconGeom The source geometry to follow when creating the new
     * one.
     * @param structure The object to generate a geometry for.
     * @param repo The {@link BorneDigueRepository} to use to retrieve input
     * {@link Positionable} bornes.
     * @return A line string for the given structure. Never null.
     */
    public static LineString buildGeometry(Geometry tronconGeom, Positionable structure, AbstractSIRSRepository<BorneDigue> repo) {

        final LineString tronconLineString = asLineString(tronconGeom);
        SegmentInfo[] segments = buildSegments(tronconLineString);

        Point positionDebut = structure.getPositionDebut();
        Point positionFin = structure.getPositionFin();
        if (positionDebut != null || positionFin != null) {
            ProjectedPoint refDebut = null, refFin = null;
            if (positionDebut != null) {
                refDebut = projectReference(segments, positionDebut);
            }
            if (positionFin != null) {
                refFin = projectReference(segments, positionFin);
            }
            if (refDebut == null) {
                refDebut = refFin;
            } else if (refFin == null) {
                refFin = refDebut;
            }

            return cut(tronconLineString, refDebut.distanceAlongLinear, refFin.distanceAlongLinear);

        } else {
            //reconstruction a partir de bornes et de distances
            final BorneDigue borneDebut = (structure.getBorneDebutId() != null) ? repo.get(structure.getBorneDebutId()) : null;
            final BorneDigue borneFin = (structure.getBorneFinId() != null) ? repo.get(structure.getBorneFinId()) : null;
            if (borneDebut == null && borneFin == null) {
                //aucune borne définie, on ne peut pas calculer la géométrie
                return null;
            }

            double distanceDebut = structure.getBorne_debut_distance();
            double distanceFin = structure.getBorne_fin_distance();
            //on considére que les troncons sont numérisé dans le sens amont vers aval.
            if (structure.getBorne_debut_aval()) {
                distanceDebut *= -1.0;
            }
            if (structure.getBorne_fin_aval()) {
                distanceFin *= -1.0;
            }

            //calcul de la distance des bornes. Il peut y avoir qu'une seule borne définie dans le cas d'un ponctuel.
            final Point tronconStart = GO2Utilities.JTS_FACTORY.createPoint(tronconLineString.getCoordinates()[0]);
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

            return cut(tronconLineString, StrictMath.min(distanceDebut, distanceFin), StrictMath.max(distanceDebut, distanceFin));
        }
    }

    /**
     * Create a line string which begins on input line, from a certain distance
     * after its beginning to another further away.
     *
     * @param linear The input {@link LineString} we want to extract a piece
     * from.
     * @param distanceDebut Distance from the start of input line for the
     * beginning of the new geometry.
     * @param distanceFin Distance from the start of input line for the end of
     * the new geometry.
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
        walker.walk((float) distanceDebut);
        float remain = (float) (distanceFin - distanceDebut);

        final List<Coordinate> structureCoords = new ArrayList<>();
        Point2D point = walker.getPosition(null);
        structureCoords.add(new Coordinate(point.getX(), point.getY()));

        while (!walker.isFinished() && remain > 0) {
            final float advance = Math.min(walker.getSegmentLengthRemaining(), remain);
            remain -= advance;
            walker.walk(advance);
            point = walker.getPosition(point);
            structureCoords.add(new Coordinate(point.getX(), point.getY()));
        }

        if (structureCoords.size() == 1) {
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
        if (projectedPoint.segment == null) {
            throw new RuntimeException("Cannot project point on linear."); // TODO : better exception
        }        // We'll try to find bornes on the nearest possible segment.
        if (projectedPoint.segmentIndex < 0) {
            throw new RuntimeException("Cannot project point on linear."); // TODO : better exception       
        }
        for (final Point borne : possibleBornes) {
            ProjectedPoint projBorne = projectReference(sourceLinear, borne);
            if (projBorne.segmentIndex < 0) {
                continue;
            }
        }

    }
}
