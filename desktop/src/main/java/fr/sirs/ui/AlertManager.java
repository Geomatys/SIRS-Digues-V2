package fr.sirs.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;


/**
 * Gestionnaire d'alertes de l'application. Elles seront affichées dans une barre en bas de l'application.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class AlertManager {
    /**
     * Liste des alertes à afficher.
     */
    private final ObservableSet<AlertItem> alerts = FXCollections.observableSet(new HashSet<>());

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

    public ObservableSet<AlertItem> getAlerts() {
        return alerts;
    }

    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }

    public void setAlertsEnabled(boolean alertsEnabled) {
        this.alertsEnabled = alertsEnabled;
    }

    /**
     * Ajoute les alertes fournies à la pile d'alertes à afficher, et actives l'affichage du panneau.
     *
     * @param alerts Liste des alertes à afficher.
     */
    public void addAlerts(final Collection<AlertItem> alerts) {
        manager.setAlertsEnabled(true);
        manager.getAlerts().addAll(alerts);
    }
}
