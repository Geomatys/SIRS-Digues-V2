
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.PointLeveDZ;
import fr.sirs.core.model.Role;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collection;
import java.util.EventObject;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.csv.CSVFeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.LayerListener;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.util.collection.CollectionChangeEvent;
import org.opengis.feature.PropertyType;
import org.opengis.filter.Id;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXImportDZ extends FXAbstractImportPointLeve<PointLeveDZ> {
    
    @FXML private ComboBox<PropertyType> uiAttD;
    
    FXImportDZ(final PojoTable pojoTable) {
        super(pojoTable);
        
        uiAttD.setConverter(stringConverter);
        uiCRS.setDisable(true);
    }

    @FXML
    void openFeatureStore(ActionEvent event) {
        final String url = uiPath.getText();
        final File file = new File(uiPath.getText());
        
        uiPaneConfig.setDisable(true);
        
        selectionProperty.removeAll(selectionProperty);
        
        try{
            if(url.toLowerCase().endsWith(".shp")){
                store = new ShapefileFeatureStore(file.toURI().toURL(), "no namespace");
                uiPaneConfig.setDisable(true);
            }else if(url.toLowerCase().endsWith(".txt") || url.toLowerCase().endsWith(".csv")){
                final char separator = (uiSeparator.getText().isEmpty()) ? ';' : uiSeparator.getText().charAt(0);
                store = new CSVFeatureStore(file, "no namespace", separator);
                uiPaneConfig.setDisable(false);
            }else{
                new Alert(Alert.AlertType.ERROR, "Le fichier sélectionné n'est pas un shp, csv ou txt", ButtonType.OK).showAndWait();
                return;
            }
            
            final Session session = store.createSession(true);
            final Name typeName = store.getNames().iterator().next();
            final FeatureCollection col = session.getFeatureCollection(QueryBuilder.all(typeName));
            final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col, RandomStyleBuilder.createDefaultVectorStyle(col.getFeatureType()));
            uiTable.init(layer);
            
            //liste des propriétés
            final Collection<? extends PropertyType> properties = col.getFeatureType().getProperties(true);
            uiAttDesignation.setItems(FXCollections.observableArrayList(properties));
            uiAttD.setItems(FXCollections.observableArrayList(properties));
            uiAttZ.setItems(FXCollections.observableArrayList(properties));
            
            if(!properties.isEmpty()){
                uiAttDesignation.getSelectionModel().clearAndSelect(0);
                uiAttD.getSelectionModel().clearAndSelect(0);
                uiAttZ.getSelectionModel().clearAndSelect(0);
            }
            
            //on ecoute la selection
            layer.addLayerListener(new LayerListener() {
                @Override
                public void styleChange(MapLayer source, EventObject event) {}
                @Override
                public void itemChange(CollectionChangeEvent<MapItem> event) {}
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(!FeatureMapLayer.SELECTION_FILTER_PROPERTY.equals(evt.getPropertyName())) return;
                    
                    selectionProperty.removeAll(selectionProperty);
                    final Id filter = layer.getSelectionFilter();
                    try {
                        final FeatureCollection selection = layer.getCollection().subCollection(QueryBuilder.filtered(typeName, filter));
                        final FeatureIterator iterator = selection.iterator();
                        while(iterator.hasNext()){
                            selectionProperty.add(iterator.next());
                        }
                        iterator.close();
                    } catch (DataStoreException ex) {
                        SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                    }
                }
            });
            
        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
            return;
        }
        
    }
    
    @Override
    protected ObservableList<PointLeveDZ> getSelectionPoint(){
        final ObservableList<Feature> features = selectionProperty;
        final ObservableList<PointLeveDZ> leves = FXCollections.observableArrayList();
        
        for(final Feature feature : features){
            final PointLeveDZ leve = new PointLeveDZ();
            
            // DZ
            leve.setD(Double.valueOf(String.valueOf(feature.getPropertyValue(uiAttD.getValue().getName().tip().toString()))));
            leve.setZ(Double.valueOf(String.valueOf(feature.getPropertyValue(uiAttZ.getValue().getName().tip().toString()))));
            
            leve.setDesignation(String.valueOf(feature.getPropertyValue(uiAttDesignation.getValue().getName().tip().toString())));
            
            leve.setAuthor(Injector.getSession().getUtilisateur().getId());
            leve.setValid(!(Injector.getSession().getRole()==Role.EXTERN));
            leves.add(leve);
        }
        return leves;
    }
}
