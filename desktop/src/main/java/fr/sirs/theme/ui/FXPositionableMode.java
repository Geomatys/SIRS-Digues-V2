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
package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.referencing.LinearReferencing;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface FXPositionableMode {

    /**
     * ID du mode d'édition. Cette valeur est stoquée dans le champ geometryMode
     * du positionable.
     *
     * @return identifiant unique
     */
    String getID();

    /**
     * Nom du mode d'édition.
     *
     * @return
     */
    String getTitle();

    Node getFXNode();

    ObjectProperty<Positionable> positionableProperty();

    BooleanProperty disablingProperty();

    default List<Node> getExtraButton() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Méthode de mise à jour des champs.
     *
     * Cette méthode doit permettre aux champs de rester à jour lorsque l'
     * information géométrique du positionable est modifiée.
     *
     * Mais elle n'est pas chargée elle-même de modifier l'information
     * géométrique.
     */
    void updateFields();

    /**
     * Méthode de mise à jour de la géométrie.
     *
     * Cette méthode doit permettre à la géométrie de rester à jour avec les
     * champs. Elle doit pour cela veiller que la modification des champs ait
     * bien eu lieu depuis le mode courant car sinon des modifications en boucle
     * risquent de se produire, par exemple :
     *
     * Modification d'un champ dans le mode A
     * <ul>
     * <li> mise à jour de la géométrie </li>
     * <li> modification d'un champ dans le mode B pour préserver la cohérence
     * de l'affichage avec la géométrie modifiée depuis le mode A.</li>
     * <li> mise à jour de la géométrie (si on n'a pas vérifié qu'on n'est pas
     * en mode B)</li>
     * <li> modification d'un champ dans le mode A pour présernver la cohérence
     * de l'affichage avec la géométrie modifiée depuis le mode B.</li>
     * <li> etc.</li>
     * </ul>
     */
    void buildGeometry();

    /**
     * Search recursively the troncon of the positionable.
     *
     * @param pos Positionable object to find parent linear.
     * @return Found linear object, or null if we cannot deduce it from input.
     */
    public static TronconDigue getTronconFromPositionable(final Positionable pos) {
        final Element currentElement = getTronconFromElement(pos);
        if (currentElement instanceof TronconDigue) {
            return (TronconDigue) currentElement;
        } else {
            return null;
        }
    }

//    // Méthodes déplacées vers ConvertPositionableCoordinates puis adaptées mais ne semble jamais appelées.
//    // A supprimer définitivement si certitude qu'il n'y a pas de perte d'intégrité.
//    
//    /**
//     * Compute a linear position for the edited {@link Positionable} using defined
//     * geographic position.
//     *
//     * @param segments
//     * @param sr The SR to use to generate linear position.
//     * @param geoPoint
//     * @return The borne to use as start point, and the distance from the borne
//     * until the input geographic position. It's negative if we go from downhill
//     * to uphill.
//     *
//     * @throws RuntimeException If the computing fails.
//     */
//    public static Map.Entry<BorneDigue, Double> computeLinearFromGeo(
//            final LinearReferencing.SegmentInfo[] segments, final SystemeReperage sr, final Point geoPoint) {
//        ArgumentChecks.ensureNonNull("Geographic point", geoPoint);
//
//        if (segments == null) throw new IllegalStateException("No computing can be done without a source linear object.");
//
//        // Get list of bornes which can be possibly used.
//        final HashMap<Point, BorneDigue> availableBornes = getAvailableBornes(sr);
//        final Point[] arrayGeom = availableBornes.keySet().toArray(new Point[0]);
//
//        // Get nearest borne from our start geographic point.
//        final Map.Entry<Integer, Double> computedRelative = LinearReferencingUtilities.computeRelative(segments, arrayGeom, geoPoint);
//        final int borneIndex = computedRelative.getKey();
//        if (borneIndex < 0 || borneIndex >= availableBornes.size()) {
//            throw new RuntimeException("Computing failed : no valid borne found.");
//        }
//        final double foundDistance = computedRelative.getValue();
//        if (Double.isNaN(foundDistance) || Double.isInfinite(foundDistance)) {
//            throw new RuntimeException("Computing failed : no valid distance found.");
//        }
//        return new AbstractMap.SimpleEntry<>(availableBornes.get(arrayGeom[borneIndex]), foundDistance);
//    }
//
//    /**
//     * Return valid bornes defined by the input {@link SystemeReperage} PRs ({@link SystemeReperageBorne}).
//     * Only bornes containing a geometry are returned.
//     * @param source The SR to extract bornes from.
//     * @return A map, whose values are found bornes, and keys are their associated geometry. Never null, but can be empty.
//     */
//    public static HashMap<Point, BorneDigue> getAvailableBornes(final SystemeReperage source) {
//        ArgumentChecks.ensureNonNull("Système de repérage source", source);
//        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
//        final HashMap<Point, BorneDigue> availableBornes = new HashMap<>(source.systemeReperageBornes.size());
//        for (final SystemeReperageBorne pr : source.systemeReperageBornes) {
//            if (pr.getBorneId() != null) {
//                final BorneDigue borne = borneRepo.get(pr.getBorneId());
//                if (borne != null && borne.getGeometry() != null) {
//                    availableBornes.put(borne.getGeometry(), borne);
//                }
//            }
//        }
//        return availableBornes;
//    }
    /**
     *
     * @param element
     * @return
     */
    public static Element getTronconFromElement(final Element element) {
        Element candidate = null;

        // Si on arrive sur un Troncon, on renvoie le troncon.
        if (element instanceof TronconDigue) {
            candidate = element;
        } // Sinon on cherche un troncon dans les parents
        else {
            // On privilégie le chemin AvecForeignParent
            if (element instanceof AvecForeignParent) {
                String id = ((AvecForeignParent) element).getForeignParentId();
                final Session session = Injector.getSession();
                final Preview preview = session.getPreviews().get(id);
                if (preview == null) {
                    return null;
                }

                final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForType(preview.getElementClass());
                candidate = getTronconFromElement((Element) repo.get(id));
            }
            // Si on n'a pas (ou pas trouvé) de troncon via la référence ForeignParent on cherche via le conteneur
            if (candidate == null && element.getParent() != null) {
                candidate = getTronconFromElement(element.getParent());
            }
        }
        return candidate;
    }

    /**
     *
     * @param spinnerNumber
     * @return
     */
    public static double fxNumberValue(final ObjectProperty<Double> spinnerNumber) {
        if (spinnerNumber.get() == null) {
            return 0;
        }
        return spinnerNumber.get();
    }
}
