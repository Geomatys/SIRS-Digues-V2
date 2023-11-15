/**
 *
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */

package fr.sirs.migration.upgrade.v2and23;

import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.model.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.DbAccessException;
import org.geotoolkit.internal.GeotkFX;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static fr.sirs.core.SirsCore.LOGGER;

/**
 * Hack-redmine: 6449
 * Update the link between EvenementHydraulique and Mesure hydraulique (MonteeEaux, LaisseCrue, LigneEau)
 * as up to SIRS version 2.46, the link was set in the mesure only and not in the EvenementHydraulique.
 *
 * @author Estelle Idée (Geomatys)
 */
public class UpgradeEvenementHydrauliqueLinkWithMesureH extends Task {

    private final DatabaseRegistry dbRegistry;
    private final String dbName;
    final int upgradeMajorVersion, upgradeMinorVersion;
    /**
     * Attribut représentant l'ensemble des échecs durant la mise à jour.
     * Consiste en une Map. Les clefs sont les éléments portant initialement la
     * relation mise à jour en échec. Les valeurs sont les identifiants (String)
     * des éléments associé à la clef qui n'ont pas pu être mis à jour.
     *
     */
    private final UpgradeFailureReport failureReport = new UpgradeFailureReport();

    public UpgradeEvenementHydrauliqueLinkWithMesureH(final DatabaseRegistry dbRegistry, final String dbName, final int upgradeMajorVersion, final int upgradeMinorVersion) {
        ArgumentChecks.ensureNonNull("dbRegistry", dbRegistry);
        ArgumentChecks.ensureNonNull("dbName", dbName);

        SirsCore.LOGGER.info(String.format("Initialisation de la mise à jour des liens entre Evenements Hydrauliques et Mesures hydrauliques pour la version : %d.%d ",upgradeMajorVersion, upgradeMinorVersion));
        this.dbRegistry = dbRegistry;
        this.dbName = dbName;
        this.upgradeMajorVersion=upgradeMajorVersion;
        this.upgradeMinorVersion=upgradeMinorVersion;

    }

    @Override
    protected Object call() throws Exception {

        updateTitle(String.format("Mise à jour des liens entre Evenements Hydrauliques et Mesures hydrauliques pour la version : %d.%d ", upgradeMajorVersion, upgradeMinorVersion));

        updateMessage("Connection à la base de données");
        try (final ConfigurableApplicationContext upgradeContext = this.dbRegistry.connectToSirsDatabase(dbName, false, false, false)) {

            Session session = upgradeContext.getBean(Session.class);

            SirsCore.LOGGER.info("Récupération des répositories nécessaires à la mise à jour");

            //Récupération des répositories nécessaires à la mise à jour :
            //-----------------------------------------------------------
            final AbstractSIRSRepository<EvenementHydraulique> ehRepository = session.getRepositoryForClass(EvenementHydraulique.class);
            final AbstractSIRSRepository<MonteeEaux> monteeEauRepository = session.getRepositoryForClass(MonteeEaux.class);
            final AbstractSIRSRepository<LaisseCrue> laisseCrueRepository = session.getRepositoryForClass(LaisseCrue.class);
            final AbstractSIRSRepository<LigneEau> ligneEauRepository = session.getRepositoryForClass(LigneEau.class);

            if (ehRepository == null) {
                SirsCore.LOGGER.info("Echec de la récupération du répository des EvenementHydraulique.");
                return false;
            } else if (monteeEauRepository == null) {
                SirsCore.LOGGER.info("Echec de la récupération du répository des MonteeEaux.");
                return false;
            } else if (laisseCrueRepository == null) {
                SirsCore.LOGGER.info("Echec de la récupération du répository des LaisseCrue.");
                return false;
            } else if (ligneEauRepository == null) {
                SirsCore.LOGGER.info("Echec de la récupération du répository des LigneEau.");
                return false;
            }

            updateMessage("Parcours des MonteeEaux.");

            final Map<String, EvenementHydraulique> ehToUpdateMap = new HashMap<>();
            update(monteeEauRepository, ehToUpdateMap, ehRepository);
            update(ligneEauRepository, ehToUpdateMap, ehRepository);
            update(laisseCrueRepository, ehToUpdateMap, ehRepository);

            updateMessage("Sauvegarde en base des Prestations modifiées");
            ehRepository.executeBulk(ehToUpdateMap.values());

            return true;

        } catch (DbAccessException ex) {
            LOGGER.log(Level.WARNING, "Problème d'accès au CouchDB, utilisateur n'ayant pas les droits administrateur.", ex);
            Platform.runLater(() -> GeotkFX.newExceptionDialog("L'utilisateur de la base CouchDB n'a pas les bons droits. "
                    + "Réinstaller CouchDB ou supprimer cet utilisateur \"geouser\" des administrateurs de CouchDB, "
                    + "puis relancer l'application.", ex).showAndWait());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            Platform.runLater(() -> GeotkFX.newExceptionDialog("Une erreur est survenue pendant l'import de la base.", ex).showAndWait());

        } finally {
            updateMessage("Fin de la mise à jour; Lecture du rapport d'erreurs");
            final String message
                    = "\n\n====================================================="
                    + "Fin de la mise à jour; Lecture du rapport d'erreurs :"
                    + "=====================================================\n";
            LOGGER.log(Level.WARNING, message);
            LOGGER.log(Level.WARNING, failureReport.getStringReport());

        }

        return false;
    }

