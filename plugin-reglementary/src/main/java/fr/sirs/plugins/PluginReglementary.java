package fr.sirs.plugins;

import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.RappelObligationReglementaire;
import fr.sirs.core.model.RefEcheanceRappelObligationReglementaire;
import fr.sirs.ui.AlertItem;
import javafx.scene.image.Image;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin correspondant au module réglementaire, permettant de gérer des documents de suivis.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class PluginReglementary extends Plugin {
    private static final String NAME = "plugin-reglementary";
    private static final String TITLE = "Module réglementaire";

    public PluginReglementary() {
        name = NAME;
        loadingMessage.set("Chargement du module réglementaire");
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
            final LocalDateTime now = LocalDateTime.now();

            for (final RappelObligationReglementaire rappel : rappels) {
                final ObligationReglementaire obl = repoObl.get(rappel.getObligationId());
                final RefEcheanceRappelObligationReglementaire period = repoEcheanceRappel.get(obl.getPeriodeId());

                if (obl.getDateEcheance().minusMonths(period.getNbMois()).compareTo(now) < 0) {
                    alerts.add(new AlertItem(obl.getLibelle(), obl.getDateEcheance()));
                }
            }

            Injector.getSession().addAlerts(alerts);
        }
    }
}
