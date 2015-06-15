package fr.sirs.query;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.component.SQLQueryRepository;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.SQLQueries;
import fr.sirs.core.model.SQLQuery;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.geotoolkit.internal.GeotkFX;

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
    private BorderPane uiBottomPane;
    
    private FXQueryPane queryEditor;
    
    // Notification lists. Contains element to add or remove at save.
    private final HashSet<SQLQuery> toAddInDB = new HashSet<>();
    private final HashMap<String, SQLQuery> toRemoveFromDB = new HashMap<>();
    private final HashSet<SQLQuery> toUpdate = new HashSet<>();
    
    /** 
     * A copy of the last edited query. So we will submit edited query for update 
     * only if its not equal to its copy.
     */
    private SQLQuery initialState;
    
    public FXAdminQueryPane() throws IOException {
        super();
        if (!Role.ADMIN.equals(Injector.getSession().getRole())) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Ce panneau est reservé aux administrateurs !", ButtonType.OK);
            alert.setResizable(true);
            alert.show();
            setCenter(new Label("Seuls les administrateurs peuvent utiliser ce panneau."));
            return;
        }
        
        SIRS.loadFXML(this);
        
        // TOOLTIP DEFINITION
        uiToDatabaseBtn.setTooltip(new Tooltip("Déplacer la sélection en base de données."));
        uiToLocalBtn.setTooltip(new Tooltip("Déplacer la sélection vers le stockage local."));
        uiDeleteDBBtn.setTooltip(new Tooltip("Supprimer la sélection de la base de données."));
        uiDeleteLocalBtn.setTooltip(new Tooltip("Supprimer la sélection du système local."));
        
        uiLocalList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiDBList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
        
        // delete button actions
        uiDeleteLocalBtn.setOnAction((ActionEvent e)-> deleteSelection(uiLocalList));
        uiDeleteDBBtn.setOnAction((ActionEvent e)-> deleteSelection(uiDBList));
        
        queryEditor = new FXQueryPane();
        
        // As focused item is not cleared when a list lose focus, we're forced to make update ourself on click.
//        uiLocalList.getFocusModel().focusedItemProperty().addListener(this::updateEditor);
//        uiDBList.getFocusModel().focusedItemProperty().addListener(this::updateEditor);
        uiLocalList.setOnMouseClicked((MouseEvent e)-> updateEditor(null, null, uiLocalList.getFocusModel().getFocusedItem()));
        uiDBList.setOnMouseClicked((MouseEvent e)-> updateEditor(null, null, uiDBList.getFocusModel().getFocusedItem()));
        
        uiBottomPane.setCenter(queryEditor);        
    }

    /**
     * Update query editor content. This method has been designed to serve as 
     * changeListener on focused item of {@link #uiDBList} and {@link #uiLocalList}.
     * @param observable The ListView on which the focus has been requested.
     * @param oldValue The previously focused element.
     * @param newValue The current focused element.
     */
    private void updateEditor(ObservableValue<? extends SQLQuery> observable, SQLQuery oldValue, SQLQuery newValue) {
        if (newValue != null) {
            /* First, we check if last edited query has been modified. Check is needed
             * only for queries already inserted in database, because all local queries
             * will be updated at save, and queries moved to db will be added anyway.
             * Also, if the query is already triggered for deletion, there's no need 
             * for update.
             */
            final SQLQuery previouslyEdited = queryEditor.getSQLQuery();
            if (previouslyEdited != null && previouslyEdited.getId() != null) {
                if (!previouslyEdited.equals(initialState) && !toRemoveFromDB.containsKey(previouslyEdited.getId())) {
                    toUpdate.add(previouslyEdited);
                }
            }
            initialState = newValue.copy();
            queryEditor.setSQLQuery(newValue);
        }
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
                
                Iterator<SQLQuery> updateIt = toUpdate.iterator();
                while (updateIt.hasNext()) {
                    queryRepo.update(updateIt.next());
                    updateIt.remove();
                }
                
                Iterator<SQLQuery> removeIt = toRemoveFromDB.values().iterator();
                while (removeIt.hasNext()) {
                    queryRepo.remove(removeIt.next());
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
    
    boolean deleteSelection(final ListView source) {
        ObservableList<SQLQuery> selectedItems = source.getSelectionModel().getSelectedItems();
        return source.getItems().removeAll(selectedItems);
    }
    
    /**
     * Move (not copy) selected items of one list to another.
     * 
     * @param source The list to get and cut selection from.
     * @param destination The destination to put selection into.
     */
    private void transferFromListToList(final ListView source, final ListView destination) {
        // Get list selection
        ObservableList<SQLQuery> selectedItems = source.getSelectionModel().getSelectedItems();
        // add it in destination 
        destination.getItems().addAll(selectedItems);
        // remove from source list
        source.getItems().removeAll(selectedItems);
    }
    
    /**
     * Show a dialog for query transfer. Return only if user has saved or canceled changes.
     */
    public static void showAndWait() {
        final Stage dialog = new Stage();
        dialog.getIcons().add(SirsCore.ICON);
        dialog.setTitle("Administration des requêtes");
        dialog.setResizable(true);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(Injector.getSession().getFrame().getScene().getWindow());

        final FXAdminQueryPane adminPanel;
        try {
            adminPanel = new FXAdminQueryPane();
        } catch (IOException ex) {
            SIRS.LOGGER.log(Level.WARNING, null, ex);
            GeotkFX.newExceptionDialog("Une erreur est survenue lors de la construction des listes de requêtes.", ex).show();
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
        hBox.setPadding(new Insets(5));
        hBox.getButtons().addAll(cancelBtn, saveBtn);
        dialogPane.setBottom(hBox);

        dialog.setScene(new Scene(dialogPane));
        dialog.showAndWait();
    }
}
