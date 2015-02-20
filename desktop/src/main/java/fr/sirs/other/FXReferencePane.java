
package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.ReferenceChecker;
import static fr.sirs.SIRS.ICON_CHECK_CIRCLE;
import static fr.sirs.SIRS.ICON_EXCLAMATION_CIRCLE;
import fr.sirs.Session;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReferencePane extends BorderPane {
    
    private final ReferencePojoTable references;
    private final Session session = Injector.getSession();
    protected final ReferenceChecker referenceChecker;
        
    public FXReferencePane(final Class<ReferenceType> type) {
        final ResourceBundle bundle = ResourceBundle.getBundle(type.getName());
        referenceChecker = session.getReferenceChecker();
        references = new ReferencePojoTable(type, bundle.getString("class"));
        references.editableProperty().set(false);
        references.fichableProperty().set(false);
        references.detaillableProperty().set(false);
        references.searchableProperty().set(false);
        this.setCenter(references);
    }
    
    
    private class ReferencePojoTable extends PojoTable{
        
        private final Map<Object, Object> incoherentReferences;
        private final List<Object> serverInstanceNotLocal;
        private final List<Object> localInstancesNotOnTheServer;

        public ReferencePojoTable(Class pojoClass, String title) {
            super(pojoClass, title);
            
            referenceChecker.getIncoherentReferences().get(pojoClass);
            
            incoherentReferences = referenceChecker.getIncoherentReferences().get(pojoClass);
            serverInstanceNotLocal = referenceChecker.getServerInstancesNotLocal().get(pojoClass);
            localInstancesNotOnTheServer = referenceChecker.getLocalInstancesNotOnTheServer().get(pojoClass);
            
            uiTable.getColumns().replaceAll(new UnaryOperator<TableColumn<Element, ?>>() {

                @Override
                public TableColumn<Element, ?> apply(TableColumn<Element, ?> t) {
                    if(t instanceof DeleteColumn) return new StateColumn();
                    else return t;
                }
            });
            
            uiTable.setRowFactory(new Callback<TableView<Element>, TableRow<Element>>() {

                @Override
                public TableRow<Element> call(TableView<Element> param) {
                    return new ReferenceTableRow();
                }
            });
            
            final ObservableList allItems = FXCollections.observableArrayList(repo.getAll());
            if(serverInstanceNotLocal!=null && !serverInstanceNotLocal.isEmpty()){
                final List<Element> newServerInstances = new ArrayList<>();
                for(final Object asObject : serverInstanceNotLocal){
                    if(asObject instanceof Element){
                        newServerInstances.add((Element) asObject);
                    }
                }
                allItems.addAll(newServerInstances);
            }
            
            setTableItems(new Supplier<ObservableList<Element>>() {

                @Override
                public ObservableList<Element> get() {
                    return allItems;
                }
            });
            
        }
    
        @Override
        public void setTableItems(Supplier<ObservableList<Element>> producer) {

            editableProperty().set(true);
            uiTable.setItems(producer.get());
            editableProperty().set(false);
        }


        private class ReferenceTableRow extends TableRow<Element>{

            @Override
            protected void updateItem(Element item, boolean empty) {
                super.updateItem(item, empty);
                
                if(item!=null){
                    // La mise à jour des références nouvelles et incohérentes est automatique.
//                    if(incoherentReferences!=null
//                            && incoherentReferences.get(item)!=null){
//                        getStyleClass().add("incoherentReferenceRow");
//                        
//                    } else if(serverInstanceNotLocal!=null
//                            && serverInstanceNotLocal.contains(item)){
//                        getStyleClass().add("newReferenceRow");
//                    } else 
                        if(localInstancesNotOnTheServer!=null
                            && localInstancesNotOnTheServer.contains(item)){
                        getStyleClass().add("deprecatedReferenceRow");
                    }
                    else{
                        getStyleClass().removeAll("incoherentReferenceRow", "newReferenceRow", "deprecatedReferenceRow");
                    }
                }
            }
        }
        
        
        private class StateButtonTableCell extends ButtonTableCell<Element, Object>{

            private final Node defaultGraphic;

            public StateButtonTableCell(Node graphic) {
                super(true, graphic, (Object t) -> true, new Function<Object, Object>() {
                    @Override
                    public Object apply(Object t) {

                        if (localInstancesNotOnTheServer != null
                                && localInstancesNotOnTheServer.contains(t)) {
                            final Tab tab = new FXFreeTab("Analyse de la base");
                            tab.setContent(new FXReferenceAnalysePane((ReferenceType)t));
                            Injector.getSession().getFrame().addTab(tab);
                        }
                        else{
                            new Alert(Alert.AlertType.INFORMATION, "Cette référence est à jour.", ButtonType.OK).showAndWait();
                        }
                        return t;
                    }
                });
                defaultGraphic = graphic;
            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty); 
                
                if(item!=null){
                    // La mise à jour des références incohérentes et nouvelles est automatique.
//                    if(incoherentReferences!=null 
//                            && incoherentReferences.get(item)!=null){
//                        button.setGraphic(new ImageView(ICON_EXCLAMATION_TRIANGLE));
//                        button.setText("Incohérente");
//                        decorate(true);
//                    } 
//                    else if(serverInstanceNotLocal!=null
//                            && serverInstanceNotLocal.contains(item)){
//                        button.setGraphic(new ImageView(ICON_DOWNLOAD));
//                        button.setText("Nouvelle");
//                        decorate(false);
//                    } 
//                    else 
                        if(localInstancesNotOnTheServer!=null
                            && localInstancesNotOnTheServer.contains(item)){
                        button.setGraphic(new ImageView(ICON_EXCLAMATION_CIRCLE));
                        button.setText("Dépréciée");
                    }
                    else{
                        button.setGraphic(defaultGraphic);
                        button.setText("À jour");
                    }
                }
            }
        }
        
        
    
    private class StateColumn extends TableColumn<Element,Object>{

        public StateColumn() {
            super("État");     
            setEditable(false);
            setSortable(false);
            setResizable(true);
            setPrefWidth(70);
//            setPrefWidth(24);
//            setMinWidth(24);
//            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_MOVEUP));
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Element, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(TableColumn.CellDataFeatures<Element, Object> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            
            
            
            
            setCellFactory(new Callback<TableColumn<Element, Object>, TableCell<Element, Object>>() {

                @Override
                public TableCell<Element, Object> call(TableColumn<Element,Object> param) {
                    
                    return new StateButtonTableCell(new ImageView(ICON_CHECK_CIRCLE)); 
                }
            });
        }  
    }
    }
}
