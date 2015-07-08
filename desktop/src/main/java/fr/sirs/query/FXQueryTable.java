
package fr.sirs.query;

import fr.sirs.core.model.SQLQuery;
import fr.sirs.core.model.SQLQueries;
import fr.sirs.SIRS;
import fr.sirs.util.SimpleButtonColumn;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.FXTableView;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.internal.GeotkFX;

/**
 * A panel which lists queries stored locally or imported from a file. It allows
 * for deletion and selection of multiple queries.
 * 
 * @author Alexis Manin
 * @author Johann Sorel
 */
public class FXQueryTable extends BorderPane{

    public final TableView<SQLQuery> table = new FXTableView<>();
    /** A button to import queries from a chosen properties file. */
    private final Button uiImportQueries = new Button("Importer");
    /** A button to export queries to a chosen properties file. */
    private final Button uiExportQueries = new Button("Exporter");
    
    public FXQueryTable(List<SQLQuery> queries) {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefSize(200, 400);
        FXUtilities.hideTableHeader(table);
        setLeft(table);
        
        table.setItems(FXCollections.observableList(queries));
        
        final TableColumn<SQLQuery,String> nameCol = new TableColumn<>();
        nameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SQLQuery, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SQLQuery, String> param) {
                return param.getValue().libelleProperty();
            }
        });
        
        table.getColumns().add(nameCol);
        table.getColumns().add(new DeleteColumn());
        
        table.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<SQLQuery>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends SQLQuery> c) {
                final SQLQuery sqlq = table.getSelectionModel().getSelectedItem();
                if(sqlq !=null){
                    setCenter(new FXQueryPane(sqlq));
                }else{
                    setCenter(null);
                }
            }
        });

        final HBox box = new HBox(10,uiImportQueries,uiExportQueries);
        box.setPadding(new Insets(10, 10, 10, 10));
        uiImportQueries.setTooltip(new Tooltip("Importer des requêtes SQL depuis un fichier."));
        uiImportQueries.setOnAction(this::importRequests);
        uiExportQueries.setTooltip(new Tooltip("Exporter les requêtes SQL dans un fichier."));
        uiExportQueries.setOnAction(this::exportRequests);
        setBottom(box);
        
        setPadding(new Insets(5));
    }
    
    private void importRequests(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Fichier à charger");
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Fichier de propriétés Java", ".properties"));
        File outputFile = chooser.showOpenDialog(null);
        if (outputFile != null) {
            try {
                table.getItems().addAll(SQLQueries.openQueryFile(outputFile.toPath()));
            } catch (IOException ex) {
                SIRS.LOGGER.log(Level.WARNING, "Impossible de sauvegarder les requêtes sélectionnées.", ex);
                GeotkFX.newExceptionDialog("Impossible de sauvegarder les requêtes sélectionnées.", ex).show();
            }
        }
    }

    private void exportRequests(ActionEvent e) {
        save();
        final List<SQLQuery> selected = table.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune requête sélectionnée pour l'export.", ButtonType.OK).show();
        } else {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Fichier d'export");
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Fichier de propriétés Java", ".properties"));
            File outputFile = chooser.showSaveDialog(null);
            if (outputFile != null) {
                try {
                    SQLQueries.saveQueriesInFile(selected, outputFile.toPath());
                } catch (IOException ex) {
                    SIRS.LOGGER.log(Level.WARNING, "Impossible de sauvegarder les requêtes sélectionnées.", ex);
                    GeotkFX.newExceptionDialog("Impossible de sauvegarder les requêtes sélectionnées.", ex).show();
                }
            }
        }
    }

    public void save(){
        try {
            SQLQueries.saveQueriesLocally(getLocalQueries());
        } catch (IOException ex) {
            // TODO : is logging sufficient ?
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    final SQLQuery getSelection() {
        return table.getSelectionModel().getSelectedItem();
    }

    /**
     * Get all queries of the current table which are not contained into CouchDB
     * database (system local and imported from a file).
     * 
     * @return A list of queries. Never null, but can be empty.
     */
    private List<SQLQuery> getLocalQueries() {
        return table.getItems().filtered((SQLQuery current)-> current.getId() == null);
    }
    
    public class DeleteColumn extends SimpleButtonColumn {

        public DeleteColumn() {
            super(GeotkFX.ICON_DELETE,
                    new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                        @Override
                        public ObservableValue call(TableColumn.CellDataFeatures param) {
                            return new SimpleObjectProperty<>(param.getValue());
                        }
                    },
                    (Object t) -> true, 
                    new Function() {

                        public Object apply(Object t) {
                            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?",
                                    ButtonType.NO, ButtonType.YES);
                            alert.setResizable(true);
                            final ButtonType res = alert.showAndWait().get();
                            if (ButtonType.YES == res) {
                                table.getItems().remove(t);
                                save();
                            }
                            return null;
                        }
                    },
                    null
            );
        }
    }
}
