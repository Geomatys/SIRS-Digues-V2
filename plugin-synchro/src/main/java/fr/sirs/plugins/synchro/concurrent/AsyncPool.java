package fr.sirs.plugins.synchro.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class AsyncPool implements AutoCloseable {

    final ForkJoinPool pool;

    public AsyncPool(int parallelism) {
        if (parallelism < 1)
            pool = new ForkJoinPool();
        else
            pool = new ForkJoinPool(parallelism);
    }

    @Override
    public void close() throws InterruptedException {
        pool.shutdown();
        if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
            pool.shutdownNow();
        }
    }

    public <I, O> CollectionExecutorBuilder<I, O> prepare(final Function<I, O> operator) {
        return new CollectionExecutorBuilder<>(pool, operator);
    }
}
