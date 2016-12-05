package fr.sirs.util;

import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import javafx.concurrent.Task;
import org.apache.sis.util.collection.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides auto-increment for {@link Element#getDesignation() } property. To do
 * so, we analyzes database {@link Previews} to get the highest numeric
 * designation by data type. Once this analysis is done, we try to keep a cache
 * of the result, simply updated by listening on document changes and (of course)
 * the designation sent back by our ownn component.
 *
 * /!\ WARNING : As we work with distributed systems, the values sent back could
 * be doublons, and so forth are not designed to serve as identifiers !
 *
 * Note : This component delivers values in range [1..{@link Integer#MAX_VALUE}].
 * The behavior past this limit is undefined (no security to prevent overflow).
 *
 * TODO : Listen on database changes to update computed designations.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class DesignationIncrementer {

    @Autowired
    protected Previews previews;

    protected final Cache<Class, Integer> lastDesignationByClass;

    private DesignationIncrementer() {
        lastDesignationByClass = new Cache(8, 0, true);
    }

    public Task<Integer> getNextDesignation(final Class forType) {
        return new Computer(forType);
    }

    private class Computer extends Task<Integer> {

        final Class forType;

        Computer(final Class forType) {
            this.forType = forType;
        }

        @Override
        protected Integer call() throws Exception {
            return lastDesignationByClass.compute(forType, this::incrementOrCompute);
        }

        private Integer incrementOrCompute(final Class key, final Integer oldVal) {
            int lastDesignation = 0;
            if (oldVal != null)
                lastDesignation = oldVal;
            else
                lastDesignation = previews.getByClass(forType).stream()
                        .mapToInt(DesignationIncrementer::designationAsInt)
                        .max()
                        .orElse(0);

            return lastDesignation + 1;
        }
    }

    public static int designationAsInt(final Preview input) {
        try {
            return Integer.parseInt(input.getDesignation());
        } catch (NullPointerException | NumberFormatException e) {
            return 0;
        }
    }
}
