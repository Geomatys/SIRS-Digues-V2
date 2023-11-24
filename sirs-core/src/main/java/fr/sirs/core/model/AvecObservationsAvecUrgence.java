

package fr.sirs.core.model;

import fr.sirs.util.SirsComparator;
import javafx.collections.FXCollections;

import java.util.List;

public interface AvecObservationsAvecUrgence extends AvecObservations {

    List<? extends AbstractObservationAvecUrgence> getObservations();

    /**
     * Redmine-ticket : 7727
     * Get the degres d'urgence of the last Observation with a degrés d'urgence.
     *
     * @return
     */
    default String getLastDegreUrgence() {
        final List<? extends AbstractObservationAvecUrgence> observations = getObservations();

        if (observations == null || observations.isEmpty()) return null;
        observations.sort(SirsComparator.OBSERVATION_COMPARATOR);

        String lastUrgence = null;
        for (int i = 0; i < observations.size(); i--) {
            final String urgenceId = observations.get(i).getUrgenceId();
            if (urgenceId != null) {
                lastUrgence = urgenceId;
                break;
            }
        }

        return lastUrgence;
    }

}
