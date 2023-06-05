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
package fr.sirs.plugin.reglementaire;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.RefHorodatageStatusRepository;
import fr.sirs.core.model.RefHorodatageStatus;
import fr.sirs.plugin.reglementaire.ui.*;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Panneau regroupant les fonctionnalités en lien avec les Registres.
 *
 * @author Estelle Idee (Geomatys)
 */
public final class RegistreTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            RegistreTheme.class.getResourceAsStream("images/gen_etats.png"));

    public RegistreTheme() {
        super("Registre", "Registre", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        // Check whether all @RefHorodatageStatus are available : Non horodaté, En attente, Horodaté.
        final String[] refNonTimeStampedStatus = new String[1];
        final String[] refWaitingStatus = new String[1];
        final String[] refTimeStampedStatus = new String[1];

        if (!checkRefHorodatageStatusAllPresent(refNonTimeStampedStatus, refWaitingStatus, refTimeStampedStatus)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(true);
            alert.getDialogPane().setMinWidth(650);
            alert.getDialogPane().setMinHeight(300);
            alert.setTitle("Statuts d'horodatage manquant");
            alert.setContentText("Les statuts d'horodatage suivant sont nécessaires au fonctionnement de la génération du tableau de synthèse des prestations : " +
                    "\n" +
                    "\n- Non horodaté" +
                    "\n- En attente" +
                    "\n- Horodaté" +
                    "\n\nVeuillez mettre à jour la table de référence avant de revenir au menu Registre.");

            alert.showAndWait();
            final Session session = Injector.getSession();
            session.getFrame().addTab(session.getOrCreateReferenceTypeTab(RefHorodatageStatus.class));
            throw new IllegalStateException("Missing timestamp status in " + RefHorodatageStatus.class.getSimpleName());
        }

        final BorderPane borderPane = new BorderPane();
        final TabPane tabPane = new TabPane();

        // Onglet to generate "Tableaux de Synthese" for Prestation.
        final Tab syntheseTab = new Tab("Tableaux de synthèse");
        syntheseTab.setClosable(false);

        syntheseTab.setContent(new HorodatageReportPane(refNonTimeStampedStatus[0], refWaitingStatus[0]));

        // Onglet to import timestamped "Tableaux de Synthèse" and create the final report with cover page and summary table.
        // TODO modify content to get a folder tree : SE -> year -> files
        final Tab gestionTab = new Tab("Gestion");
        gestionTab.setClosable(false);

        // Ajout des onglets
        tabPane.getTabs().add(syntheseTab);
        tabPane.getTabs().add(gestionTab);
        borderPane.setCenter(tabPane);
        return borderPane;
    }

    /**
     * Check that all the requested time stamped status are available in SIRS :
     * <ul>
     *     <li>Non horodaté</li>
     *     <li>En attente</li>
     *     <li>Horodaté</li>
     * </ul>
     * @return true if all the requested status are present.
     */
    private boolean checkRefHorodatageStatusAllPresent(final String[] refNonTimeStampedStatus, final String[] refWaitingStatus, final String[] refTimeStampedStatus) {
        final List<RefHorodatageStatus> allStatus = Injector.getBean(RefHorodatageStatusRepository.class).getAll();
        if (allStatus == null || allStatus.isEmpty())
            return false;


        allStatus.stream().forEach(status -> {
            String libelle = status.getLibelle();
            if ("Non horodaté".equalsIgnoreCase(libelle.trim()))
                refNonTimeStampedStatus[0] = status.getId();
            else if ("En attente".equalsIgnoreCase(libelle.trim()))
                refWaitingStatus[0] = status.getId();
            else if ("Horodaté".equalsIgnoreCase(libelle.trim()))
                refTimeStampedStatus[0] = status.getId();
        });
        return refNonTimeStampedStatus[0] != null && refWaitingStatus[0] != null && refTimeStampedStatus[0] != null;
    }

}
