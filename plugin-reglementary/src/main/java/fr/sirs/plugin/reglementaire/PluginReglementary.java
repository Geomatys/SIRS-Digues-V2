package fr.sirs.plugin.reglementaire;

import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RappelObligationReglementaire;
import fr.sirs.core.model.RefEcheanceRappelObligationReglementaire;
import fr.sirs.core.model.RefFrequenceObligationReglementaire;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.ui.AlertItem;
import javafx.scene.image.Image;
import org.apache.sis.util.logging.Logging;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
        themes.add(new TemplatesTheme());
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
    private void showAlerts() {
        final List<AlertItem> alerts = new ArrayList<>();

        final List<RappelObligationReglementaire> rappels = Injector.getSession().getRepositoryForClass(RappelObligationReglementaire.class).getAll();
        if (rappels != null && !rappels.isEmpty()) {
            // Des rappels existent, il faut les analyser pour savoir si une alerte doit être affichée ou non.
            final AbstractSIRSRepository<ObligationReglementaire> repoObl =
                    Injector.getSession().getRepositoryForClass(ObligationReglementaire.class);
            final AbstractSIRSRepository<RefEcheanceRappelObligationReglementaire> repoEcheanceRappel =
                    Injector.getSession().getRepositoryForClass(RefEcheanceRappelObligationReglementaire.class);
            final AbstractSIRSRepository<RefFrequenceObligationReglementaire> repoFrequenceRappel =
                    Injector.getSession().getRepositoryForClass(RefFrequenceObligationReglementaire.class);
            final AbstractSIRSRepository<RefTypeObligationReglementaire> repoTypeObl =
                    Injector.getSession().getRepositoryForClass(RefTypeObligationReglementaire.class);
            final LocalDate now = LocalDate.now();

            for (final RappelObligationReglementaire rappel : rappels) {
                final ObligationReglementaire obligation;
                try {
                    obligation = repoObl.get(rappel.getObligationId());
                } catch (RuntimeException ex) {
                    // Pourrait survenir si une obligation a été supprimée mais pas son rappel
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                    continue;
                }

                if (obligation.getEcheanceId() != null) {
                    final RefEcheanceRappelObligationReglementaire period = repoEcheanceRappel.get(obligation.getEcheanceId());

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
                    if (oblDate.minusMonths(period.getNbMois()).compareTo(now) <= 0 && oblDate.compareTo(now) >= 0) {
                        alerts.add(new AlertItem(sb.toString(), oblDate));
                        continue;
                    }

                    // On doit maintenant vérifier la fréquence de répétition du rappel
                    if (rappel.getFrequenceId() != null) {
                        LocalDate newOblDate = LocalDate.from(oblDate);
                        final RefFrequenceObligationReglementaire frequenceRappel = repoFrequenceRappel.get(rappel.getFrequenceId());
                        while (newOblDate.compareTo(now) <= 0) {
                            newOblDate = newOblDate.plusMonths(frequenceRappel.getNbMois());
                        }

                        // On a dépassé la date actuelle, on peut donc vérifier combien de mois avant la date de fin l'alerte doit
                        // être affichée.
                        if (newOblDate.minusMonths(period.getNbMois()).compareTo(now) < 0) {
                            alerts.add(new AlertItem(sb.toString(), oblDate));
                        }
                    }
                }
            }
        }

        Injector.getSession().addAlerts(alerts);
    }
}
