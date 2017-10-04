package fr.sirs.plugins.synchro.concurrent;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javafx.concurrent.Task;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class CollectionExecutorBuilder<I, O> {

    final Executor pool;
    final Function<I, O> op;

    Stream<I> target;
    BiConsumer<O, Throwable> whenComplete;

    String title;

    CollectionExecutorBuilder(final Executor pool, Function<I, O> op) {
        this.pool = pool;
        this.op = op;
    }

    public CollectionExecutorBuilder<I, O> setTarget(final Stream<I> target) {
        this.target = target;
        return this;
    }

    public CollectionExecutorBuilder<I, O> setWhenComplete(final BiConsumer<O, Throwable> whenComplete) {
        this.whenComplete = whenComplete;
        return this;
    }

    public Task<Void> build() {
        ArgumentChecks.ensureNonNull("Operator", op);
        ArgumentChecks.ensureNonNull("Stream to process", target);

        return new CollectionOperator<>(pool, target, op, whenComplete, title);
    }
}
