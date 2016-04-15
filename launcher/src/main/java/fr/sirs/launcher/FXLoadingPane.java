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
package fr.sirs.launcher;

import fr.sirs.SIRS;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXLoadingPane extends BorderPane {

    @FXML
    private Label uiTitle;

    @FXML
    private ProgressBar uiProgress;

    @FXML
    private Label uiMessage;

    final Task task;

    public FXLoadingPane(final Task task) {
        super();
        ArgumentChecks.ensureNonNull("Task to watch", task);
        this.task = task;

        SIRS.loadFXML(this);
        uiTitle.textProperty().bind(task.titleProperty());
        uiProgress.progressProperty().bind(task.progressProperty());
        uiMessage.textProperty().bind(task.messageProperty());
    }

    @FXML
    void cancel(ActionEvent event) {
        task.cancel();
    }

}
