
package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.ReferenceUsageRepository;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.ReferenceUsage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReferenceAnalysePane extends BorderPane {
    
    private TableView<ReferenceUsage> usages;
    private final Session session = Injector.getSession();
    private final Map<String, ResourceBundle> bundles = new HashMap<>();
        
    public FXReferenceAnalysePane(final ReferenceType reference) {
        final ResourceBundle bundle = ResourceBundle.getBundle(ReferenceUsage.class.getName());
        
        try {
            final Method getId = reference.getClass().getMethod("getId");
            final String id = (String) getId.invoke(reference);
            final ReferenceUsageRepository referenceUsageRepository = session.getReferenceUsageRepository();
            final List<ReferenceUsage> referenceUsages = referenceUsageRepository.getReferenceUsages(id);
            
            if(referenceUsages!=null && !referenceUsages.isEmpty()){
                usages = new TableView<>(FXCollections.observableArrayList(referenceUsages));
                usages.setEditable(false);
                
                final TableColumn<ReferenceUsage, String> propertyColumn = new TableColumn<>(bundle.getString("property"));
                propertyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ReferenceUsage, String>, ObservableValue<String>>() {

                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ReferenceUsage, String> param) {
                        return new SimpleObjectProperty<>(param.getValue().getProperty());
                    }
                });
                usages.getColumns().add(propertyColumn);
                
                final TableColumn<ReferenceUsage, String> objectIdColumn = new TableColumn<>(bundle.getString("objectId"));
                objectIdColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ReferenceUsage, String>, ObservableValue<String>>() {

                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ReferenceUsage, String> param) {
                        return new SimpleObjectProperty(param.getValue().getObjectId());
                    }
                });
                usages.getColumns().add(objectIdColumn);
                
                final TableColumn<ReferenceUsage, String> typeColumn = new TableColumn<>(bundle.getString("type"));
                typeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ReferenceUsage, String>, ObservableValue<String>>() {

                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ReferenceUsage, String> param) {
                        String type = param.getValue().getType();
                        if(bundles.get(type)==null) bundles.put(type, ResourceBundle.getBundle(type));
                        type = bundles.get(type).getString("class");
                        return new SimpleObjectProperty(type);
                    }
                });
                usages.getColumns().add(typeColumn);
                
                final TableColumn<ReferenceUsage, String> labelColumn = new TableColumn<>(bundle.getString("label"));
                labelColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ReferenceUsage, String>, ObservableValue<String>>() {

                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ReferenceUsage, String> param) {
                        return new SimpleObjectProperty(param.getValue().getLabel());
                    }
                });
                usages.getColumns().add(labelColumn);
                setCenter(usages);
                
                final ResourceBundle topBundle = ResourceBundle.getBundle(reference.getClass().getName());
                final Label uiTitle = new Label("Usages dans la base de la référence \""+ ((AvecLibelle)reference).getLibelle()+"\" ("+topBundle.getString("class")+")");
                uiTitle.getStyleClass().add("pojotable-header");
                uiTitle.setAlignment(Pos.CENTER);
                uiTitle.setPadding(new Insets(5));
                uiTitle.setPrefWidth(USE_COMPUTED_SIZE);
                setTop(uiTitle);
                
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            SIRS.LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    
}
