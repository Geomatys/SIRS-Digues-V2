package fr.sirs.util;

import java.util.Comparator;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Type of objects to compare.
 */
public class LabelComparator<T extends Object> implements Comparator<T> {

    final SirsStringConverter labelConverter = new SirsStringConverter();
    final boolean prefixLabels;

    public LabelComparator() {
        this(true);
    }

    public LabelComparator(boolean prefixLabels) {
        this.prefixLabels = prefixLabels;
    }

    @Override
    public int compare(T o1, T o2) {
        if (o1 == o2)
            return 0;
        else if (o1 == null)
            return 1;
        else if (o2 == null)
            return -1;
        else if (o1.equals(o2))
            return 0;

        final String s1 = labelConverter.toString(o1, prefixLabels);
        final String s2 = labelConverter.toString(o2, prefixLabels);
        if (s1 == s2)
            return 0;
        else if (s1 == null)
            return 1;
        else if (s2 == null)
            return -1;

        try {
            return Double.valueOf(s1).compareTo(Double.valueOf(s2));
        } catch (NumberFormatException e) {
            return s1.compareTo(s2);
        }
    }
}
