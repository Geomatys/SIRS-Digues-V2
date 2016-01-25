package fr.sirs.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Collection;
import java.util.TreeSet;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;


/**
 * Gestionnaire d'alertes de l'application. Elles seront affichées dans une barre en bas de l'application.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class AlertManager {
    
    /**
     * Liste des alertes à afficher.
     */
    private final ObservableSet<AlertItem> alerts = FXCollections.observableSet(new TreeSet<>());

    /**
     * Définit si les alertes doivent être affichées dans l'application ou non.
     */
    private final ReadOnlyBooleanWrapper alertsEnabled = new ReadOnlyBooleanWrapper();

    private static AlertManager manager = null;

    private AlertManager() {
        alertsEnabled.bind(Bindings.isNotEmpty(alerts));
    }

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
        return alertsEnabled.get();
    }

    /**
     *
     * @return A read-only property indicating if alerts are currently present in this manager.
     */
    public ReadOnlyBooleanProperty alertsEnabledProperty() {
        // return a read-only property to be sure user won't attempt to mess with its value.
        return alertsEnabled.getReadOnlyProperty();
    }

    /**
     * Ajoute les alertes fournies à la pile d'alertes à afficher, et actives l'affichage du panneau.
     *
     * @param alerts Liste des alertes à afficher.
     */
    public void addAlerts(final Collection<AlertItem> alerts) {
        manager.getAlerts().addAll(alerts);
    }

    /**
     * Supprime les alertes pour le parent donné.
     *
     * @param parent Le parent des alertes à supprimer.
     */
    public void removeAlertsForParent(final Object parent) {
        alerts.removeIf(alertItem -> alertItem.getParent() != null && alertItem.getParent().equals(parent));
    }
}
