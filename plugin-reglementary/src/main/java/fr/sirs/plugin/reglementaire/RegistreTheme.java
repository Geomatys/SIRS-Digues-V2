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
import fr.sirs.core.component.EtapeObligationReglementaireRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.plugin.reglementaire.ui.*;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.util.SimpleFXEditMode;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

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

        final BorderPane borderPane = new BorderPane();
        final TabPane tabPane = new TabPane();

        // Onglet to generate "Tableaux de Synthese" for Prestation.
        final Tab syntheseTab = new Tab("Tableaux de synthèse");
        syntheseTab.setClosable(false);
        syntheseTab.setContent(new HorodatageReportPane());

        // Onglet to import timestamped "Tableaux de Synthèse" and create the final report with cover page and summary table.
        // TODO modify content to get a folder tree : SE -> year -> files
        final Tab gestionTab = new Tab("Gestion");
        gestionTab.setClosable(false);
//        // Gestion du bouton consultation / édition pour la pojo table
//        final Separator separatorEtape = new Separator();
//        separatorEtape.setVisible(false);
//        final SimpleFXEditMode editEtapeMode = new SimpleFXEditMode();
//        final HBox topEtapePane = new HBox(separatorEtape, editEtapeMode);
//        HBox.setHgrow(separatorEtape, Priority.ALWAYS);
//        final EtapeObligationReglementaireRepository eorrRepo = Injector.getBean(EtapeObligationReglementaireRepository.class);
//        final ObservableList<EtapeObligationReglementaire> allEtapes = FXCollections.observableList(eorrRepo.getAll());
//        // Ajoute un listener sur tous les ajouts/suppression d'étapes d'obligations pour mettre à jour la liste et donc la table.
////        final DocumentsTheme.ListDocumentListener<EtapeObligationReglementaire> etapeListener =
////                new DocumentsTheme.ListDocumentListener<>(EtapeObligationReglementaire.class, allEtapes);
////        Injector.getBean(DocumentChangeEmiter.class).addListener(etapeListener);
//        final EtapesPojoTable etapesPojoTable = new EtapesPojoTable(tabPane, (ObjectProperty<? extends Element>) null);
//        etapesPojoTable.setTableItems(() -> (ObservableList) allEtapes);
//        etapesPojoTable.editableProperty().bind(editEtapeMode.editionState());
//        gestionTab.setContent(new BorderPane(etapesPojoTable, topEtapePane, null, null, null));

        // Ajout des onglets
        tabPane.getTabs().add(syntheseTab);
        tabPane.getTabs().add(gestionTab);
        borderPane.setCenter(tabPane);
        return borderPane;
    }

}
