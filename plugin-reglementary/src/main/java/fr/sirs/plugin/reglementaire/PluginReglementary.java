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
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
        showAlerts();
    }

    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    @Override
    public Image getImage() {
        // TODO: choisir une image pour ce plugin
        return null;
    }

    /**
     * Récupère les alertes à afficher pour l'utilisateur.
     */
    private void showAlerts() {
        final List<AlertItem> alerts = new ArrayList<>();

        final List<RappelObligationReglementaire> rappels = Injector.getSession().getRepositoryForClass(RappelObligationReglementaire.class).getAll();
        if (rappels != null && !rappels.isEmpty()) {
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
                final ObligationReglementaire obl;
                try {
                    obl = repoObl.get(rappel.getObligationId());
                } catch (RuntimeException ex) {
                    // Pourrait survenir si une obligation a été supprimée mais pas son rappel
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                    continue;
                }
                final RefEcheanceRappelObligationReglementaire period = repoEcheanceRappel.get(obl.getEcheanceId());

                final StringBuilder sb = new StringBuilder();
                sb.append(repoTypeObl.get(obl.getTypeId()).getAbrege()).append(" - ");
                final Preview previewSE = Injector.getSession().getPreviews().get(obl.getSystemeEndiguementId());
                sb.append(previewSE.getLibelle()).append(" - ").append(obl.getAnnee());

                if (obl.getDateEcheance().minusMonths(period.getNbMois()).compareTo(now) <= 0 && obl.getDateEcheance().compareTo(now) >= 0) {
                    alerts.add(new AlertItem(sb.toString(), obl.getDateEcheance()));
                    continue;
                }

                LocalDate newEcheanceDate = LocalDate.from(obl.getDateEcheance());
                final RefFrequenceObligationReglementaire frequenceRappel = repoFrequenceRappel.get(rappel.getFrequenceId());
                while (newEcheanceDate.compareTo(now) <= 0) {
                    newEcheanceDate = newEcheanceDate.plusMonths(frequenceRappel.getNbMois());
                }

                if (obl.getDateEcheance().minusMonths(period.getNbMois()).compareTo(now) < 0) {
                    alerts.add(new AlertItem(sb.toString(), obl.getDateEcheance()));
                }
            }
        }

        Injector.getSession().addAlerts(alerts);
    }
}
