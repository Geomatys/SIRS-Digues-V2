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
