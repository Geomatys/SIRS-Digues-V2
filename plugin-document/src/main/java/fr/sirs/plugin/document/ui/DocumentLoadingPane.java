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

import fr.sirs.plugin.document.FileAndUnsupportedFiles;
import fr.sirs.ui.LoadingPane;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

import java.util.List;

/**
 * A simple panel whose aim is to display advancement of a given task.
 *
 * @author Alexis Manin (Geomatys)
 */
public class DocumentLoadingPane extends LoadingPane {
    public DocumentLoadingPane() {
        super();
    }

    @Override
    protected void taskChanged(final ObservableValue<? extends Task> obs, final Task oldValue, final Task newValue) {
        if (oldValue != null) {
            uiProgress.progressProperty().unbind();
            uiProgressLabel.textProperty().unbind();
            uiGenerateFinish.visibleProperty().unbind();
            uiErrorLabel.visibleProperty().unbind();
        }

        if (newValue == null) {
            uiProgress.setVisible(false);
            uiGenerateFinish.setVisible(false);
            uiErrorLabel.setVisible(false);
            uiProgressLabel.setText("Aucune tâche en cours");
        } else {
            uiProgress.setVisible(true);
            uiProgress.progressProperty().bind(newValue.progressProperty());
            uiProgressLabel.textProperty().bind(newValue.messageProperty());
            uiGenerateFinish.disableProperty().bind(newValue.runningProperty());
            newValue.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, evt -> {
                final Object taskValue = newValue.getValue();
                if (taskValue instanceof FileAndUnsupportedFiles) {
                    FileAndUnsupportedFiles taskResult = (FileAndUnsupportedFiles) taskValue;
                    final List<String> unsupportedFiles = taskResult.getUnsupportedFiles();
                    if (unsupportedFiles != null && !unsupportedFiles.isEmpty()) {
                        StringBuilder errorMsg = new StringBuilder("Information : \nLes fichiers suivants n'ont pas été ajoutés au dossier de synthèse \ncar leurs formats ne sont pas pris en charge:\n");
                        for (String fileName : unsupportedFiles) {
                            errorMsg.append("\n" + fileName);
                        }
                        errorMsg.append(" \n\nFormats pris en charge : ODF, PDF, Image et text\n");
                        uiErrorLabel.setText(errorMsg.toString());
                        uiErrorLabel.setVisible(true);
                        this.getScene().getWindow().sizeToScene();
                    }
                }
                // If bound task did not update its progress to finish state, we do it ourself.
                if (uiProgress.getProgress() < 1) {
                    uiProgressLabel.textProperty().unbind();
                    uiProgressLabel.setText("");
                    uiProgress.progressProperty().unbind();
                    uiProgress.setProgress(1);
                }
            });

            newValue.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, evt -> {
                    uiProgressLabel.textProperty().unbind();
                    uiProgressLabel.setText("Une erreur est survenue !");
                    uiProgressLabel.setStyle("-fx-text-fill:red");
                    uiProgress.progressProperty().unbind();
                    uiProgress.setProgress(0);
            });


            newValue.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, evt -> {
                    uiProgressLabel.textProperty().unbind();
                    uiProgressLabel.setText("La tâche a été interrompue !");
                    uiProgress.progressProperty().unbind();
                    uiProgress.setProgress(0);
            });
        }
    }
}
