package fr.sirs.plugins.synchro.concurrent;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.concurrent.Task;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class CollectionExecutorBuilder<I, O> {

    final Executor pool;
    final Function<I, O> op;

    Iterator<I> target;
    BiConsumer<O, Throwable> whenComplete;

    CollectionExecutorBuilder(final Executor pool, Function<I, O> op) {
        this.pool = pool;
        this.op = op;
    }

    public CollectionExecutorBuilder<I, O> setTarget(final Iterator<I> target) {
        this.target = target;
        return this;
    }

    public CollectionExecutorBuilder<I, O> setWhenComplete(final BiConsumer<O, Throwable> whenComplete) {
        this.whenComplete = whenComplete;
        return this;
    }

    public Task<Void> build() {
        ArgumentChecks.ensureNonNull("Operator", op);
        ArgumentChecks.ensureNonNull("Collection to process", target);

        return new CollectionOperator<>(pool, target, op, whenComplete);
    }
}
