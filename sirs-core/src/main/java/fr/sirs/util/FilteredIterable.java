/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
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
