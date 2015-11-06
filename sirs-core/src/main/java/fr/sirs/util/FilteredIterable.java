package fr.sirs.util;

import java.util.Iterator;
import java.util.function.Predicate;
import org.apache.sis.util.ArgumentChecks;

/**
 * Wrap a given {@link Iterable} to distribute only elements successfully tested
 * with given {@link Predicate}.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class FilteredIterable<T> implements Iterable<T> {

    private final Iterable<T> source;
    private final Predicate<T> filter;

    public FilteredIterable(final Iterable<T> toWrap, final Predicate<T> filter) {
        ArgumentChecks.ensureNonNull("Iterable object to wrap", toWrap);
        ArgumentChecks.ensureNonNull("Filter", filter);
        this.source = toWrap;
        this.filter = filter;
    }

    @Override
    public Iterator<T> iterator() {
        return new FilteredIterator<>(source.iterator(), filter);
    }
}
