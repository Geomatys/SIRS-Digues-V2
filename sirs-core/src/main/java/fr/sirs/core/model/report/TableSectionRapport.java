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
package fr.sirs.core.model.report;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.util.odt.ODTUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.geotoolkit.feature.Feature;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Used for printing brut table reports.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TableSectionRapport extends AbstractSectionRapport {

    @Override
    public Element copy() {
        final TableSectionRapport rapport = ElementCreator.createAnonymValidElement(TableSectionRapport.class);
        super.copy(rapport);
        return rapport;
    }

    @Override
    public boolean removeChild(Element toRemove) {
        return false;
    }

    @Override
    public boolean addChild(Element toAdd) {
        return false;
    }

    @Override
    public Element getChildById(String toSearch) {
        if (toSearch != null && toSearch.equals(getId()))
            return this;
        return null;
    }

    @Override
    protected void printSection(final PrintContext ctx) throws Exception {
        // HACK : For tables, filter is considered to be data, and elements only
        // serve to filter request data by Ids.
        if (ctx.filterValues != null) {
            final List<String> properties = ctx.filterValues.getFeatureType().getProperties(true).stream()
                    .map(p -> p.getName().tip().toString())
                    .collect(Collectors.toList());

            boolean idPresent = properties.remove("id");
            final Set<String> idFilter;
            if (ctx.elements != null && idPresent) {
                idFilter = ctx.elements
                        .map(elt -> elt.getId())
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());
            } else idFilter = null;

            try (final FeatureIterator it = ctx.filterValues.iterator()) {
                final FeatureIterator tmpIt;
                if (idFilter != null)
                    tmpIt = new FilteredFeatureIterator(it, f -> idFilter.contains(f.getPropertyValue("id")));
                else
                    tmpIt = it;
                ODTUtils.appendTable(ctx.target, tmpIt, properties);
            }

            // If there's no SQL query, we try to print elements
        } else if (ctx.elements != null) {
            final List<String> properties = ctx.propertyNames == null? null : new ArrayList<>(ctx.propertyNames);
            ODTUtils.appendTable(ctx.target, Spliterators.iterator(ctx.elements.spliterator()), properties);
        }
    }

    private static class FilteredFeatureIterator implements FeatureIterator {

        private final FeatureIterator source;
        private final Predicate<Feature> filter;

        private Feature next;

        public FilteredFeatureIterator(FeatureIterator source, Predicate<Feature> filter) {
            this.source = source;
            this.filter = filter;
        }

        @Override
        public Feature next() throws FeatureStoreRuntimeException {
            if (hasNext()) {
                final Feature tmpNext = next;
                next = null;
                return tmpNext;
            }

            throw new FeatureStoreRuntimeException("No more elements !");
        }

        @Override
        public boolean hasNext() throws FeatureStoreRuntimeException {
            while (next == null && source.hasNext()) {
                next = source.next();
                if (!filter.test(next)) {
                    next = null;
                }
            }

            return next != null;
        }

        @Override
        public void close() {
            source.close();
        }
    }
}
