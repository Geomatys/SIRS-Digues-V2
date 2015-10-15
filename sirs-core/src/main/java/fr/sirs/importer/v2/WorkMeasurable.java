package fr.sirs.importer.v2;

import javafx.beans.property.IntegerProperty;

/**
 * A component which wants to
 * @author Alexis Manin (Geomatys)
 */
public interface WorkMeasurable {

    int getTotalWork();

    IntegerProperty getWorkDone();
}
