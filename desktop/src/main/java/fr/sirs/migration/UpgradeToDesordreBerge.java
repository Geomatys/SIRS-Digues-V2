/**
 *
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

package fr.sirs.migration;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.GlobalRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.*;
import fr.sirs.migration.upgrade.v2and23.UpgradeFailureReport;
import fr.sirs.migration.upgrade.v2and23.UpgradeLink1NtoNN;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.geotoolkit.internal.GeotkFX;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static fr.sirs.core.SirsCore.LOGGER;

/**
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
public class UpgradeToDesordreBerge extends Task {

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

    public UpgradeToDesordreBerge(final DatabaseRegistry dbRegistry, final String dbName, final int upgradeMajorVersion, final int upgradeMinorVersion) {
        ArgumentChecks.ensureNonNull("dbRegistry", dbRegistry);
        ArgumentChecks.ensureNonNull("dbName", dbName);

        SirsCore.LOGGER.info(String.format("Initialisation de la migration des Désordres sur des Berges vers des DesordreBerge pour la version : %d.%d ",
                upgradeMajorVersion, upgradeMinorVersion));
        this.dbRegistry = dbRegistry;
        this.dbName = dbName;
        this.upgradeMajorVersion=upgradeMajorVersion;
        this.upgradeMinorVersion=upgradeMinorVersion;

    }

    @Override
    protected Object call() throws Exception {

        updateTitle(String.format("Migration des Désordres sur des Berges vers des DesordreBerge pour la version : %d.%d ", upgradeMajorVersion, upgradeMinorVersion));

        updateMessage("Connection à la base de données");
        try (final ConfigurableApplicationContext upgradeContext = this.dbRegistry.connectToSirsDatabase(dbName, false, false, false)) {

            Session session = upgradeContext.getBean(Session.class);

            SirsCore.LOGGER.info("Récupération des répositories nécessaire à la mise à jour");

            //Récupération des répositories nécessaires à la mise à jour :
            //-----------------------------------------------------------
            final AbstractSIRSRepository<Desordre> desordreRepository = session.getRepositoryForClass(Desordre.class);
            final Previews previewRepo = session.getPreviews();

            if (desordreRepository == null) {
                SirsCore.LOGGER.info("Echec de la récupération du répository des Desordres.");
                return false;
            }

            updateMessage("Parcours des Désordres et tri de ceux qui appartiennent à des berges.");
            final List<Desordre> desordres = desordreRepository.getAll();

            final List<Element> desordresOnBerge = new ArrayList<>();
            Element e;
            for (int i = 0; i < desordres.size(); i++) {
                e = desordres.get(i);
                String linearId = ((AvecForeignParent)e).getForeignParentId();
                if (linearId == null) continue;
                Preview linear;
                try {
                    linear = Injector.getBean(Session.class).getPreviews().get(linearId);
                } catch (DocumentNotFoundException ex) {
                    continue;
                }
                if (linear.getElementClass().equals("fr.sirs.core.model.Berge"))
                    desordresOnBerge.add(e);

            }
//
//            desordresOnBerge.forEach(d -> {
//                d.getClass().
//            });

            final List<Prestation> prestationsToUpdate = new ArrayList<>();

            // Parcours des Prestations et contrôle de leurs coordonnées.
//            repository.getAllStreaming()
//                    .forEach(prestation -> {
//                        if (prestation != null) {
//                            final String elementInfo = String.format("Prestation parcourue : %s   -> %s ", prestation.getClass().getCanonicalName(), prestation.getDesignation());
//                            updateMessage(elementInfo);
//                            if (UpgradeLink1NtoNN.tryUpgradeCoordinates(prestation, String.format("Prestation : ", prestation))) {
//                                prestationsToUpdate.add(prestation);
//                            }
//                        }
//                    });
//
//            updateMessage("Sauvegarde en base des Prestations modifiées");
//            repository.executeBulk(prestationsToUpdate);

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




}