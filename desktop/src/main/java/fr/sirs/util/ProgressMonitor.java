package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.core.TaskManager;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import org.apache.sis.util.ArgumentChecks;
import org.controlsfx.dialog.ExceptionDialog;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ProgressMonitor extends HBox {

    public static final Image ICON_CANCEL = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_BAN, 13, Color.BLACK), null);
    public static final Image ICON_ERROR = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXCLAMATION_CIRCLE, 16, new Color(200, 0, 0)), null);
    public static final Image ICON_RUNNING_TASKS = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ELLIPSIS_V, 16, new Color(0, 200, 220)), null);

    private TaskProgress lastTask = new TaskProgress();

    private final MenuButton runningTasks = new MenuButton("", new ImageView(ICON_RUNNING_TASKS));
    private final MenuButton tasksInError = new MenuButton("", new ImageView(ICON_ERROR));

    private final TaskManager taskRegistry;

    public ProgressMonitor(TaskManager registry) {
        ArgumentChecks.ensureNonNull("Input task registry", registry);
        setSpacing(10);
        setAlignment(Pos.CENTER);
        minWidthProperty().bind(prefWidthProperty());
        prefWidthProperty().set(USE_COMPUTED_SIZE);

        taskRegistry = registry;

        
        // Hide list of tasks if there's no information on tasks.
        runningTasks.visibleProperty().bind(new SimpleListProperty(runningTasks.getItems()).emptyProperty().not());
        tasksInError.visibleProperty().bind(new SimpleListProperty(tasksInError.getItems()).emptyProperty().not());

        // Do not reserve size for hidden components.
        runningTasks.managedProperty().bind(runningTasks.visibleProperty());
        tasksInError.managedProperty().bind(tasksInError.visibleProperty());
        lastTask.managedProperty().bind(lastTask.visibleProperty());
        
        runningTasks.setMinWidth(0);
        runningTasks.setAlignment(Pos.CENTER);
        tasksInError.setAlignment(Pos.CENTER);
        
        runningTasks.setBorder(Border.EMPTY);
        tasksInError.setBorder(Border.EMPTY);
        
        getChildren().addAll(lastTask, runningTasks, tasksInError);
    }

    /**
     * Fill panel with currently submitted tasks. Add listeners on {@link TaskManager}
     * to be aware of new events.
     */
    private void initTasks() {
        // Listen on current running tasks
        final ObservableList<Task> tmpSubmittedTasks = taskRegistry.getSubmittedTasks();
        tmpSubmittedTasks.addListener((ListChangeListener.Change<? extends Task> c) -> {
            synchronized (tmpSubmittedTasks) {
                final int tmpSize = tmpSubmittedTasks.size();
                final String runTooltip = "" + tmpSize + " tâche" + ((tmpSize > 1) ? "s" : "") + " en cours";
                runningTasks.setTooltip(new Tooltip(runTooltip));
                boolean updateLastTask = false;
                while (c.next()) {
                    List<? extends Task> addedSubList = c.getAddedSubList();
                    if (addedSubList != null && !addedSubList.isEmpty()) {
                        updateLastTask = true;
                        SIRS.LOGGER.info("new tasks detected !");
                        for (Task task : addedSubList) {
                            final CustomMenuItem item = new CustomMenuItem(new TaskProgress(task));
                            item.setHideOnClick(false);
                            runningTasks.getItems().add(item);
                        }
                    }

                    // remove Ended tasks
                    List<? extends Task> removeSubList = c.getRemoved();
                    if (removeSubList != null && !removeSubList.isEmpty()) {
                        runningTasks.getItems().removeIf(new GetItemsForTask(removeSubList));
                        if (removeSubList.contains(lastTask.getTask())) {
                            updateLastTask = true;
                        }
                    }
                }

                // if task order changed, we update the one on display.
                if (updateLastTask) {
                    if (tmpSubmittedTasks.size() > 0) {
                        final Task newTask = tmpSubmittedTasks.get(tmpSubmittedTasks.size() - 1);
                        lastTask.setTask(newTask);
                        runningTasks.getItems().removeIf(new GetItemsForTask(Collections.singletonList(newTask)));
                    } else {
                        lastTask.setTask(null);
                    }
                }
            }
        });

        // Check failed tasks.
        final ObservableList<Task> tmpTasksInError = taskRegistry.getTasksInError();
        tmpTasksInError.addListener((ListChangeListener.Change<? extends Task> c) -> {
            synchronized (tmpTasksInError) {
                final int tmpSize = tmpTasksInError.size();
                final String errorTooltip = "" + tmpSize + " tâche" + ((tmpSize > 1) ? "s ont échouées" : " a échouée");
                tasksInError.setTooltip(new Tooltip(errorTooltip));
                while (c.next()) {
                    List<? extends Task> addedSubList = c.getAddedSubList();
                    if (addedSubList != null && !addedSubList.isEmpty()) {
                        for (Task task : addedSubList) {
                            tasksInError.getItems().add(new ErrorMenuItem(task));
                        }
                    }

                    // remove Ended tasks
                    List<? extends Task> removeSubList = c.getRemoved();
                    if (removeSubList != null && !removeSubList.isEmpty()) {
                        tasksInError.getItems().removeIf(new GetItemsForTask(removeSubList));
                    }
                }
            }
        });
        
        synchronized (tmpSubmittedTasks) {
            final int nbSubmitted = tmpSubmittedTasks.size();
            // do not add last task to our menu, it will be used on main display.
            for (int i = 0; i < nbSubmitted - 1; i++) {
                final CustomMenuItem item = new CustomMenuItem(new TaskProgress(tmpSubmittedTasks.get(i)));
                item.setHideOnClick(false);
                runningTasks.getItems().add(item);
            }
            lastTask.setTask(tmpSubmittedTasks.get(nbSubmitted - 1));
        }

        synchronized (tmpTasksInError) {
            for (Task t : tmpTasksInError) {
                tasksInError.getItems().add(new ErrorMenuItem(t));
            }
        }
    }
    
    /**
     * The node giving information about a specific task. Allow to see title,
     * description and current progress, as to cancel the task.
     */
    private static class TaskProgress extends HBox {

        private final Label title = new Label();
        private final Tooltip description = new Tooltip();
        private final ProgressBar progress = new ProgressBar();
        private final Button cancelButton = new Button("", new ImageView(ICON_CANCEL));

        private Task currentTask;

        public TaskProgress() {
            this(null);
        }

        public TaskProgress(final Task t) {
            setSpacing(5);
            setAlignment(Pos.CENTER);
            
            getChildren().addAll(title, progress, cancelButton);

            if (t != null) {
                setTask(t);
            }
        }

        public Task getTask() {
            return currentTask;
        }

        public synchronized void setTask(Task t) {
            title.textProperty().unbind();
            progress.progressProperty().unbind();
            description.textProperty().unbind();

            cancelButton.setOnAction(null);

            currentTask = t;

            if (currentTask != null) {
                title.textProperty().bind(currentTask.titleProperty());
                description.textProperty().bind(currentTask.messageProperty());
                progress.progressProperty().bind(currentTask.workDoneProperty());
                cancelButton.setOnAction((ActionEvent e) -> currentTask.cancel());
                setVisible(true);
            } else {
                setVisible(false);
            }
        }
    }
    
    private static class ErrorMenuItem extends MenuItem {
         
        final Task failedTask;
        
        public ErrorMenuItem(final Task failedTask) {
            ArgumentChecks.ensureNonNull("task in error", failedTask);
            this.failedTask = failedTask;
            textProperty().bind(this.failedTask.titleProperty());
            Dialog d = new ExceptionDialog(failedTask.getException());
            d.setResizable(true);
            
            setOnAction((ActionEvent ae)->d.show());
        }
        
        public Task getTask() {
            return failedTask;
        }
    }

    /**
     * A simple {@link Predicate} which return current monitor progress bars
     * which are focused on one of the given tasks.
     */
    private static class GetItemsForTask implements Predicate<MenuItem> {

        private final List<? extends Task> tasks;

        public GetItemsForTask(final List<? extends Task> taskFilter) {
            ArgumentChecks.ensureNonNull("Input filter tasks", taskFilter);
            tasks = taskFilter;
        }

        @Override
        public boolean test(MenuItem item) {
            if (item instanceof CustomMenuItem) {
                Node content = ((CustomMenuItem) item).getContent();
                return (content instanceof TaskProgress
                        && tasks.contains(((TaskProgress) content).getTask()));
            } else if (item instanceof ErrorMenuItem) {
                return tasks.contains(((ErrorMenuItem) item).getTask());
            } 
            return false;
        }
    }
}
