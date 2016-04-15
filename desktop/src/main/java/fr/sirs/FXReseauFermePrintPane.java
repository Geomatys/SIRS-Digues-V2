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
package fr.sirs;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObservationReseauHydrauliqueFerme;
import fr.sirs.core.model.RefConduiteFermee;
import fr.sirs.core.model.RefUrgence;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReseauFermePrintPane extends TemporalTronconChoicePrintPane {

    @FXML private Tab uiConduiteTypeChoice;
    @FXML private Tab uiUrgenceTypeChoice;

    @FXML private CheckBox uiOptionPhoto;
    @FXML private CheckBox uiOptionReseauOuvrage;

    private final TypeChoicePojoTable conduiteTypesTable = new TypeChoicePojoTable(RefConduiteFermee.class, "Types de conduites fermées");
    private final TypeChoicePojoTable urgenceTypesTable = new TypeChoicePojoTable(RefUrgence.class, "Types d'urgences");

    public FXReseauFermePrintPane(){
        super(FXReseauFermePrintPane.class);
        conduiteTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefConduiteFermee.class).getAll()));
        conduiteTypesTable.commentAndPhotoProperty().set(false);
        uiConduiteTypeChoice.setContent(conduiteTypesTable);
        urgenceTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefUrgence.class).getAll()));
        urgenceTypesTable.commentAndPhotoProperty().set(false);
        uiUrgenceTypeChoice.setContent(urgenceTypesTable);
    }

    @FXML private void cancel(){

    }

    @FXML
    private void print(){
        Injector.getSession().getTaskManager().submit("Génération de fiches détaillées de réseaux hydrauliques fermés",
        () -> {

            final List<ReseauHydrauliqueFerme> reseauxFermes = Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).getAll();

            final List<ReseauHydrauliqueFerme> tempReseaux = new ArrayList<>(reseauxFermes);
            // On retire les désordres de la liste dans les cas suivants :
            tempReseaux.removeIf(
                    new LocalPredicate().or(
                            new LocationPredicate<>().or(
                                    (Predicate) new TemporalPredicate())));

            try {
                if(!tempReseaux.isEmpty()){
                    Injector.getSession().getPrintManager().printReseaux(tempReseaux, uiOptionPhoto.isSelected(), uiOptionReseauOuvrage.isSelected());
                }
            } catch (Exception ex) {
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            }
        });
    }

    private class LocalPredicate implements Predicate<ReseauHydrauliqueFerme>{

        final List<String> typeConduitesIds = new ArrayList<>();
        final List<String> typeUrgencesIds = new ArrayList<>();

        LocalPredicate(){
            for(final Element element : conduiteTypesTable.getSelectedItems()){
                typeConduitesIds.add(element.getId());
            }
            for(final Element element : urgenceTypesTable.getSelectedItems()){
                typeUrgencesIds.add(element.getId());
            }
        }

        @Override
        public boolean test(final ReseauHydrauliqueFerme reseauFerme) {

            /*
            On retire les réseaux dont la dernière urgence ne figure pas
            dans les urgence choisies.
             */
            final boolean urgenceOption;

            /*
            Si aucune urgence n'a été choisie on ne retire aucun réseau.
            */
            if(typeUrgencesIds.isEmpty()){
                urgenceOption = false;
            }
            else{
                // Recherche de la dernière observation.
                final List<ObservationReseauHydrauliqueFerme> observations = reseauFerme.getObservations();
                ObservationReseauHydrauliqueFerme derniereObservation = null;
                for(final ObservationReseauHydrauliqueFerme obs : observations){
                    if(obs.getDate()!=null){
                        if(derniereObservation==null) derniereObservation = obs;
                        else{
                            if(obs.getDate().isAfter(derniereObservation.getDate())) derniereObservation = obs;
                        }
                    }
                }

                /*
                Si le désordre a une "dernière observation", on regarde si
                son degré d'urgence figure dans les urgences choisies.
                */
                if(derniereObservation!=null){
                    urgenceOption = !typeUrgencesIds.contains(derniereObservation.getUrgenceId());
                }
                /*
                Si on n'a pas pu déterminer de "dernière observation" :
                On garde le désordre (mais c'est discutable).
                */
                else urgenceOption=false;
            }


            /*
            On retire le réseau s'il est d'un type qui n'est pas sélectionné
            dans la liste.
            */
            final boolean typeSelected;
            // Si on n'a sélectionné aucun désordre, on laisse passer a priori quel que soit le type de désordre.
            if (typeConduitesIds.isEmpty()) typeSelected = false;
            // Si la liste de sélection des types de désordres n'est pas vide on vérifie de type de désordre
            else typeSelected = (reseauFerme.getTypeConduiteFermeeId()==null
                    || !typeConduitesIds.contains(reseauFerme.getTypeConduiteFermeeId()));

            return typeSelected || urgenceOption;
        }
    }

}