    private void update(final AbstractSIRSRepository<? extends AvecEvenementHydraulique> mesureRepo, final Map<String, EvenementHydraulique> ehToUpdateMap,
                        final AbstractSIRSRepository<EvenementHydraulique> ehRepository) throws NoSuchMethodException {
        ArgumentChecks.ensureNonNull("mesureRepo", mesureRepo);
        ArgumentChecks.ensureNonNull("ehToUpdateMap", ehToUpdateMap);
        ArgumentChecks.ensureNonNull("ehRepository", ehRepository);

        final Class<? extends AvecEvenementHydraulique> mesureClass = mesureRepo.getModelClass();
        final Method getMesureIds = EvenementHydraulique.class.getMethod("get" + mesureClass.getSimpleName() + "Ids");

        final List<? extends AvecEvenementHydraulique> allMesures = mesureRepo.getAll();
        for (AvecEvenementHydraulique mesure : allMesures) {
            if (mesure != null) {
                final String elementInfo = String.format(mesureClass + " parcourue : %s   -> %s ", mesureClass.getCanonicalName(), mesure.getDesignation());
                updateMessage(elementInfo);
                final String evenementHydrauliqueId = mesure.getEvenementHydrauliqueId();
                if (evenementHydrauliqueId != null) {
                    final EvenementHydraulique eh;
                    if (!ehToUpdateMap.containsKey(evenementHydrauliqueId)) {
                        eh = ehRepository.get(evenementHydrauliqueId);
                        if (eh == null) return;
                        ehToUpdateMap.put(evenementHydrauliqueId, eh);
                    } else {
                        eh = ehToUpdateMap.get(evenementHydrauliqueId);
                    }
                    try {
                        final Object invoke = getMesureIds.invoke(eh);
                        if (invoke instanceof ObservableList) {
                            final ObservableList<String> list = (ObservableList<String>) invoke;
                            final String mesureId = mesure.getId();
                            if (!list.contains(mesureId)) {
                                list.add(mesureId);
                            }
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException("Error while using method " + getMesureIds + " on class " + eh.getClass());
                    } catch (RuntimeException e) {
                        final String message = failureReport.addFailure(mesure, evenementHydrauliqueId);
                        LOGGER.log(Level.WARNING, message, e);
                    }
                }
            }
        }
    }

}
