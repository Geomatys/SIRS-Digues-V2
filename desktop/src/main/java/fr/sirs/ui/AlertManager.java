package fr.sirs.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 * @author Cédric Briançon (Geomatys)
 */
public class AlertManager {
    private final ObservableList<AlertItem> alerts = FXCollections.observableArrayList();
    private static AlertManager manager = null;

    private AlertManager() {}

    public static AlertManager getInstance() {
        if (manager == null) {
            manager = new AlertManager();
        }
        return manager;
    }

    public ObservableList<AlertItem> getAlerts() {
        return alerts;
    }
}
