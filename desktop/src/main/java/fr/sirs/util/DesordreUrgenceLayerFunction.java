/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */

package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AvecObservationsAvecUrgence;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.filter.function.AbstractFunction;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;


/**
 * Redmine-ticket : 7727
 * Filter used to fill in a map context layer showing the "degrés d'urgence" of the most recent Observation of a Desordre.
 * Look for the most recent Observation having a "degrés d'urgence" non-null.
 * Works for all classes that implement @{@link AvecObservationsAvecUrgence}.
 *
 * @author Estelle Idée (Geomatys)
 */
public class DesordreUrgenceLayerFunction extends AbstractFunction {
    public static final String NAME = "urgenceFunction";
    private final AbstractSIRSRepository<? extends AvecObservationsAvecUrgence> repo;

    public DesordreUrgenceLayerFunction(final Literal classCanonicalName, final Expression expression) {
        super(NAME, new Expression[] {classCanonicalName, expression}, null);
        ArgumentChecks.ensureNonNull("classCanonicalName", classCanonicalName);
        this.repo = Injector.getBean(Session.class).getRepositoryForType(classCanonicalName.getValue().toString());
    }

    public Object evaluate(Object object) {
        ArgumentChecks.ensureNonNull("object", object);
        final String urgenceId = parameters.get(1).evaluate(object, String.class);

        final AvecObservationsAvecUrgence item = repo.get(((BeanFeature) object).getIdentifier().getID());

        if (item == null) return false;

        final String lastDegreUrgence = item.getLastDegreUrgence();
        return lastDegreUrgence != null && lastDegreUrgence.equals(urgenceId);
    }

}
