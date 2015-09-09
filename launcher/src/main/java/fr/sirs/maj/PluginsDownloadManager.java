package fr.sirs.maj;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.geotoolkit.gui.javafx.util.TaskManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire de téléchargements de plugins pour l'application.
 * Cet outil permet de lancer un ou plusieurs téléchargements en parallèle et permet d'afficher
 * un suivi de l'avancée générale.
 * Un {@linkplain #txtProperty indicateur texte} est mis à jour dynamiquement, exemple de contenu :
 * "Téléchargement 1/2 (16,07 Mo)".
 * Un message "Téléchargement terminé" apparaît lorsque l'ensemble des téléchargements ont été traités.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class PluginsDownloadManager {
    /**
     * Instance du manager à utiliser.
     */
    public static final PluginsDownloadManager INSTANCE = new PluginsDownloadManager();

    /**
     * Stocke la correspondance entre une tâche de téléchargement et la taille en méga octets
     * du plugin à télécharger.
     */
    private final Map<Task,Double> map = new HashMap<>();

    /**
     * Liste des tâches en cours.
     */
    private final ObservableList<Task> submittedTasks = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    /**
     * Texte à afficher à l'utilisateur.
     */
    private final ObjectProperty<String> txtProperty = new SimpleObjectProperty<>();

    /**
     * Indice de la tâche en cours d'exécution.
     */
    private int indexTask = 1;

    /**
     * Taille totale des téléchargements, en méga octets.
     */
    private double pluginsSize;

    /**
     * Nombre total de téléchargements à exécuter.
     */
    private int nbDl;

    /**
     * Format d'affichage de la taille en méga octets à télécharger.
     */
    private final NumberFormat decimalFormat = new DecimalFormat("0.##");

    private PluginsDownloadManager() {
        submittedTasks.addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> c) {
                while (c.next()) {
                    for (Task additem : c.getAddedSubList()) {
                        // On ajoute la taille du plugin courant au total à télécharger.
                        pluginsSize += map.get(additem);
                        nbDl++;
                    }
                }

                // Mise à jour du texte
                txtProperty.setValue("Téléchargement "+ indexTask +"/"+ nbDl +" ("+ decimalFormat.format(pluginsSize) +" Mo)");
            }
        });
    }

    /**
     * Mise dans la file d'attente de téléchargement d'une tâche.
     *
     * @param task La tâche de téléchargement.
     * @param pluginsSize La taille en méga octets à télécharger.
     */
    public void addToDlQueue(final Task task, final double pluginsSize) {
        final Task manageTask = TaskManager.INSTANCE.submit(task);
        map.put(manageTask, pluginsSize);
        submittedTasks.add(manageTask);
    }

    /**
     * Traitement de la fin d'exécution d'une tâche.
     *
     * @param task Tâche réalisée.
     */
    public void finished(Task task) {
        indexTask++;
        map.remove(task);
        submittedTasks.remove(task);
        if (submittedTasks.isEmpty()) {
            reset();
        }
    }

    /**
     * Indique s'il reste des tâches à traiter.
     *
     * @return {@code True} s'il reste des tâches de téléchargement en cours, {@code false}
     *         si tout est terminé.
     */
    public boolean hasPendingDl() {
        return !submittedTasks.isEmpty();
    }

    /**
     * La propriété texte à récupérer pour l'affichage à l'utilisateur.
     *
     * @return
     */
    public ObjectProperty<String> txtProperty() {
        return txtProperty;
    }

    /**
     * Réinitialise la file de téléchargements une fois la queue traitée.
     */
    private void reset() {
        indexTask = 1;
        pluginsSize = 0;
        nbDl = 0;
        txtProperty.setValue("Téléchargement terminé");
    }
}
