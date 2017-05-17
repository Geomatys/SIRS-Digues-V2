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

import fr.sirs.core.model.RefConduiteFermee;
import fr.sirs.core.model.RefOuvrageHydrauliqueAssocie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.ui.Growl;
import java.util.ArrayList;
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
public class FXOuvrageAssociePrintPane extends TemporalTronconChoicePrintPane {

    @FXML private Tab uiOuvrageTypeChoice;

    @FXML private CheckBox uiOptionPhoto;
    @FXML private CheckBox uiOptionReseauOuvrage;

    private final TypeChoicePojoTable ouvrageTypesTable = new TypeChoicePojoTable(RefOuvrageHydrauliqueAssocie.class, "Types d'ouvrages associés");

    private final ObjectProperty<Task<Boolean>> taskProperty = new SimpleObjectProperty<>();

    @FXML private Button uiPrint;
    @FXML private Button uiCancel;

    public FXOuvrageAssociePrintPane(){
        super(FXOuvrageAssociePrintPane.class);
        ouvrageTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefOuvrageHydrauliqueAssocie.class).getAll()));
        ouvrageTypesTable.commentAndPhotoProperty().set(false);
        uiOuvrageTypeChoice.setContent(ouvrageTypesTable);

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
        final Task<Boolean> printing = new TaskManager.MockTask<>("Génération de fiches détaillées d'ouvrages hydrauliques associés", () -> {
            final Thread t = Thread.currentThread();
            final Predicate userOptions = new TypeOuvragePredicate()
                    .and(new TemporalPredicate())
                    .and(new LinearPredicate<>())
                    .and(new PRPredicate<>());

            final List<ReseauHydrauliqueFerme> toPrint = new ArrayList<>(100);
            try (final CloseableIterator<ReseauHydrauliqueFerme> it = Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).getAllStreaming().iterator()) {
                ReseauHydrauliqueFerme r;
                while (it.hasNext() && !t.isInterrupted()) {
                    r = it.next();
                    if (userOptions.test(r)){
                        toPrint.add(r);
                    }
                }
            }

            if (!toPrint.isEmpty() && !t.isInterrupted()) {
                Injector.getSession().getPrintManager().printReseaux(toPrint, uiOptionPhoto.isSelected(), uiOptionReseauOuvrage.isSelected());
            }
            
            return !toPrint.isEmpty();
        });
        taskProperty.set(printing);

        TaskManager.INSTANCE.submit(printing);
    }

    private class TypeOuvragePredicate implements Predicate<ReseauHydrauliqueFerme> {

        final Set<String> acceptedIds;

        TypeOuvragePredicate() {
            acceptedIds = ouvrageTypesTable.getSelectedItems().stream()
                    .map(e -> e.getId())
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean test(ReseauHydrauliqueFerme t) {
            return acceptedIds.isEmpty() || (t.getTypeConduiteFermeeId() != null && acceptedIds.contains(t.getTypeConduiteFermeeId()));
        }
    }
}
