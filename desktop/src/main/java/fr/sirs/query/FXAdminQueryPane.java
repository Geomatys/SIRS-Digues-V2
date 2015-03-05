package fr.sirs.query;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.TaskManager;
import fr.sirs.core.component.SQLQueryRepository;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.SQLQueries;
import fr.sirs.core.model.SQLQuery;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A panel which allows an administrator to send locally saved queries into CouchDB,
 * or edit and remove queries.
 * 
 * /!\ WARNING : All modifications will be saved only if {@link #saveQueries() }
 * method is called.
 * 
 * TODO : Add methods to find doublons in each list, or common to lists.
 * TODO : add form at panel bottom to allow query edition.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class FXAdminQueryPane extends BorderPane {
    
    @FXML
    private Button uiToDatabaseBtn;

    @FXML
    private ListView<SQLQuery> uiDBList;

    @FXML
    private Button uiToLocalBtn;

    @FXML
    private ListView<SQLQuery> uiLocalList;

    @FXML
    private Button uiDeleteDBBtn;

    @FXML
    private Button uiDeleteLocalBtn;

    @FXML
    private Button uiLocalEditBtn;
    
    // Notification lists. Contains element to add or remove at save.
    private HashSet<SQLQuery> toAddInDB = new HashSet<>();
    private HashMap<String, SQLQuery> toRemoveFromDB = new HashMap<>();
    
    public FXAdminQueryPane() throws IOException {
        super();
        if (!Role.ADMIN.equals(Injector.getSession().getRole())) {
            new Alert(Alert.AlertType.ERROR, "Ce panneau est reservé aux administrateurs !", ButtonType.OK).show();
            setCenter(new Label("Seuls les administrateurs peuvent utiliser ce panneau."));
            return;
        }
        
        SIRS.loadFXML(this);
        
        // TOOLTIP DEFINITION
        uiToDatabaseBtn.setTooltip(new Tooltip("Déplacer la sélection en base de données."));
        uiToLocalBtn.setTooltip(new Tooltip("Déplacer la sélection vers le stockage local."));
        uiDeleteDBBtn.setTooltip(new Tooltip("Supprimer la sélection de la base de données."));
        uiDeleteLocalBtn.setTooltip(new Tooltip("Supprimer la sélection du système local."));
        
        uiLocalList.setCellFactory(new SQLQueries.QueryListCellFactory());
        uiDBList.setCellFactory(new SQLQueries.QueryListCellFactory());
        
        // Fill query lists
        ObservableList<SQLQuery> localQueries = FXCollections.observableArrayList(SQLQueries.getLocalQueries());
        uiLocalList.setItems(localQueries);
        
        ObservableList<SQLQuery> dbQueries = FXCollections.observableArrayList(
                Injector.getSession().getSqlQueryRepository().getAll());
        uiDBList.setItems(dbQueries);
        
        // Listen on database list to know which elements we must update.
        uiDBList.getItems().addListener((ListChangeListener.Change<? extends SQLQuery> c) -> {
            while(c.next()) {
                if (c.wasAdded()) {
                    toAddInDB.addAll(c.getAddedSubList());
                }
                
                if (c.wasRemoved()) {
                    for (final SQLQuery query : c.getRemoved()) {
                        // Notify that we must delete query. If the query has no ID, 
                        // It's a query which has already switched of list since last update.
                        if (query.getId() == null) {
                            toAddInDB.remove(query);
                        } else {
                            toRemoveFromDB.put(query.getId(), query);                            
                        }
                    }
                }
            }
        });
    }

    /**
     * Save queries (re)moved using the panel. It means insertions / deletion in
     * couchDB and local system properties.
     */
    void saveQueries() {
        final Task t = new Task() {

            @Override
            protected Object call() throws Exception {
                updateTitle("Sauvegarde de requêtes SQL");
                
                updateMessage("Sauvegarde des requêtes locales.");
                SQLQueries.saveQueriesLocally(uiLocalList.getItems());

                updateMessage("Mise à jour des requêtes dans la base de données.");
                // Update database. For each element updated, we can remove it from notification lists.
                SQLQueryRepository queryRepo = Injector.getSession().getSqlQueryRepository();
                Iterator<SQLQuery> addIt = toAddInDB.iterator();
                while (addIt.hasNext()) {
                    queryRepo.add(addIt.next());
                    addIt.remove();
                }

                Iterator<SQLQuery> removeIt = toRemoveFromDB.values().iterator();
                while (removeIt.hasNext()) {
                    queryRepo.add(removeIt.next());
                    removeIt.remove();
                }
                return null;
            }
        };
        
        TaskManager.INSTANCE.submit(t);
        
    }

    @FXML
    void localToDatabase(ActionEvent event) {
        transferFromListToList(uiLocalList, uiDBList);
    }

    @FXML
    void databaseToLocal(ActionEvent event) {
        transferFromListToList(uiDBList, uiLocalList);
    }
    
    /**
     * Move (not copy) selected items of one list to another.
     * @param source The list to get and cut selection from.
     * @param destination The destination to put selection into.
     */
    private void transferFromListToList(final ListView source, ListView destination) {
        // Get list selection and remove it
        ObservableList<SQLQuery> selectedItems = source.getSelectionModel().getSelectedItems();
        source.getItems().removeAll(selectedItems);
        // add it in destination 
        destination.getItems().addAll(selectedItems);
    }
    
    /**
     * Show a dialog for query transfer. Return only if user has saved or cancelled changes.
     */
    public static void showAndWait() {
        final Stage dialog = new Stage();
        dialog.setResizable(true);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(Injector.getSession().getFrame().getScene().getWindow());

        final FXAdminQueryPane adminPanel;
        try {
            adminPanel = new FXAdminQueryPane();
        } catch (IOException ex) {
            SIRS.LOGGER.log(Level.WARNING, null, ex);
            SIRS.newExceptionDialog("Une erreur est survenue lors de la construction des listes de requêtes.", ex).show();
            return;
        }

        final Button cancelBtn = new Button("Annuler");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction((ActionEvent e) -> dialog.close());

        final Button saveBtn = new Button("Sauvegarder");
        saveBtn.setOnAction((ActionEvent e) -> {
            adminPanel.saveQueries();
            dialog.close();
        });

        final BorderPane dialogPane = new BorderPane(adminPanel);
        final ButtonBar hBox = new ButtonBar();
        hBox.getButtons().addAll(cancelBtn, saveBtn);
        dialogPane.setBottom(hBox);

        dialog.setScene(new Scene(dialogPane));
        dialog.showAndWait();
    }
}
