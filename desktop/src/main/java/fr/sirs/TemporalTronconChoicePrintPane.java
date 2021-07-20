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

import fr.sirs.core.model.AbstractObservation;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.AvecObservations;
import fr.sirs.util.DatePickerConverter;
import fr.sirs.util.ObjectDataSource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import org.apache.sis.measure.Range;

/**
 *
 * Classe abstraite permettant de restreindre la sélection d'objets à imprimer à
 * une période de temps.
 *
 * @author Samuel Andrés (Geomatys)
 * @author Maxime Gavens (Geomatys)
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
    protected DatePicker uiOptionDebutLastObservation;
    @FXML
    protected DatePicker uiOptionFinLastObservation;

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
            else {
                uiOptionDebutArchive.setValue(null);
                uiOptionFinArchive.setValue(null);
            }
        });

        uiOptionDebutArchive.disableProperty().bind(uiOptionNonArchive.selectedProperty());
        uiOptionFinArchive.disableProperty().bind(uiOptionNonArchive.selectedProperty());

        DatePickerConverter.register(uiOptionDebut);
        DatePickerConverter.register(uiOptionFin);
        DatePickerConverter.register(uiOptionDebutArchive);
        DatePickerConverter.register(uiOptionFinArchive);
        DatePickerConverter.register(uiOptionDebutLastObservation);
        DatePickerConverter.register(uiOptionFinLastObservation);
    }

    /**
     * Check miscellaneous user options related to objects temporal lifecycle.
     */
    class TemporalPredicate implements Predicate<AvecBornesTemporelles> {

        /**
         * A flag which role is to tell what we should do with archived objects.
         * There're 3 possible states :
         * - Negative : We must exclude archived objects.
         * - Zero : We must include all objects, archived or not.
         * - Positive : We must include archived objects only.
         */
        final byte archiveOption;

        final Range<LocalDate> selectedRange;
        final Range<LocalDate> archiveRange;

        TemporalPredicate() {
            if (uiOptionNonArchive.isSelected())
                archiveOption = -1;
            else if (uiOptionArchive.isSelected())
                archiveOption = 1;
            else
                archiveOption = 0;

            LocalDate start = uiOptionDebutArchive.getValue();
            LocalDate end = uiOptionFinArchive.getValue();
            if (archiveOption < 0 || (start == null && end == null)) {
                archiveRange = null;
            } else {
                archiveRange = new Range<>(LocalDate.class, start == null? LocalDate.MIN : start, true, end == null? LocalDate.MAX : end, true);
            }

            start = uiOptionDebut.getValue();
            end = uiOptionFin.getValue();
            if (start == null && end == null) {
                selectedRange = null;
            } else {
                selectedRange = new Range<>(LocalDate.class, start == null? LocalDate.MIN : start, true, end == null? LocalDate.MAX : end, true);
            }
        }

        @Override
        public boolean test(AvecBornesTemporelles input) {
            if (archiveOption < 0 && input.getDate_fin() != null)
                return false;
            else if (archiveOption > 0 && input.getDate_fin() == null)
                return false;

            if (archiveRange != null && !archiveRange.contains(input.getDate_fin()))
                return false;

            return selectedRange == null || selectedRange.contains(input.getDate_debut());
        }
    }

    class LastObservationPredicate implements Predicate<AvecObservations> {

        final Range<LocalDate> selectedRange;

        public LastObservationPredicate() {
            final LocalDate start = uiOptionDebutLastObservation.getValue();
            final LocalDate end = uiOptionFinLastObservation.getValue();
            if (start == null && end == null) {
                selectedRange = null;
            } else {
                selectedRange = new Range<>(LocalDate.class, start == null? LocalDate.MIN : start, true, end == null? LocalDate.MAX : end, true);
            }
        }

        @Override
        public boolean test(AvecObservations t) {
            final List<? extends AbstractObservation> observations = t.getObservations();

            if ((observations!=null) && (observations.size() > 0)) {
                final AbstractObservation lastObservation = Collections.min(observations, ObjectDataSource.OBSERVATION_COMPARATOR);
                final LocalDate ld = lastObservation.getDate();

                if (ld != null) {
                    return selectedRange == null || selectedRange.contains(ld);
                }
            }
            return selectedRange == null;
        }
    }
}
