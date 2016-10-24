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

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.RefUrgence;
import fr.sirs.ui.Growl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.util.collection.CloseableIterator;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDisorderPrintPane extends TemporalTronconChoicePrintPane {

    private static final Comparator<Observation> DATE_COMPARATOR = (o1, o2) -> {
        if (o1.getDate() == o2.getDate())
            return 0;
        if (o1.getDate() == null)
            return 1;
        if (o2.getDate() == null)
            return -1;
        return o1.getDate().compareTo(o2.getDate());
    };

    @FXML private Tab uiDisorderTypeChoice;
    @FXML private Tab uiUrgenceTypeChoice;

    @FXML private CheckBox uiOptionPhoto;
    @FXML private CheckBox uiOptionReseauOuvrage;
    @FXML private CheckBox uiOptionVoirie;

    private final TypeChoicePojoTable disordreTypesTable = new TypeChoicePojoTable(RefTypeDesordre.class, "Types de désordres");
    private final TypeChoicePojoTable urgenceTypesTable = new TypeChoicePojoTable(RefUrgence.class, "Types d'urgences");

    private final ObjectProperty<Task<Boolean>> taskProperty = new SimpleObjectProperty<>();

    @FXML private Button uiPrint;
    @FXML private Button uiCancel;

    public FXDisorderPrintPane(){
        super(FXDisorderPrintPane.class);
        disordreTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefTypeDesordre.class).getAll()));
        disordreTypesTable.commentAndPhotoProperty().set(false);
        uiDisorderTypeChoice.setContent(disordreTypesTable);
        urgenceTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefUrgence.class).getAll()));
        urgenceTypesTable.commentAndPhotoProperty().set(false);
        uiUrgenceTypeChoice.setContent(urgenceTypesTable);

        uiPrint.disableProperty().bind(uiCancel.disableProperty().not());
        uiCancel.setDisable(true);
        taskProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                uiCancel.disableProperty().unbind();
                uiCancel.setDisable(true);
            } else {
                uiCancel.disableProperty().bind(newVal.runningProperty().not());
                newVal.setOnSucceeded(evt -> Platform.runLater(() -> {
                    if (Boolean.TRUE.equals(evt.getSource().getValue())) {
                        new Growl(Growl.Type.INFO, "La fiche a été générée avec succès.").showAndFade();
                    } else {
                        new Growl(Growl.Type.WARNING, "Aucun élément trouvé pour les critères demandés.").showAndFade();
                    }
                    taskProperty.set(null);
                }));
                newVal.setOnFailed(evt -> Platform.runLater(() -> {
                    new Growl(Growl.Type.ERROR, "L'impression a échouée.").showAndFade();
                    taskProperty.set(null);
                }));
            }
        });
    }

    @FXML private void cancel() {
        final Task t = taskProperty.get();
        if (t != null)
            t.cancel();
    }


    @FXML
    private void print() {
        final Task<Boolean> printing = new TaskManager.MockTask<>("Génération de fiches détaillées de désordres", () -> {
            final Thread t = Thread.currentThread();
            final Predicate userOptions  = new TypePredicate()
                    .and(new TemporalPredicate())
                    .and(new LinearPredicate<>())
                    .and(new PRPredicate<>())
                    .and(new UrgencePredicate());

            final List<Desordre> toPrint = new ArrayList(100);
            try (final CloseableIterator<Desordre> it = Injector.getSession().getRepositoryForClass(Desordre.class).getAllStreaming().iterator()) {
                Desordre d;
                while (it.hasNext() && !t.isInterrupted()) {
                    d = it.next();
                    if (userOptions.test(d)) {
                        toPrint.add(d);
                    }
                }
            }

            if (!toPrint.isEmpty() && !t.isInterrupted())
                Injector.getSession().getPrintManager().printDesordres(toPrint, uiOptionPhoto.isSelected(), uiOptionReseauOuvrage.isSelected(), uiOptionVoirie.isSelected());

            return !toPrint.isEmpty();

        });
        taskProperty.set(printing);

        TaskManager.INSTANCE.submit(printing);
    }

    /**
     * Check that given {@link Desordre} object has a {@link Desordre#getTypeDesordreId() }
     * contained in a specific input list. If user has not selected any disorder
     * type, this predicate always return true.
     */
    private class TypePredicate implements Predicate<Desordre> {

        final Set<String> acceptedIds;

        TypePredicate() {
            acceptedIds = disordreTypesTable.getSelectedItems().stream()
                    .map(e -> e.getId())
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean test(Desordre t) {
            return acceptedIds.isEmpty() || (t.getTypeDesordreId() != null && acceptedIds.contains(t.getTypeDesordreId()));
        }
    }

    /**
     * Check that the most recent observation defined on given disorder has an
     * {@link Observation#getUrgenceId() } compatible with user choice.
     * If user has not chosen any urgence, all disorders are accepted.
     */
    private class UrgencePredicate implements Predicate<Desordre> {

        final Set<String> acceptedIds;

        UrgencePredicate() {
            acceptedIds = urgenceTypesTable.getSelectedItems().stream()
                    .map(e -> e.getId())
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean test(final Desordre desordre) {
            if (acceptedIds.isEmpty())
                return true;

            return desordre.getObservations().stream()
                    .max(DATE_COMPARATOR)
                    .map(obs -> obs.getUrgenceId() != null && acceptedIds.contains(obs.getUrgenceId()))
                    .orElse(false);
        }
    }
}
