package fr.sirs.plugins.synchro.ui.database;

import fr.sirs.Session;
import fr.sirs.plugins.synchro.common.TaskProvider;
import fr.sirs.plugins.synchro.ui.DocumentSelector;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class AttachmentUploadPane extends StackPane {

    final ProgressIndicator indicator;
    final VBox container;
    final DocumentSelector docSelector;
    final LocalDistantView ldList;
    final PhotoExport photoPane;

    final ObjectBinding<Task[]> tasks;

    public AttachmentUploadPane(final Session session) {
        ArgumentChecks.ensureNonNull("Session", session);
        docSelector = new DocumentSelector(session);
        ldList = new LocalDistantView(session.getConnector(), docSelector.getDocuments());
        photoPane = new PhotoExport(session, docSelector.getSelectedTroncons(), docSelector.getDateFilter());

        tasks = Bindings.createObjectBinding(() -> {
            return Stream.of(docSelector, ldList, photoPane)
                    .map(TaskProvider.class::cast)
                    .map(TaskProvider::getTask)
                    .filter(Objects::nonNull)
                    .toArray(size -> new Task[size]);
            },
                docSelector.taskProperty(), ldList.taskProperty(), photoPane.taskProperty()
        );

        tasks.addListener(obs -> {
            disableProperty().unbind();
            final Task[] currentTasks = tasks.get();
            if (currentTasks.length == 1) {
                disableProperty().bind(currentTasks[0].runningProperty());
            } else if (currentTasks.length > 1) {
                BooleanExpression disableTrigger = currentTasks[0].runningProperty();
                for (int i = 1 ; i < currentTasks.length ; i++) {
                    disableTrigger = disableTrigger.or(currentTasks[i].runningProperty());
                }
                disableProperty().bind(disableTrigger);
            }
        });

        indicator = new ProgressIndicator(-1);
        indicator.setVisible(false);
        container = new VBox(10, docSelector, ldList, photoPane);
        container.disableProperty().bind(indicator.visibleProperty());
        container.setFillWidth(true);

        getChildren().addAll(container, indicator);
    }
}
