package fr.sirs.util;

import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.concurrent.Task;
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
 * Once upper limit reached, no more increment will be done, and {@link Integer#MAX_VALUE}
 * will always be returned.
 *
 * TODO : Listen on database changes to update computed designations.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class DesignationIncrementer implements DocumentListener {

    @Autowired
    protected Previews previews;

    /**
     * Cache containing last incremented designations. It's important for it to
     * never lose entries in the session, because user could create multiple
     * elements in-memory before saving them, so we need to keep track of given
     * values, not just the ones in database.
     */
    protected final Map<Class, Integer> lastDesignationByClass;

    private DesignationIncrementer() {
        this(null);
    }

    @Autowired(required = false)
    private DesignationIncrementer(final DocumentChangeEmiter emiter) {
        lastDesignationByClass = new ConcurrentHashMap<>(8);
        if (emiter != null)
            emiter.addListener(this);
    }

    public Task<Integer> nextDesignation(final Class forType) {
        return new Computer(forType);
    }

    @Override
    public void documentCreated(Map<Class, List<Element>> added) {
        updateIncrements(added);
    }

    @Override
    public void documentChanged(Map<Class, List<Element>> changed) {
        updateIncrements(changed);
    }

    protected void updateIncrements(final Map<Class, ? extends Collection<Element>> newElements) {
        // Update upper limit for each modified type, only if they've already been queried.
        for (final Map.Entry<Class, ? extends Collection<Element>> entry : newElements.entrySet()) {
            lastDesignationByClass.computeIfPresent(entry.getKey(), (key, value) -> selectNext(value, entry.getValue()));
        }
    }

    @Override
    public void documentDeleted(Set<String> deleted) {
        // nothing to do
    }

    /**
     * A task whose role is to find the next free designation number for a given
     * data type. Give back integers between 1 and {@link Integer#MAX_VALUE}.
     */
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

            return lastDesignation < Integer.MAX_VALUE? lastDesignation + 1 : Integer.MAX_VALUE;
        }
    }

    /**
     * Extract numeric value for the designation property of the input object,
     * if possible.
     * @param input Preview to extract / transform designation from.
     * @return input {@link Preview#getDesignation() } property, cast as integer
     * if possible. Otherwise, 0 is returned.
     */
    public static int designationAsInt(final Preview input) {
        try {
            return Integer.parseInt(input.getDesignation());
        } catch (NullPointerException | NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Extract numeric value for the designation property of the input object,
     * if possible.
     * @param input Element to extract / transform designation from.
     * @return input {@link Eleement#getDesignation() } property, cast as integer
     * if possible. Otherwise, 0 is returned.
     */
    public static int designationAsInt(final Element input) {
        try {
            return Integer.parseInt(input.getDesignation());
        } catch (NullPointerException | NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Select highest numeric designation from input parameters.
     * @param oldValue A value (previous highest value) to compare to designations
     * extracted from given elements.
     * @param toSearchIn A collection of elements to extract highest designation from.
     * @return The highest designation found in input parameters.
     */
    protected static int selectNext(final int oldValue, final Collection<Element> toSearchIn) {
        if (oldValue >= Integer.MAX_VALUE)
            return oldValue;

        final int newValue = toSearchIn.stream()
                .mapToInt(DesignationIncrementer::designationAsInt)
                .max()
                .orElse(0);

        return Math.max(oldValue, newValue);
    }
}
