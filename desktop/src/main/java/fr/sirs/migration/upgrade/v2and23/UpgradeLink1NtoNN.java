/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.migration.upgrade.v2and23;

import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import static fr.sirs.core.SirsCore.LOGGER;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.model.Element;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.BackingStoreException;
import org.ektorp.DbAccessException;
import org.geotoolkit.internal.GeotkFX;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
public final class UpgradeLink1NtoNN extends Task {

//    private final AbstractSIRSRepository <? extends Element> repoSide1;
//    private final Map< Class, AbstractSIRSRepository<? extends Element>> reposSidesN;

    private final DatabaseRegistry dbRegistry;
    private final String dbName;
    private final Upgrades1NtoNNSupported upgrade;

    /**
     * Attribut représentant l'ensemble des échecs durant la mise à jour.
     * Consiste en une Map.
     * Les clefs sont les éléments portant initialement la relation mise à jour en échec.
     * Les valeurs sont les identifiants (String) des éléments associé à la clef
     * qui n'ont pas pu être mis à jour.
     *
     */
    private final UpgradeFailureReport failureReport = new UpgradeFailureReport();


    public UpgradeLink1NtoNN(final DatabaseRegistry dbRegistry, final String dbName, final Upgrades1NtoNNSupported upgrade){
        ArgumentChecks.ensureNonNull("dbRegistry", dbRegistry);
        ArgumentChecks.ensureNonNull("dbName", dbName);

        SirsCore.LOGGER.info(String.format("Initialisation de la mise à jours des relations 1-N en relation N-N pour la version : %d.%d ", upgrade.upgradeMajorVersion, +upgrade.upgradeMinorVersion));
        this.upgrade = upgrade;
        this.dbRegistry = dbRegistry;
        this.dbName = dbName;

    }


    @Override
    protected Object call() throws Exception {

        updateTitle(String.format("Mise à jours des relations 1-N en relation N-N pour la version : %d.%d ", upgrade.upgradeMajorVersion, upgrade.upgradeMinorVersion));

        updateMessage("Connection à la base de données");
        try (final ConfigurableApplicationContext upgradeContext = this.dbRegistry.connectToSirsDatabase(dbName, false, false, false)) {


            Session session = upgradeContext.getBean(Session.class);

            SirsCore.LOGGER.info("Récupération des répositories nécessaire à la mise à jour");

            //Récupération des répositories nécessaire à la mise à jour :
            //-----------------------------------------------------------
            AbstractSIRSRepository<? extends Element> repoSide1 = session.getRepositoryForClass(upgrade.linkSide1);

            Map< Class, AbstractSIRSRepository<Element>> reposSidesN = upgrade.linkSidesN.stream()
                    .map(u -> u.clazz)
                    .collect(Collectors.toMap(c -> c,
                            c -> session.getRepositoryForClass(c)
                    ));


            updateMessage("Parcours des éléments portant initialement les liens et mise à jour");

            /* Parcours des éléments portant initialement les liens
            * (exemple : si migration [Desordre 1..* Ouvrage] en [Desordre *..* Ouvrage]
            * => parcours de chacun des désordres). */
            repoSide1.getAllStreaming()
                    .forEach(d -> upgrade(upgrade, d, reposSidesN));

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
            final String message =
                  "\n\n====================================================="
                    + "Fin de la mise à jour; Lecture du rapport d'erreurs :"
                    + "=====================================================\n";
            LOGGER.log(Level.WARNING, message);
            LOGGER.log(Level.WARNING, failureReport.getStringReport());

        }

        return false;
    }

    private void upgrade(final Upgrades1NtoNNSupported upgrade, final Element element, final Map< Class, AbstractSIRSRepository<Element>> reposSidesN) {

        updateMessage(String.format("Element parcouru : %s   -> %s ", element.getClass().getCanonicalName(), element.getDesignation()));

        if (!(upgrade.linkSide1.isInstance(element))) {
            throw new IllegalArgumentException(" Element input must be an instance of the upgrade input");
        }

        try {
            for (ClassAndItsGetter classeAndGetter : upgrade.linkSidesN) {
                final List<String> extractedIds  = (List<String>) classeAndGetter.getter.invoke(element);
                final AbstractSIRSRepository<Element> repo = reposSidesN.get(classeAndGetter.clazz);
                final List<Element> updated = extractedIds.stream()
                        .map(
                                id -> addChildToElementFromId(element, id, repo) //return null if an exception occured when trying to add child
                        )
                        .filter(e -> e != null)
                        .collect(Collectors.toList());
                repo.executeBulk(updated);

            }
        } catch (Exception e) {
            throw new BackingStoreException(e.getCause());
        }
    }

    /**
     *
     * @param element to add as child
     * @param id id of the {@link Element} we want to add a child
     * @param repo repository allowing to retrive the {@link Element} associated with the id-input
     * @return the {@link Element} associated with the given input after adding the input {@linkplain Element element} Or null if an exception occured.
     * The null return aim to allow to filter the updated elements.
     */
    private Element addChildToElementFromId(final Element element, final String id, final AbstractSIRSRepository<Element> repo) {
        try {
            final Element elt = repo.get(id);
            elt.addChild(element);
            return elt;
        } catch (Exception e) {
            final String message = failureReport.addFailure(element,id);
            LOGGER.log(Level.WARNING, message, e);
            return null;
        }
    }

}
