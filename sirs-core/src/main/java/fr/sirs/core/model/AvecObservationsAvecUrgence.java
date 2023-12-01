

package fr.sirs.core.model;

import fr.sirs.util.SirsComparator;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public interface AvecObservationsAvecUrgence extends AvecObservations {

    ObservableList<? extends IObservationAvecUrgence> getObservations();

    /**
     * Redmine-ticket : 7727
     * Get the degres d'urgence of the last Observation with a degrés d'urgence.
     *
     * @return
     */
    default String getLastDegreUrgence() {
        // The getObservations() returns an ObservableList.
        // We copy the list to avoid issues when sorting it below.
        // Otherwise, we sometimes get some javafx thread issues.
        final List<? extends IObservationAvecUrgence> observations = new ArrayList<>(getObservations());

        if (observations == null || observations.isEmpty()) return null;
        observations.sort(SirsComparator.OBSERVATION_COMPARATOR);

        String lastUrgence = null;
        for (int i = 0; i < observations.size(); i++) {
            final String urgenceId = observations.get(i).getUrgenceId();
            if (urgenceId != null) {
                lastUrgence = urgenceId;
                break;
            }
        }

        return lastUrgence;
    }



}
