package fr.sirs.util;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.model.BorneDigue;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.internal.GeotkFX;

import java.util.concurrent.atomic.AtomicReference;

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

/**
 *
 * @author Estelle Idée (Geomatys)
 */
public class BorneUtils {

    /**
     * Opens a @{@link Dialog} to update the borne's libelle and designation.
     * @param selectedItem the borne to be modified : change designation and libelle.
     * @return whether the borne has been updated.
     */
    public static boolean openBorneRenamingWindow(BorneDigue selectedItem) {
        if (isStartOrEndBorneLibelle(selectedItem.getLibelle(), "Les bornes de début et de fin d'un tronçon ne peuvent être renommées."))
            return false;

        AtomicReference<Boolean> wasUpdated = new AtomicReference<>(Boolean.FALSE);
        String oldLibelle = selectedItem.getLibelle();
        String oldDesignation = selectedItem.getDesignation();
        AtomicReference<String> newLibelle = new AtomicReference<>("");
        AtomicReference<String> newDesignation = new AtomicReference<>(oldDesignation);

        // Form to fill in the new borne libelle and designation.
        final Dialog dialog = new Dialog<Boolean>();
        dialog.setResizable(true);
        dialog.setTitle("Borne existante : " + oldDesignation + " : " + oldLibelle);
        dialog.setGraphic(null);
        dialog.setHeaderText("Modification des informations");

        final GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6 * 2);
        grid.setPadding(new Insets(6));

        Label designationLabel = new Label("Désignation : ");
        TextField designationInput = new TextField(oldDesignation);
        designationLabel.setLabelFor(designationInput);
        grid.add(designationLabel, 0, 0);
        grid.add(designationInput, 1, 0);

        Label libelleLabel = new Label("Libellé : ");
        TextField libelleInput = new TextField(oldLibelle);
        libelleLabel.setLabelFor(libelleInput);
        grid.add(libelleLabel, 0, 1);
        grid.add(libelleInput, 1, 1);

        final DialogPane pane = dialog.getDialogPane();

        pane.setContent(grid);
        pane.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);


        dialog.setResultConverter((button) -> {
            if (!ButtonType.OK.equals(button)) {
                return null;
            }
            newLibelle.set(libelleInput.getText());
            newDesignation.set(designationInput.getText());
            if (oldLibelle.equals(newLibelle.get()) && oldDesignation.equals(newDesignation.get()))
                return null;
            return true;
        });

        dialog.showAndWait().ifPresent((result) -> {
            if (!Boolean.FALSE.equals(result)) {
                String newLib = newLibelle.get();
                String newDes = newDesignation.get();

                // We check that the given libelle is not part of the libelles used for the elementary SR.
                if (isStartOrEndBorneLibelle(newLib, "Le libellé de borne \"" + newLib + "\" est réservé au SR élémentaire."))
                    return;

                if ((newLib != null && !newLib.trim().isEmpty()) || (newDes != null && !newDes.trim().isEmpty()))
                    wasUpdated.set(true);
            }
        });
        // if the libelle nor the designation has been updated,
        // then the selectedItem shall not be updated in the database
        if (Boolean.FALSE.equals(wasUpdated.get())) return false;

        String newLib = newLibelle.get();
        String newDes = newDesignation.get();

        final TaskManager.MockTask renamer = new TaskManager.MockTask("Changement de libellé et de désignation d'une borne", () -> {
            if (!newLib.trim().isEmpty())
                selectedItem.setLibelle(newLib);
            if (!newDes.trim().isEmpty())
                selectedItem.setDesignation(newDes);
            InjectorCore.getBean(BorneDigueRepository.class).update(selectedItem);
        });

        renamer.setOnFailed(evt ->
                Platform.runLater(() ->
                        GeotkFX.newExceptionDialog("Une erreur est survenue lors du changement de libellé/désignation de la borne.", renamer.getException()).show()));

        TaskManager.INSTANCE.submit(renamer);

        return wasUpdated.get();
    }

    public static boolean isStartOrEndBorneLibelle(String libelle, String alertMessage) {
        if (SirsCore.SR_ELEMENTAIRE_START_BORNE.equals(libelle) || SirsCore.SR_ELEMENTAIRE_END_BORNE.equals(libelle)) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, alertMessage, ButtonType.CLOSE);
            alert.setResizable(true);
            alert.showAndWait();
            return true;
        }
        return false;
    }
}
