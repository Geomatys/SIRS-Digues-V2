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
package fr.sirs.plugin.document.ui;

import fr.sirs.SIRS;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A simple panel whose aim is to display advancement of a given task.
 *
 * @author Alexis Manin (Geomatys)
 */
public class LoadingPane extends GridPane {

    @FXML
    public ProgressIndicator uiProgress;

    @FXML
    public Button uiGenerateFinish;

    @FXML
    public Label uiProgressLabel;

    /**
     * Property holding task to listen to.
     */
    public final SimpleObjectProperty<Task> taskProperty;

    public LoadingPane() {
        this(null);
    }

    public LoadingPane(final Task input) {
        SIRS.loadFXML(this);

        this.taskProperty = new SimpleObjectProperty<>();
        taskProperty.addListener(this::taskChanged);
        taskProperty.set(input);
    }

    private void taskChanged(final ObservableValue<? extends Task> obs, final Task oldValue, final Task newValue) {
        if (oldValue != null) {
            uiProgress.progressProperty().unbind();
            uiProgressLabel.textProperty().unbind();
            uiGenerateFinish.visibleProperty().unbind();
        }

        if (newValue == null) {
            uiProgress.setVisible(false);
            uiGenerateFinish.setVisible(false);
            uiProgressLabel.setText("Aucune tâche en cours");
        } else {
            uiProgress.setVisible(true);
            uiProgress.progressProperty().bind(newValue.progressProperty());
            uiProgressLabel.textProperty().bind(newValue.messageProperty());
            uiGenerateFinish.disableProperty().bind(newValue.runningProperty());
        }
    }

    public static void showDialog(final Task t) {
        final LoadingPane gpane = new LoadingPane(t);
        final Stage stage = new Stage();
        gpane.uiGenerateFinish.setOnAction(event -> stage.close());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.titleProperty().bind(t.titleProperty());

        stage.setScene(new Scene(gpane));
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }
}
