package fr.sirs.util;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.model.BorneDigue;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.internal.GeotkFX;

import java.util.Optional;

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
public class BorneUtils {

    public static boolean openBorneRenamingWindow(BorneDigue selectedItem) {
        if (isStartOrEndBorneLibelle(selectedItem.getLibelle(), "Les bornes de début et de fin d'un tronçon ne peuvent être renommées."))
            return false;

        // Form to fill in the new borne libelle.
        final TextInputDialog dialog = new TextInputDialog(selectedItem.getLibelle());
        dialog.getEditor().setPromptText("borne ...");
        dialog.setTitle("Borne existante : " + selectedItem.getLibelle());
        dialog.setGraphic(null);
        dialog.setHeaderText("Nouveau libellé de la borne");


        final Optional<String> opt = dialog.showAndWait();
        if (!opt.isPresent() || opt.get().trim().isEmpty()) return false;

        // Edition of the borne.
        final String borneLbl = opt.get();

        // We check that the given libelle is not part of the libelles used for the elementary SR.
        if (isStartOrEndBorneLibelle(borneLbl, "Le libellé de borne \"" + borneLbl + "\" est réservé au SR élémentaire."))
            return false;

        final TaskManager.MockTask renamer = new TaskManager.MockTask("Changement de libellé d'une borne", () -> {
            selectedItem.setLibelle(borneLbl);
            InjectorCore.getBean(BorneDigueRepository.class).update(selectedItem);
        });

        renamer.setOnFailed(evt ->
                Platform.runLater(() ->
                        GeotkFX.newExceptionDialog("Une erreur est survenue lors du changement de libellé de la borne.", renamer.getException()).show()));

        TaskManager.INSTANCE.submit(renamer);

        return true;
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