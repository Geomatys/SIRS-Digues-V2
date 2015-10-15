package fr.sirs.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class CacheRules {

    public static final AtomicBoolean cacheAllDocs = new AtomicBoolean(false);

    public static final ConcurrentHashMap<Class, Boolean> typesToCache = new ConcurrentHashMap<>();

    /**
     * A method which tells if repositories working with a specific data type should cache them as long as possible, or just ensure unique instance of loaded objects.
     * @param toTest The data type to test.
     * @return True if given data type should be cached, false if we should just ensure unicity.
     */
    public static boolean cacheElementsOfType(final Class toTest) {
        if (cacheAllDocs.get()) {
            return true;
        }
        final Boolean typeRule = typesToCache.get(toTest);
        if (typeRule != null) {
            return typeRule;
        }

        for (final Map.Entry<Class, Boolean> entry : typesToCache.entrySet()) {
            if (entry.getKey().isAssignableFrom(toTest)) {
                return entry.getValue();
            }
        }
        return false;
    }
}
