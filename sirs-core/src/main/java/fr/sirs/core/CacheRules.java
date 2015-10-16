package fr.sirs.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class CacheRules {

    public static final AtomicBoolean cacheAllDocs = new AtomicBoolean(false);

    public static final ConcurrentHashMap<Class, Boolean> typesToCache = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class, AtomicLong> loadCount = new ConcurrentHashMap<>();

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

    public static void incrementLoaded(final Class type) {
        final AtomicLong count = loadCount.get(type);
        if (count == null) {
            loadCount.put(type, new AtomicLong(1));
        } else {
            count.incrementAndGet();
        }
    }

    public static void printLoadCount() {
        final ArrayList<Map.Entry<Class, AtomicLong>> results = new ArrayList<>(loadCount.entrySet());
        Collections.sort(results, (o1, o2) -> {
            return (int) (o2.getValue().get() - o1.getValue().get());
        });

        SirsCore.LOGGER.info(() -> {
            final StringBuilder countBuilder
                    = new StringBuilder("--------------------------------------")
                    .append('\n')
                    .append("Number of loaded documents");
            for (final Map.Entry<Class, AtomicLong> toto : results) {
                countBuilder
                        .append('\n')
                        .append(completeWithSpaces(toto.getKey().getSimpleName(), 51))
                        .append(" : ")
                        .append(toto.getValue().get());
            }
            return countBuilder.toString();
        });
    }

    private static String completeWithSpaces(final String input, int totalCharacters) {
        final int delta = totalCharacters - input.length();
        if (delta <= 0)
            return input;
        final char[] completion = new char[delta];
        Arrays.fill(completion, ' ');
        return input.concat(new String(completion));
    }
}
