

package fr.sirs.core.model;

import fr.sirs.util.SirsComparator;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.apache.sis.measure.Range;

public interface AvecObservations extends  Element {

    public List<? extends AbstractObservation> getObservations();

    /**
     * Filtre les éléments par la date de leur derniere observation.
     * L'élément est sélectionné si sa dernière observation a été créé à une date comprise dans l'interval donné.
     */
    public static final class LastObservationPredicate implements Predicate<AvecObservations> {

        final Range<LocalDate> selectedRange;

        /**
         * /!\ si aucune date de début ou date de fin n'est renseignée
         * alors selectedRange est initialisé à null.
         */
        public LastObservationPredicate(final LocalDate start, final LocalDate end) {
            //final LocalDate start = uiOptionDebutLastObservation.getValue();
            //final LocalDate end = uiOptionFinLastObservation.getValue();
            if (start == null && end == null) {
                selectedRange = null;
            } else {
                selectedRange = new Range<>(LocalDate.class, start == null? LocalDate.MIN : start, true, end == null? LocalDate.MAX : end, true);
            }
        }

        @Override
        public boolean test(AvecObservations t) {
            final List<? extends AbstractObservation> observations = t.getObservations();

            if ((observations!=null) && (observations.size() > 0)) {
                final AbstractObservation lastObservation = Collections.min(observations, SirsComparator.OBSERVATION_COMPARATOR);
                final LocalDate ld = lastObservation.getDate();

                if (ld != null) {
                    return selectedRange == null || selectedRange.contains(ld);
                }
            }
            return selectedRange == null;
        }
    }
}

