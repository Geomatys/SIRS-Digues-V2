/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.theme.ui.calculcoordinates;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.FXPositionableMode;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.referencing.LinearReferencing;

/**
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
public class ConvertPositionableCoordinates {

    //===============================
    //  Compute Geo from Linear.
    //===============================
    /**
     * Compute current positionable point using linear referencing information
     * defined in the form. Returned point is expressed with Database CRS.
     *
     * Méthode extraite de FXPositionableAbstractLinearMode.java
     *
     * @param distance
     * @param borneProperty
     * @param amont
     * @param positionable
     * @return The point computed from starting borne. If we cannot, we return
     * null.
     */
    public static Point computeGeoFromLinear(final Number distance,
            final BorneDigue borneProperty, final boolean amont, final Positionable positionable) {

//        final Positionable positionable = posProperty.get();
        final TronconDigue t = FXPositionableMode.getTronconFromPositionable(positionable);

        if (distance != null && borneProperty != null && t != null) {
            //calcul à partir des bornes
            final Point bornePoint = borneProperty.getGeometry();
            double dist = distance.doubleValue();
            if (amont) {
                dist *= -1;
            }
            return LinearReferencingUtilities.computeCoordinate(t.getGeometry(), bornePoint, dist, 0);
        } else {
            return null;
        }
    }

    public static void computePositionableGeometryAndCoord(Positionable positionableWithLinearCoord) {
        ArgumentChecks.ensureNonNull("Borne de début du Positionable", positionableWithLinearCoord.getBorneDebutId());
        ArgumentChecks.ensureNonNull("Borne de fin du Positionable", positionableWithLinearCoord.getBorneFinId());
        ArgumentChecks.ensureNonNull("Distance borne début du Positionable", positionableWithLinearCoord.getBorne_debut_distance());
        ArgumentChecks.ensureNonNull("Distance borne fin du Positionable", positionableWithLinearCoord.getBorne_fin_distance());

        final TronconDigue troncon = FXPositionableMode.getTronconFromPositionable(positionableWithLinearCoord);
        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
        final LineString geometry = LinearReferencingUtilities.buildGeometryFromBorne(troncon.getGeometry(), positionableWithLinearCoord, borneRepo);

        //sauvegarde de la geometrie
        positionableWithLinearCoord.geometryProperty().set(geometry);

        // Affectation des coordonnées calculées.
        positionableWithLinearCoord.setPositionDebut(geometry.getStartPoint());
        positionableWithLinearCoord.setPositionFin(geometry.getEndPoint());

        // On indique que les coordonnées Géographique du Positionable n'ont pas été éditées.
        positionableWithLinearCoord.setEditedGeoCoordinate(Boolean.FALSE);

    }

    //===============================
    //  Compute Linear from Geo
    //===============================
    public static LinearReferencing.SegmentInfo[] getSourceLinear(final SystemeReperage source, final Positionable positionable) {
        final TronconDigue t = FXPositionableMode.getTronconFromPositionable(positionable);
        return LinearReferencingUtilities.getSourceLinear(t, source);
    }
    //--------------------------------------------------------------------------

    /**
     * Calcul des coordonnées linéaires et mise à jour d'un Positionable à
     * partir de ses coordonnées Géométriques.
     *
     * @param sr : Systeme de repérage utilisé comme référence pour les
     * coordonnées linéaires.
     * @param positionableWithGeo : le Positionable pour lequel les coordonnées
     * linéaires seront mises à jour. Les attributs startPoint et endPoint de ce
     * Positionable doivent être renseignés.
     */
    public static void computePositionableLinearCoordinate(final SystemeReperage sr, final Positionable positionableWithGeo) {
        ArgumentChecks.ensureNonNull("Système de repérage", sr);
        ArgumentChecks.ensureNonNull("Positionable", positionableWithGeo);

        final Point startPoint = positionableWithGeo.getPositionDebut();
        final Point endPoint = positionableWithGeo.getPositionFin();

        if (startPoint == null || endPoint == null) {
            throw new IllegalArgumentException("The attributes 'positionDebut' and 'positionFin' of the method's Positionable input must be provided to compute the linear coordinates");
        }

        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);

        // Calcul des coordonnées linéaires du début du positionnable
        final LinearReferencing.SegmentInfo[] segments = getSourceLinear(sr, positionableWithGeo);
        Map.Entry<BorneDigue, Double> computedLinear = computeLinearFromGeo(segments, sr, startPoint);
        boolean aval = true;
        double distanceBorne = computedLinear.getValue();
        if (distanceBorne < 0) {
            distanceBorne = -distanceBorne;
            aval = false;
        }
        float computedPR = TronconUtils.computePR(segments, sr, startPoint, borneRepo);

        // Affectation des coordonnées linéaires calculées au Positionable :
        positionableWithGeo.setBorneDebutId(computedLinear.getKey().getId());
        positionableWithGeo.setBorne_debut_distance(distanceBorne);
        positionableWithGeo.setBorne_debut_aval(aval);
        positionableWithGeo.setPrDebut(computedPR);

        // Calcul des coordonnées linéaires du point de fin du positionnable si différent du points de départ.
        if (!startPoint.equals(endPoint)) {
            computedLinear = computeLinearFromGeo(segments, sr, endPoint);
            aval = true;
            distanceBorne = computedLinear.getValue();
            if (distanceBorne < 0) {
                distanceBorne = -distanceBorne;
                aval = false;
            }
            computedPR = TronconUtils.computePR(getSourceLinear(sr, positionableWithGeo), sr, endPoint, borneRepo);
        }

        // Affectation des coordonnées linéaires calculées au Positionable :
        positionableWithGeo.setBorneFinId(computedLinear.getKey().getId());
        positionableWithGeo.setBorne_fin_distance(distanceBorne);
        positionableWithGeo.setBorne_fin_aval(aval);
        positionableWithGeo.setPrFin(computedPR);

        // Mise à jour de l'identifiant du Système de représentation;
        positionableWithGeo.setSystemeRepId(sr.getId());

        if ((positionableWithGeo.getEditedGeoCoordinate() == null) || (!positionableWithGeo.getEditedGeoCoordinate())) {
            // On indique que les coordonnées Géographique du Positionable ont été éditées.
            positionableWithGeo.setEditedGeoCoordinate(Boolean.TRUE);
        }

    }

    /**
     * Compute a linear position for the edited {@link Positionable} using
     * defined geographic position.
     *
     * Méthode extraite de FXPositionableMode.java
     *
     * @param segments
     * @param sr The SR to use to generate linear position.
     * @param geoPoint
     * @return The borne to use as start point, and the distance from the borne
     * until the input geographic position. It's negative if we go from downhill
     * to uphill.
     *
     * @throws RuntimeException If the computing fails.
     */
    public static Map.Entry<BorneDigue, Double> computeLinearFromGeo(
            final LinearReferencing.SegmentInfo[] segments, final SystemeReperage sr, final Point geoPoint) {
        ArgumentChecks.ensureNonNull("Geographic point", geoPoint);

        if (segments == null) {
            throw new IllegalStateException("No computing can be done without a source linear object.");
        }

        // Get list of bornes which can be possibly used.
        final HashMap<Point, BorneDigue> availableBornes = getAvailableBornes(sr);
        final Point[] arrayGeom = availableBornes.keySet().toArray(new Point[0]);

        // Get nearest borne from our start geographic point.
        final Map.Entry<Integer, Double> computedRelative = LinearReferencingUtilities.computeRelative(segments, arrayGeom, geoPoint);
        final int borneIndex = computedRelative.getKey();
        if (borneIndex < 0 || borneIndex >= availableBornes.size()) {
            throw new RuntimeException("Computing failed : no valid borne found.");
        }
        final double foundDistance = computedRelative.getValue();
        if (Double.isNaN(foundDistance) || Double.isInfinite(foundDistance)) {
            throw new RuntimeException("Computing failed : no valid distance found.");
        }
        return new AbstractMap.SimpleEntry<>(availableBornes.get(arrayGeom[borneIndex]), foundDistance);
    }

    /**
     * Return valid bornes defined by the input {@link SystemeReperage} PRs
     * ({@link SystemeReperageBorne}). Only bornes containing a geometry are
     * returned.
     *
     * Méthode extraite de FXPositionableMode.java
     *
     * @param source The SR to extract bornes from.
     * @return A map, whose values are found bornes, and keys are their
     * associated geometry. Never null, but can be empty.
     */
    public static HashMap<Point, BorneDigue> getAvailableBornes(final SystemeReperage source) {
        ArgumentChecks.ensureNonNull("Système de repérage source", source);
        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
        final HashMap<Point, BorneDigue> availableBornes = new HashMap<>(source.systemeReperageBornes.size());
        for (final SystemeReperageBorne pr : source.systemeReperageBornes) {
            if (pr.getBorneId() != null) {
                final BorneDigue borne = borneRepo.get(pr.getBorneId());
                if (borne != null && borne.getGeometry() != null) {
                    availableBornes.put(borne.getGeometry(), borne);
                }
            }
        }
        return availableBornes;
    }

}
