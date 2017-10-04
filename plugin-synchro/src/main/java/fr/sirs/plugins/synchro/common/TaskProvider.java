package fr.sirs.plugins.synchro.common;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Result type of provided task.
 */
public interface TaskProvider<T> {

    ObservableValue<Task<T>> taskProperty();
    Task<T> getTask();
}
