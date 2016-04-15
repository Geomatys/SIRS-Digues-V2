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
package fr.sirs.core.component;

import fr.sirs.core.model.TronconDigue;
import java.lang.ref.WeakReference;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.application.Platform;

/**
 * Watch over default SR property of a {@link TronconDigue}, in order to compute
 * PRs of attached objects if its value change.
 * @author Alexis Manin (Geomatys)
 */
public class DefaultSRChangeListener implements ChangeListener<String> {

    private static final String CONFIRMATION_TEXT = "Êtes-vous sûr de vouloir changer le SR par défaut ? Les PRs de tous les objets associés au tronçon seront recalculés.";

    private final AbstractTronconDigueRepository repo;
    final WeakReference<TronconDigue> target;

    /**
     * Used as a flag to inform that current detected change has been introduced
     * by a reset from here.
     */
    private String previousValue;


    public DefaultSRChangeListener(final TronconDigue troncon, final AbstractTronconDigueRepository repo) {
        this.repo = repo;
        target = new WeakReference<>(troncon);
        if (troncon != null) {
            troncon.systemeRepDefautIdProperty().addListener(this);
        }
    }

    /**
     * Called when the default SR of a tronçon changes. We ask user if he really
     * want to change this parameter, because it will induce update of all
     * computed PRs for the objects on the current {@link TronconDigue}.
     * If user agree, we register for computing, otherwise we reset value.
     *
     * @param observable The default SR observable property
     * @param oldValue The previous value for the SR property.
     * @param newValue The set value for default SR property.
     */
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        TronconDigue troncon = target.get();
        if (troncon != null && oldValue != null && newValue != null) { // TODO : manage troncon cutting (oldValue == null)
            // Value reset
            if (previousValue != null && previousValue.equals(newValue)) {
                previousValue = null;
                // Ask user if he's sure of his change.

            } else {
                // TODO : use notification system ?
                final Task<Optional<ButtonType>> confirmation = new Task<Optional<ButtonType>>() {

                    @Override
                    protected Optional<ButtonType> call() throws Exception {
                        final Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, CONFIRMATION_TEXT, ButtonType.NO, ButtonType.YES);
                        confirmation.setResizable(true);
                        return confirmation.showAndWait();
                    }
                };

                final Optional<ButtonType> result;
                if (Platform.isFxApplicationThread()) {
                    confirmation.run();
                } else {
                    Platform.runLater(() -> confirmation.run());
                }

                try {
                    result = confirmation.get();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                if (result.isPresent() && result.get().equals(ButtonType.YES)) {
                    repo.registerForPRComputing(troncon);
                } else {
                    previousValue = oldValue;
                    troncon.setSystemeRepDefautId(oldValue);
                }
            }
        }
    }
}
