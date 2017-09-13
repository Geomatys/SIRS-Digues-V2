package fr.sirs.plugins.synchro.concurrent;

import fr.sirs.SIRS;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.concurrent.Task;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class CollectionOperator<I, O> extends Task<Void> {

    final Executor pool;
    final Iterator<I> input;

    final Function<I, CompletableFuture<O>> executorBuilder;

    CollectionOperator(Executor pool, Iterator<I> input, Function<I, O> op, BiConsumer<O, Throwable> whenComplete) {
        this.pool = pool;
        this.input = input;

        final Function<I, CompletableFuture<O>> baseFutureBuilder = in -> CompletableFuture.supplyAsync(() -> op.apply(in), pool);
        if (whenComplete == null) {
            executorBuilder = baseFutureBuilder;
        } else {
            executorBuilder = baseFutureBuilder.andThen(future -> future.whenComplete(whenComplete));
        }
    }

    @Override
    protected Void call() throws Exception {
        final List<CompletableFuture<O>> futures = new ArrayList<>();
        while (!isCancelled() && input.hasNext()) {
            final I next = input.next();
            futures.add(executorBuilder.apply(next));
        }

        final Iterator<CompletableFuture<O>> futureIt = futures.iterator();
        while (!isCancelled() && futureIt.hasNext()) {
            final CompletableFuture<O> future = futureIt.next();
            try {
                future.join();
            } catch (CompletionException e) {
                // TODO : What to do ? User should have already treated exception with its "whenComplete" handler.
                SIRS.LOGGER.log(Level.FINE, "part of the computing failed", e);
            }
        }

        /* At this point, if there's still some futures present, it means someone
         * cancelled the task, so we must propagate the state to our workers.
         */
        while (futureIt.hasNext()) {
            futureIt.next().cancel(true);
        }

        return null;
    }
}
