package fr.sirs.util;

import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.Element;
import java.util.Comparator;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DefaultElementComparator implements Comparator<Element> {

    @Override
    public int compare(Element o1, Element o2) {
        int designationComparison = -1;
        if (o1.getDesignation() == null) {
            designationComparison = o2.getDesignation() == null ? 0 : 1;
        } else if (o1.getDesignation() != null) {
            /* If both designation can be converted to numbers, we will
                 * perform a algebric comparision. Otherwise, we'll compare
                 * directly strings.
             */
            try {
                designationComparison = Integer.decode(o1.getDesignation()).compareTo(Integer.decode(o2.getDesignation()));
            } catch (NumberFormatException e) {
                designationComparison = o1.getDesignation().compareTo(o2.getDesignation());
            }
        }

        if (designationComparison == 0 && (o1 instanceof AvecLibelle) && (o2 instanceof AvecLibelle)) {
            final String l1 = ((AvecLibelle) o1).getLibelle();
            final String l2 = ((AvecLibelle) o2).getLibelle();
            return l1 == l2 ? 0 : l1 == null ? 1 : l2 == null ? -1 : l1.compareTo(l2);
        }

        return designationComparison;
    }
}
