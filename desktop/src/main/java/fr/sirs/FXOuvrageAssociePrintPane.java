/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs;

import fr.sirs.core.SirsCore;
import fr.sirs.core.model.*;
import fr.sirs.core.model.AvecObservations.LastObservationPredicate;
import fr.sirs.ui.Growl;
import fr.sirs.util.ClosingDaemon;
import fr.sirs.util.ConvertPositionableCoordinates;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.property.SirsPreferences;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.util.collection.CloseableIterator;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXOuvrageAssociePrintPane extends TemporalTronconChoicePrintPane {

    private static final String MEMORY_ERROR_MSG = String.format(
            "Impossible d'imprimer les fiches : la mémoire disponible est insuffisante. Vous devez soit :%n"
                    + " - sélectionner moins d'ouvrages hydrauliques associés,%n"
                    + " - allouer plus de mémoire à l'application."
    );

    @FXML private Tab uiOuvrageTypeChoice;
    @FXML private Tab uiUrgenceTypeChoice;
    @FXML private CheckBox uiOptionPhoto;
    @FXML private CheckBox uiOptionReseauxFermes;
    @FXML private CheckBox uiOptionLocationInsert;
    @FXML private CheckBox uiDisablePR;
    @FXML private CheckBox uiDisableXY;
    @FXML private CheckBox uiDisableBorne;
    @FXML private CheckBox uiOptionObservationsSpec;

    private final TypeChoicePojoTable ouvrageTypesTable = new TypeChoicePojoTable(RefOuvrageHydrauliqueAssocie.class, "Types d'ouvrages associés");
    private final TypeChoicePojoTable urgenceTypesTable = new TypeChoicePojoTable(RefUrgence.class, "Types d'urgences");

    private final ObjectProperty<Task<Boolean>> taskProperty = new SimpleObjectProperty<>();

    @FXML private Button uiPrint;
    @FXML private Button uiCancel;

    @FXML private Label uiCountLabel;
    @FXML private ProgressIndicator uiCountProgress;
    @FXML private FXSuiteApporterPredicater uiSuiteApporterPredicater;

    private final InvalidationListener parameterListener;
    private final ObjectProperty<Task> countTask = new SimpleObjectProperty<>();

    public FXOuvrageAssociePrintPane(){
        super(FXOuvrageAssociePrintPane.class);
        ouvrageTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefOuvrageHydrauliqueAssocie.class).getAll()));
        ouvrageTypesTable.commentAndPhotoProperty().set(false);
        uiOuvrageTypeChoice.setContent(ouvrageTypesTable);
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
                newVal.setOnRunning(evt -> Platform.runLater(() -> {
                    if (uiOptionLocationInsert.isSelected()) {
                        new Growl(Growl.Type.WARNING, "Durant l'extraction des données lors de l'impression des fiches, le style de la carte est temporairement modifié.").showAndFade();
                    }
                }));
            }
        });

        parameterListener = this::updateCount;
        ouvrageTypesTable.getSelectedItems().addListener(parameterListener);
        urgenceTypesTable.getSelectedItems().addListener(parameterListener);
        // TODO : listen PR change on selected items.
        tronconsTable.getSelectedItems().addListener(parameterListener);
        uiOptionArchive.selectedProperty().addListener(parameterListener);
        uiOptionNonArchive.selectedProperty().addListener(parameterListener);
        uiOptionDebut.valueProperty().addListener(parameterListener);
        uiOptionFin.valueProperty().addListener(parameterListener);
        uiOptionDebutArchive.valueProperty().addListener(parameterListener);
        uiOptionFinArchive.valueProperty().addListener(parameterListener);
        uiOptionDebutNonArchive.valueProperty().addListener(parameterListener);
        uiOptionFinNonArchive.valueProperty().addListener(parameterListener);
        uiOptionDebutLastObservation.valueProperty().addListener(parameterListener);
        uiOptionFinLastObservation.valueProperty().addListener(parameterListener);
        uiOptionExcludeValid.selectedProperty().addListener(parameterListener);
        uiOptionExcludeInvalid.selectedProperty().addListener(parameterListener);

        uiPrestationPredicater.uiOptionPrestation.selectedProperty().addListener(parameterListener);

        uiCountProgress.setVisible(false);
        updateCount(null);

        uiSuiteApporterPredicater.addListener(parameterListener);
    }

    @FXML private void cancel() {
        final Task t = taskProperty.get();
        if (t != null) {
            //restore the map style
            PrinterUtilities.restoreMap(getData().findFirst().orElseThrow(() -> new RuntimeException("No ouvrage associé to print")));
            try {
                t.cancel();
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, "Could not cancel printing Ouvrages", e);
                throw e;
            } finally {
                PrinterUtilities.canPrint.set(true);
            }
        }
    }

    @FXML
    private void print() {
        final Task<Boolean> printing = new TaskManager.MockTask<>("Génération de fiches détaillées", () -> {
            final List<OuvrageHydrauliqueAssocie> toPrint = new ArrayList<>();
            try {
                try (final Stream<OuvrageHydrauliqueAssocie> data = getData()) {
                    toPrint.addAll(data.collect(Collectors.toList()));
                }

                if (!toPrint.isEmpty() && !Thread.currentThread().isInterrupted())
                    Injector.getSession().getPrintManager().printOuvragesAssocies(toPrint, uiOptionPhoto.isSelected(), uiOptionReseauxFermes.isSelected(), uiOptionLocationInsert.isSelected(), !uiDisablePR.isSelected(), !uiDisableXY.isSelected(), !uiDisableBorne.isSelected(), uiOptionObservationsSpec.isSelected());

                PrinterUtilities.canPrint.set(true);
                return !toPrint.isEmpty();

            } catch (OutOfMemoryError error) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot print ouvrages hydrauliques associés due to lack of memory", error);
                Platform.runLater(() -> {
                    final Alert alert = new Alert(Alert.AlertType.ERROR, MEMORY_ERROR_MSG, ButtonType.OK);
                    alert.show();
                });
                PrinterUtilities.canPrint.set(true);
                throw error;
            } catch (RuntimeException re) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot print ouvrages hydrauliques associés due to error", re);
                if (!toPrint.isEmpty())
                    //restore the map style
                    PrinterUtilities.restoreMap(toPrint.get(0));
                PrinterUtilities.canPrint.set(true);
                throw re;
            }
        });
        if (PrinterUtilities.canPrint.compareAndSet(true, false)) {
            taskProperty.set(printing);
            TaskManager.INSTANCE.submit(printing);
        } else {
            SirsCore.LOGGER.log(Level.WARNING, "Cannot print Ouvrages Hydrauliques Associés due to other printing on going");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Une impression de fiche est en cours,\nveuillez réessayer quand elle sera terminée", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private Stream<OuvrageHydrauliqueAssocie> getData() {
            Predicate userOptions = new TypeOuvragePredicate()
                    .and(new ValidPredicate())
                    .and(new TemporalPredicate())
                    .and(new LinearPredicate<>())
                // /!\ It's important that pr filtering is done AFTER linear filtering.
                    .and(new PRPredicate<>())
                    .and(new AvecObservations.UrgencePredicate(urgenceTypesTable.getSelectedItems().stream()
                            .map(Identifiable::getId)
                            .collect(Collectors.toSet())))
                    .and(uiPrestationPredicater.getPredicate())
                    .and(new LastObservationPredicate(uiOptionDebutLastObservation.getValue(), uiOptionFinLastObservation.getValue()))
                    .and(new AvecObservations.LastObservationSuiteApporterPredicate(uiSuiteApporterPredicater.getSuiteApporter()));

        // HACK-REDMINE-4408 : remove elements on archived Troncons
        if (SirsPreferences.getHideArchivedProperty()) {
            userOptions = userOptions.and(new isNotOnArchivedTroncon());
        }

        final CloseableIterator<OuvrageHydrauliqueAssocie> it = Injector.getSession()
                .getRepositoryForClass(OuvrageHydrauliqueAssocie.class)
                .getAllStreaming()
                .iterator();

        final Spliterator<OuvrageHydrauliqueAssocie> split = Spliterators.spliteratorUnknownSize(it, 0);
        final Stream dataStream = StreamSupport.stream(split, false)
                .filter(userOptions)
                .peek(p -> ConvertPositionableCoordinates.COMPUTE_MISSING_COORD.test((Positionable) p));

        dataStream.onClose(it::close);
        ClosingDaemon.watchResource(dataStream, it);

        return dataStream;
    }

    private void updateCount(final Observable changedObs) {
        final Task oldTask = countTask.get();
        if (oldTask != null)
            oldTask.cancel(true);

        final Task t = new TaskManager.MockTask<>("Décompte des éléments à imprimer", () -> {
            try (final Stream data = getData()) {
                return data.count();
            }
        });

        uiCountProgress.visibleProperty().bind(t.runningProperty());
        t.setOnRunning(evt -> uiCountLabel.setText(null));

        t.setOnSucceeded(evt -> uiCountLabel.setText(String.valueOf(t.getValue())));

        t.setOnFailed(evt -> new Growl(Growl.Type.ERROR, "Impossible de déterminer le nombre d'ouvrages à imprimer.").showAndFade());

        countTask.set(t);
        TaskManager.INSTANCE.submit(t);
    }


    @Override
    protected InvalidationListener getParameterListener() {
        return parameterListener;
    }

    private class TypeOuvragePredicate implements Predicate<OuvrageHydrauliqueAssocie> {

        final Set<String> acceptedIds;

        TypeOuvragePredicate() {
            acceptedIds = ouvrageTypesTable.getSelectedItems().stream()
                    .map(Identifiable::getId)
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean test(OuvrageHydrauliqueAssocie t) {
            return acceptedIds.isEmpty() || (t.getTypeOuvrageHydroAssocieId()!= null && acceptedIds.contains(t.getTypeOuvrageHydroAssocieId()));
        }
    }
}
