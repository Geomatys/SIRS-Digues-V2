
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.ReferenceChecker;
import fr.sirs.Session;
import fr.sirs.core.model.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
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
        
    public FXReferencePane(final Class type) {
        final ResourceBundle bundle = ResourceBundle.getBundle(type.getName());
        referenceChecker = session.getReferenceChecker();
        references = new ReferencePojoTable(type, bundle.getString("class"));
        references.editableProperty().bind(session.nonGeometryEditionProperty());
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
            
            if(serverInstanceNotLocal!=null && !serverInstanceNotLocal.isEmpty()){
                final List<Element> newServerInstances = new ArrayList<>();
                for(final Object asObject : serverInstanceNotLocal){
                    if(asObject instanceof Element){
                        newServerInstances.add((Element) asObject);
                    }
                }
                getAllValues().addAll(newServerInstances);
            }
        }
        
        
        
        public class ReferenceTableRow extends TableRow<Element>{

            @Override
            protected void updateItem(Element item, boolean empty) {
                super.updateItem(item, empty);
                
                if(item!=null && incoherentReferences!=null){
                    if(incoherentReferences.get(item)!=null){
                        setBackground(new Background(new BackgroundFill(Color.BISQUE, null, null)));
                    } else if(serverInstanceNotLocal.contains(item)){
                        setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN, null, null)));
                    } else if(localInstancesNotOnTheServer.contains(item)){
                        setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
                    }
                }
            }
        }
        
        
        public class StateButtonTableCell extends ButtonTableCell<Element, Object>{

            public StateButtonTableCell(Node graphic) {
                super(true, graphic, (Object t) -> true, new Function<Object, Object>() {
                                @Override
                                public Object apply(Object t) {
                                    
//                    editFct.accept(t);
                                    return t;
                                }
                            });
            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty); 
                
                if(item!=null && incoherentReferences!=null){
                    if(incoherentReferences.get(item)!=null){
                        button.setGraphic(new ImageView(GeotkFX.ICON_DUPLICATE));
                        button.setText("Incohérente");
                    } else if(serverInstanceNotLocal.contains(item)){
                        button.setGraphic(new ImageView(GeotkFX.ICON_NEW));
                        button.setText("Nouvelle");
                    } else if(localInstancesNotOnTheServer.contains(item)){
                        button.setGraphic(new ImageView(GeotkFX.ICON_DELETE));
                        button.setText("Dépréciée");
                    }else{
                        button.setText("À jour");
                    }
                }
            }
            
            
            
        }
        
        
    
    public class StateColumn extends TableColumn<Element,Object>{

        public StateColumn() {
            super("Edition");        
            setSortable(false);
            setResizable(false);
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

                public TableCell<Element, Object> call(TableColumn<Element,Object> param) {
                    
                    
                    
                    ButtonTableCell button = new StateButtonTableCell(
                            new ImageView(GeotkFX.ICON_EDIT)); 
                    return button;
                }
            });
            
            
//            setCellFactory(new Callback<TableColumn<Element, Object>, TableCell<Element, Object>>() {
//
//                public TableCell<Element, Object> call(TableColumn<Element,Object> param) {
//                    return new ButtonTableCell(
//                            false,new ImageView(GeotkFX.ICON_EDIT), (Object t) -> true, new Function<Object, Object>() {
//                                @Override
//                                public Object apply(Object t) {
////                    editFct.accept(t);
//                                    return t;
//                                }
//                            }); }
//            });
        }  
    }
    }
}
