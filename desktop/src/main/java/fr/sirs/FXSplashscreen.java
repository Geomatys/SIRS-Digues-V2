package fr.sirs;

import fr.sirs.core.SirsDBInfo;
import fr.sirs.core.component.DatabaseRegistry;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSplashscreen {

    @FXML public GridPane uiLoadingPane;
    @FXML public Label uiProgressLabel;
    @FXML public ProgressBar uiProgressBar;
    @FXML public Button uiCancel;

    @FXML public GridPane uiLoginPane;
    @FXML public TextField uiLogin;
    @FXML public PasswordField uiPassword;
    @FXML public Button uiConnexion;
    @FXML public Label uiLogInfo;
    @FXML public Label uiRemoteDb;
    @FXML public Label uiSynchroState;

    @FXML
    void closeApp(ActionEvent event) {
        System.exit(0);
    }

    void analyzeDatabase(final DatabaseRegistry registry, final String databaseName) {
        final Tooltip remoteTip = new Tooltip();
        remoteTip.textProperty().bind(uiRemoteDb.textProperty());
        uiRemoteDb.setTooltip(remoteTip);

        final TaskManager.MockTask<SirsDBInfo> task = new TaskManager.MockTask<>("Recherche...", () -> registry.getInfo(databaseName).orElse(null));
        task.setOnSucceeded(evt -> Platform.runLater(() -> {
            final SirsDBInfo dbInfo = task.getValue();
            if (dbInfo == null || dbInfo.getRemoteDatabase() == null || dbInfo.getRemoteDatabase().isEmpty()) {
                uiRemoteDb.setText("Aucune");
            } else {
                uiRemoteDb.setText(DatabaseRegistry.cleanDatabaseName(dbInfo.getRemoteDatabase()));
            }
        }));

        Runnable onCancelOrFail = () -> {
            uiRemoteDb.setText("Impossible de récupérer l'information");
            uiRemoteDb.setTextFill(Color.RED);
        };

        task.setOnCancelled(evt -> Platform.runLater(onCancelOrFail));
        task.setOnFailed(evt -> {
            SIRS.LOGGER.log(Level.WARNING, "Cannot get database information", task.getException());
            Platform.runLater(onCancelOrFail);
        });

        TaskManager.INSTANCE.submit(task);

        TaskManager.MockTask<Set<String>> task2 = new TaskManager.MockTask<>("", () -> registry.getSynchronizationTasks(databaseName)
                .map(status -> {
                    if (status.getSourceDatabaseName().equals(databaseName)) {
                        return DatabaseRegistry.cleanDatabaseName(status.getTargetDatabaseName());
                    } else {
                        return DatabaseRegistry.cleanDatabaseName(status.getSourceDatabaseName());
                    }
                })
                .collect(Collectors.toSet()));

        task2.setOnSucceeded(evt -> {
            final Set<String> synchronisations = task2.getValue();
            if (synchronisations.isEmpty()) {
                uiSynchroState.setText("Aucune synchronisation en cours");
            } else {
                final StringBuilder tipBuilder = new StringBuilder("synchronisation avec :");
                for (final String dbName : synchronisations) {
                    tipBuilder.append(System.lineSeparator()).append(dbName);
                }
                uiSynchroState.setText(String.valueOf(synchronisations.size()).concat(synchronisations.size() == 1 ? " synchronisation en cours" : " synchronisations en cours"));
                uiSynchroState.setTooltip(new Tooltip(tipBuilder.toString()));
            }
        });

        TaskManager.INSTANCE.submit(task2);
    }
}
