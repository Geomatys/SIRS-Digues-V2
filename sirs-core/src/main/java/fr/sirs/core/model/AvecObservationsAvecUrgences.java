

package fr.sirs.core.model;

import fr.sirs.util.SirsComparator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public interface AvecObservationsAvecUrgences extends Element {

    /**
     * Redmine-ticket : 7727
     * Get the degres d'urgence of the last Observation with a degrés d'urgence.
     *
     * @return
     */
    default String getLastDegreUrgence() {
        if (!(this instanceof Desordre)) {
            throw new IllegalStateException("The interface AvecObservationsAvecUrgences is only valid for Desordre.");
        }

        final ObservableList<AbstractObservation> observations = FXCollections.observableArrayList(((Desordre) this).getObservations());

        if (observations == null || observations.isEmpty()) return null;
        observations.sort(SirsComparator.OBSERVATION_COMPARATOR);

        String lastUrgence = null;
        for (int i = 0; i < observations.size(); i--) {
            final AbstractObservation abstractObservation = observations.get(i);
            if (!(Observation.class.isAssignableFrom(abstractObservation.getClass()))) {
                throw new IllegalStateException("Only applicable to Observation with a urgence attribut.");
            }
            final String urgenceId = ((Observation) observations.get(i)).getUrgenceId();
            if (urgenceId != null) {
                lastUrgence = urgenceId;
                break;
            }
        }

        return lastUrgence;
    }

}
