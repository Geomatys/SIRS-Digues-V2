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

import fr.sirs.core.model.AvecBornesTemporelles;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Predicate;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import org.apache.sis.measure.NumberRange;

/**
 *
 * Classe abtraite permettant de restreindre la sélection d'objets à imprimer à
 * une période de temps.
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class TemporalTronconChoicePrintPane extends TronconChoicePrintPane {

    @FXML
    protected DatePicker uiOptionDebut;
    @FXML
    protected DatePicker uiOptionFin;

    @FXML
    protected DatePicker uiOptionDebutArchive;
    @FXML
    protected DatePicker uiOptionFinArchive;

    @FXML
    protected CheckBox uiOptionNonArchive;
    @FXML
    protected CheckBox uiOptionArchive;

    public TemporalTronconChoicePrintPane(final Class forBundle) {
        super(forBundle);

        uiOptionNonArchive.disableProperty().bind(uiOptionArchive.selectedProperty());
        uiOptionArchive.disableProperty().bind(uiOptionNonArchive.selectedProperty());
        uiOptionNonArchive.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                if (uiOptionArchive.isSelected()) {
                    uiOptionArchive.setSelected(false);
                }
                uiOptionDebutArchive.setValue(null);
                uiOptionFinArchive.setValue(null);
            }
        });
        uiOptionArchive.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue && uiOptionNonArchive.isSelected()) {
                uiOptionNonArchive.setSelected(false);
            }
        });

        uiOptionDebutArchive.disableProperty().bind(uiOptionNonArchive.selectedProperty());
        uiOptionFinArchive.disableProperty().bind(uiOptionNonArchive.selectedProperty());
    }

    class TemporalPredicate implements Predicate<AvecBornesTemporelles> {

        final NumberRange<Long> selectedRange;
        final NumberRange<Long> archiveRange;

        TemporalPredicate() {

            long minTimeSelected = Long.MIN_VALUE;
            long maxTimeSelected = Long.MAX_VALUE;

            {
                LocalDateTime tmpTimeSelected = uiOptionDebut.getValue() == null ? null : uiOptionDebut.getValue().atTime(LocalTime.MIDNIGHT);
                if (tmpTimeSelected != null) {
                    minTimeSelected = Timestamp.valueOf(tmpTimeSelected).getTime();
                }

                tmpTimeSelected = uiOptionFin.getValue() == null ? null : uiOptionFin.getValue().atTime(LocalTime.MIDNIGHT);
                if (tmpTimeSelected != null) {
                    maxTimeSelected = Timestamp.valueOf(tmpTimeSelected).getTime();
                }
            }

            // Intervalle de temps de présence du désordre
            selectedRange = NumberRange.create(minTimeSelected, true, maxTimeSelected, true);

            minTimeSelected = Long.MIN_VALUE;
            maxTimeSelected = Long.MAX_VALUE;

            {
                LocalDateTime tmpTimeSelected = uiOptionDebutArchive.getValue() == null ? null : uiOptionDebutArchive.getValue().atTime(LocalTime.MIDNIGHT);
                if (tmpTimeSelected != null) {
                    minTimeSelected = Timestamp.valueOf(tmpTimeSelected).getTime();
                }

                tmpTimeSelected = uiOptionFinArchive.getValue() == null ? null : uiOptionFinArchive.getValue().atTime(LocalTime.MIDNIGHT);
                if (tmpTimeSelected != null) {
                    maxTimeSelected = Timestamp.valueOf(tmpTimeSelected).getTime();
                }
            }

            // Intervalle d'archivage du désordre
            archiveRange = NumberRange.create(minTimeSelected, true, maxTimeSelected, true);
        }

        @Override
        public boolean test(AvecBornesTemporelles reseauFerme) {

            /*
             CONDITION PORTANT SUR LES OPTIONS
             */
            // 1- Si on a décidé de ne pas générer de fiche pour les désordres archivés.
            final boolean excludeArchiveCondition = (uiOptionNonArchive.isSelected() && reseauFerme.getDate_fin() != null);

            // 2- Si le désordre n'a pas eu lieu durant la période retenue
            final boolean periodeCondition;

            long minTime = Long.MIN_VALUE;
            long maxTime = Long.MAX_VALUE;
            LocalDateTime tmpTime = reseauFerme.getDate_debut() == null ? null : reseauFerme.getDate_debut().atTime(LocalTime.MIDNIGHT);
            if (tmpTime != null) {
                minTime = Timestamp.valueOf(tmpTime).getTime();
            }

            tmpTime = reseauFerme.getDate_fin() == null ? null : reseauFerme.getDate_fin().atTime(LocalTime.MIDNIGHT);
            if (tmpTime != null) {
                maxTime = Timestamp.valueOf(tmpTime).getTime();
            }

            final NumberRange<Long> desordreRange = NumberRange.create(minTime, true, maxTime, true);
            periodeCondition = !selectedRange.intersects(desordreRange);

            // 3- Si on a décidé de ne générer la fiche que des désordres archivés
            final boolean onlyArchiveCondition = (uiOptionArchive.isSelected() && reseauFerme.getDate_fin() == null);

            final boolean periodeArchiveCondition;

            if (!onlyArchiveCondition) {
                long time = Long.MAX_VALUE;

                tmpTime = reseauFerme.getDate_fin() == null ? null : reseauFerme.getDate_fin().atTime(LocalTime.MIDNIGHT);
                if (tmpTime != null) {
                    time = Timestamp.valueOf(tmpTime).getTime();
                }

                final NumberRange<Long> archiveDesordreRange = NumberRange.create(time, true, time, true);
                periodeArchiveCondition = !archiveRange.intersects(archiveDesordreRange);
            } else {
                periodeArchiveCondition = false;
            }

            final boolean archiveCondition = onlyArchiveCondition || periodeArchiveCondition;

            return excludeArchiveCondition || periodeCondition || archiveCondition;
        }

    }
}
