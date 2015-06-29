package fr.sirs.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 * Gestionnaire d'alertes de l'application. Elles seront affichées dans une barre en bas de l'application.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class AlertManager {
    /**
     * Liste des alertes à afficher.
     */
    private final ObservableList<AlertItem> alerts = FXCollections.observableArrayList();
    /**
     * Définit si les alertes doivent être affichées dans l'application ou non.
     */
    private boolean alertsEnabled = false;
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

    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }

    public void setAlertsEnabled(boolean alertsEnabled) {
        this.alertsEnabled = alertsEnabled;
    }
}
