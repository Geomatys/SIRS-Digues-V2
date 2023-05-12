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

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.ContactRepository;
import fr.sirs.core.model.*;
import fr.sirs.util.odt.ODTUtils;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.geotoolkit.feature.Feature;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static fr.sirs.core.SirsCore.*;

/**
 * Used for printing Prestation synthese table reports.
 *
 * @author Estelle Idee (Geomatys)
 */
public class PrestationTableSectionRapport extends AbstractSectionRapport {

    @Override
    public Element copy() {
        final PrestationTableSectionRapport rapport = ElementCreator.createAnonymValidElement(PrestationTableSectionRapport.class);
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

    /**
     * Pour l'impression des tableaux, le comportement est le suivant :
     *
     * S'il y a une requête, le résultat à imprimer se base sur le résultat de la requête. Si les propriétés de la requête
     * incluent un identifiant ET que le flux d'éléments n'est pas null, on ne retient pour impression que les tuples de
     * la requête dont l'identifiant peut être retrouvé parmi les identifiants des éléments. Sinon, tous les tuples sont
     * retenus pour impression.
     *
     * S'il n'y a pas de requête, le résultat à imprimer se base sur les éléments.
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    protected void printSection(final PrintContext ctx) throws Exception {
        // HACK : For tables, filter is considered to be data, and elements only
        // serve to filter request data by Ids.

        // récupération des propriétés du résultat de la requête
//            final List<String> properties = propertyNames(ctx.queryResult.getFeatureType());
        final List<String> properties = Arrays.asList(
                DESIGNATION_FIELD, PREVIEW_BUNDLE_KEY_LIBELLE, LINEAR_ID_FIELD, "typePrestationId",
                DATE_DEBUT_FIELD, DATE_FIN_FIELD, "intervenantsIds",
                AUTHOR_FIELD, COMMENTAIRE_FIELD);


        final Map<String, Function<Element, String>> printMapping = new HashMap<>();
        ContactRepository userRepo = (ContactRepository) InjectorCore.getBean(SessionCore.class).getRepositoryForClass(Contact.class);
        printMapping.put("intervenantsIds", new Function<Element, String>() {
            @Override
            public String apply(Element element) {
                StringBuilder result = new StringBuilder();
                ((Prestation) element).getIntervenantsIds().forEach(interId -> {
                    Contact user = userRepo.get(interId);
                    result.append(user.getNom() + " " + user.getPrenom() +"\n");
                });

                return result.toString();
            }
        });
        // ajout des éléments filtrés
        ODTUtils.appendPrestationSyntheseTable(ctx.target, Spliterators.iterator(ctx.elements.spliterator()), properties, printMapping);

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
