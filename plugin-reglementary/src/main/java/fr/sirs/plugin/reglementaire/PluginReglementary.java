package fr.sirs.plugin.reglementaire;

import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.component.RappelObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RappelObligationReglementaire;
import fr.sirs.core.model.RefEcheanceRappelObligationReglementaire;
import fr.sirs.core.model.RefFrequenceObligationReglementaire;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.ui.AlertItem;
import fr.sirs.ui.AlertManager;
import javafx.scene.image.Image;
import org.apache.sis.util.logging.Logging;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Plugin correspondant au module réglementaire, permettant de gérer des documents de suivis.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class PluginReglementary extends Plugin {
    private static final String NAME = "plugin-reglementary";
    private static final String TITLE = "Module réglementaire";
    private static final Logger LOGGER = Logging.getLogger(PluginReglementary.class);

    public PluginReglementary() {
        name = NAME;
        loadingMessage.set("module réglementaire");
        themes.add(new DocumentsTheme());
        themes.add(new StatesGeneratorTheme());
    }

    /**
     * Chargement du plugin et vérification des alertes possibles à afficher.
     *
     * @throws Exception
     */
    @Override
    public void load() throws Exception {
        getConfiguration();
        showAlerts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    /**
     * Pas d'image pour ce plugin, image par défaut.
     *
     * @return {@code null}
     */
    @Override
    public Image getImage() {
        return null;
    }

    /**
     * Récupère les alertes à afficher pour l'utilisateur, selon les dates fournies dans les obligations réglementaires
     * et la fréquence de rappel.
     */
    public static void showAlerts() {
        AlertManager.getInstance().getAlerts().clear();
        final List<AlertItem> alerts = new ArrayList<>();

        final ObligationReglementaireRepository orr = Injector.getBean(ObligationReglementaireRepository.class);
        final List<ObligationReglementaire> obligations = orr.getAll();
        if (obligations.isEmpty()) {
            AlertManager.getInstance().addAlerts(alerts);
            return;
        }

        final RappelObligationReglementaireRepository rorr = Injector.getBean(RappelObligationReglementaireRepository.class);
        final AbstractSIRSRepository<RefEcheanceRappelObligationReglementaire> repoEcheanceRappel =
                Injector.getSession().getRepositoryForClass(RefEcheanceRappelObligationReglementaire.class);
        final AbstractSIRSRepository<RefFrequenceObligationReglementaire> repoFrequenceRappel =
                Injector.getSession().getRepositoryForClass(RefFrequenceObligationReglementaire.class);
        final AbstractSIRSRepository<RefTypeObligationReglementaire> repoTypeObl =
                Injector.getSession().getRepositoryForClass(RefTypeObligationReglementaire.class);
        final LocalDate now = LocalDate.now();

        for (final ObligationReglementaire obligation : obligations) {
            if (obligation.getEcheanceId() == null) {
                continue;
            }

            final RefEcheanceRappelObligationReglementaire echeance = repoEcheanceRappel.get(obligation.getEcheanceId());
            // Construction du texte à afficher sur le calendrier
            final StringBuilder sb = new StringBuilder();
            if (obligation.getTypeId() != null) {
                sb.append(repoTypeObl.get(obligation.getTypeId()).getAbrege()).append(" - ");
            }
            if (obligation.getSystemeEndiguementId() != null) {
                final Preview previewSE = Injector.getSession().getPreviews().get(obligation.getSystemeEndiguementId());
                sb.append(previewSE.getLibelle()).append(" - ");
            }
            sb.append(obligation.getAnnee());

            final LocalDate oblDate = obligation.getDateRealisation() != null ? obligation.getDateRealisation() :
                    obligation.getDateEcheance();
            if (oblDate == null) {
                continue;
            }

            // Compare la date actuelle avec la date d'échéance de l'obligation et le temps avant la date d'échéance
            // pour afficher l'alerte. Exemple : une obligation au 1er juillet avec un rappel 3 mois avant,
            // l'alerte sera affichée si la date du lancement de l'application est comprise dans cette période.
            if (oblDate.minusMonths(echeance.getNbMois()).compareTo(now) <= 0 && oblDate.compareTo(now) >= 0) {
                alerts.add(new AlertItem(sb.toString(), oblDate));
                continue;
            }

            // On doit maintenant vérifier la fréquence de répétition du rappel
            final List<RappelObligationReglementaire> rappels = rorr.getByObligation(obligation);
            if (rappels.isEmpty()) {
                continue;
            }
            for (final RappelObligationReglementaire rappel : rappels) {
                if (rappel.getFrequenceId() != null) {
                    LocalDate newOblDate = LocalDate.from(oblDate);
                    final RefFrequenceObligationReglementaire frequenceRappel = repoFrequenceRappel.get(rappel.getFrequenceId());
                    while (newOblDate.compareTo(now) <= 0) {
                        newOblDate = newOblDate.plusMonths(frequenceRappel.getNbMois());
                    }

                    // On a dépassé la date actuelle, on peut donc vérifier combien de mois avant la date de fin l'alerte doit
                    // être affichée.
                    if (newOblDate.minusMonths(echeance.getNbMois()).compareTo(now) < 0) {
                        alerts.add(new AlertItem(sb.toString(), oblDate));
                    }
                }
            }
        }

        AlertManager.getInstance().addAlerts(alerts);
    }
}
