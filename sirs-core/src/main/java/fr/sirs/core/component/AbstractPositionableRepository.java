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
package fr.sirs.core.component;

import com.vividsolutions.jts.geom.LineString;
import fr.sirs.core.*;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.ConvertPositionableCoordinates;
import fr.sirs.util.StreamingIterable;
import java.util.List;
import java.util.logging.Level;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A repository to access the view giving positionable objects by
 * {@link TronconDigue}.
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T> Type of object managed by this repository.
 */
public abstract class AbstractPositionableRepository<T extends Positionable> extends AbstractSIRSRepository<T> {

    @Autowired
    private SystemeReperageRepository srRepo;

    public AbstractPositionableRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
    }

    public List<T> getByLinear(final TronconDigue linear) {
        ArgumentChecks.ensureNonNull("Linear", linear);
        return this.getByLinearId(linear.getId());
    }

    public List<T> getByLinearId(final String linearId) {
        return cacheList(globalRepo.getByLinearId(type, linearId));
    }

    public StreamingIterable<T> getByLinearStreaming(final TronconDigue linear) {
        ArgumentChecks.ensureNonNull("Linear", linear);
        return this.getByLinearIdStreaming(linear.getId());
    }

    public StreamingIterable<T> getByLinearIdStreaming(final String linearId) {
        ArgumentChecks.ensureNonNull("Linear", linearId);
        return new StreamingViewIterable(globalRepo.createByLinearIdQuery(type, linearId));
    }

    @Override
    protected T onLoad(T loaded) {
        loaded = super.onLoad(loaded);
        boolean toSave = checkAndAdaptOnload(loaded);

        if (toSave) {
            try {
                update(loaded);
            } catch (RuntimeException re) {
                SirsCore.LOGGER.log(Level.WARNING, "Failed to update loaded element with id : "+loaded.getId(), re);
            }
        }

        return loaded;
    }

    protected boolean checkAndAdaptOnload(T loaded) {
        boolean toSave = false;
        try {
            if (loaded.getSystemeRepId() != null) {
                try {
                    srRepo.get(loaded.getSystemeRepId());
                } catch (DocumentNotFoundException e) {
                    // HACK redmine-8127 : Sometimes when removing a SR from a troncon, some positionables'SRs are not updated properly
                    // and keep pointing to the removed SR leading to a @DocumentNotFoundException.
                    final TronconDigue troncon = ConvertPositionableCoordinates.getTronconFromPositionable(loaded);

                    if (troncon != null && troncon.getSystemeRepDefautId() != null) {
                        loaded.setSystemeRepId(troncon.getSystemeRepDefautId());
                        toSave = true;
                        SirsCore.LOGGER.log(Level.INFO, "Système de repérage absent de la base de données pour le positionable " + loaded.getId() + ". Modification vers le système " +
                                "de repérage par défaut du Tronçon.");
                        // If there are geographical coords, we can force recomputing the linear coords from them.
                        if (loaded.getPositionDebut() != null && loaded.getPositionFin() != null) {
                            loaded.setBorneDebutId(null);
                            loaded.setBorneFinId(null);
                            loaded.setBorne_debut_distance(0);
                            loaded.setBorne_fin_distance(0);
                            loaded.setGeometryMode("COORD");
                            loaded.setEditedGeoCoordinate(true);
                            LineString geometry = LinearReferencingUtilities.buildGeometryFromGeo(troncon.getGeometry(), loaded.getPositionDebut(), loaded.getPositionFin());
                            loaded.setGeometry(geometry);
                            ConvertPositionableCoordinates.computePositionableLinearCoordinate(loaded);
                        }
                    } else {
                        SirsCore.LOGGER.log(Level.WARNING, "Système de repérage absent de la base de données pour le positionable " + loaded.getId() + ". " +
                                "Aucun système de repérage disponible sur le tronçon pour remplacer.");
                    }
                }
            } else {
                toSave = ConvertPositionableCoordinates.COMPUTE_MISSING_COORD.test(loaded);
            }
        } catch (RuntimeException e) {
            SirsCore.LOGGER.log(Level.WARNING, "Echec du calcul de coordonnées pour l'élément chargé : \n" + loaded.toString(), e);
        }

        if (loaded.getGeometry() == null) {
            ConvertPositionableCoordinates.updateGeometryAndPRs(loaded);
        }
        return toSave;
    }

}
