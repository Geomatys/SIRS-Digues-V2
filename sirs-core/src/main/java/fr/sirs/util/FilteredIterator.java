package fr.sirs.util;

import java.util.Iterator;
import java.util.function.Predicate;
import org.apache.sis.util.ArgumentChecks;

/**
 * Wrap an iterator to distribute only elements which are successfully tested by
 * the given {@link Predicate}.
 *
 * @author Alexis Manin (Geomatys)
 */
public class FilteredIterator<T> implements Iterator<T> {

    private final Iterator<T> source;
    private final Predicate<T> filter;

    private T next = null;

    /**
     * Create a new iterator.
     * @param toWrap Source iterator.
     * @param filter Predicate to apply on source iterator to filter results.
     */
    public FilteredIterator(final Iterator<T> toWrap, final Predicate<T> filter) {
        ArgumentChecks.ensureNonNull("Iterator to wrap", toWrap);
        ArgumentChecks.ensureNonNull("Filter", filter);
        this.source = toWrap;
        this.filter = filter;
    }

    @Override
    public boolean hasNext() {
        while (next == null && source.hasNext()) {
            next = source.next();
            if (!filter.test(next)) {
                next = null;
            }
        }

        return next != null;
    }

    @Override
    public T next() {
        if (hasNext()) {
            try {
                return next;
            } finally {
                next = null;
            }
        } else {
            throw new IllegalStateException("Cannot call next() method when there's not any element available.");
        }
    }
}
