
package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.PointLeveXYZ;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.XYZLeveProfilTravers;
import fr.sirs.core.model.XYZProfilLong;
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
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.LayerListener;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.util.collection.CollectionChangeEvent;
import org.opengis.feature.PropertyType;
import org.opengis.filter.Id;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXImportXYZ extends FXAbstractImportPointLeve<PointLeveXYZ> {
    
    @FXML private ComboBox<PropertyType> uiAttX;
    @FXML private ComboBox<PropertyType> uiAttY;
    
    FXImportXYZ(final PojoTable pojoTable) {
        super(pojoTable);
        
        uiAttX.setConverter(stringConverter);
        uiAttY.setConverter(stringConverter);
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
            uiAttX.setItems(FXCollections.observableArrayList(properties));
            uiAttY.setItems(FXCollections.observableArrayList(properties));
            uiAttZ.setItems(FXCollections.observableArrayList(properties));
            
            if(!properties.isEmpty()){
                uiAttDesignation.getSelectionModel().clearAndSelect(0);
                uiAttX.getSelectionModel().clearAndSelect(0);
                uiAttY.getSelectionModel().clearAndSelect(0);
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
    protected ObservableList<PointLeveXYZ> getSelectionPoint(){
        final ObservableList<Feature> features = selectionProperty;
        final ObservableList<PointLeveXYZ> leves = FXCollections.observableArrayList();
        
        for(final Feature feature : features){
            final PointLeveXYZ leve;
            
            if(pojoTable.getParentElement() instanceof LeveProfilTravers){
                leve = Injector.getSession().getElementCreator().createElement(XYZLeveProfilTravers.class);
            } else if(pojoTable.getParentElement() instanceof ProfilLong){
                leve = Injector.getSession().getElementCreator().createElement(XYZProfilLong.class);
            } else {
                throw new UnsupportedOperationException("Type d'élément parent inconnu pour les points de levé.");
            }
            
            Point geom;
            final CoordinateReferenceSystem dataCrs;

            // X/Y
            if(uiPaneConfig.isDisable()){
                //shapefile
                geom = ((Geometry)feature.getDefaultGeometryProperty().getValue()).getCentroid();
                dataCrs = feature.getType().getCoordinateReferenceSystem();

            }else{
                //csv
                final String attX = String.valueOf(feature.getPropertyValue(uiAttX.getValue().getName().tip().toString()));
                final String attY = String.valueOf(feature.getPropertyValue(uiAttY.getValue().getName().tip().toString()));
                geom = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(Double.valueOf(attX), Double.valueOf(attY)));
                dataCrs = uiCRS.getValue();
            }

            //transform to RGF93 
            try{
                final MathTransform trs = CRS.findMathTransform(dataCrs, SirsCore.getEpsgCode(), true);
                geom = (Point) JTS.transform(geom, trs);
                JTS.setCRS(geom, SirsCore.getEpsgCode());

            }catch(TransformException | FactoryException ex){
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
            }
            leve.setX(geom.getX());
            leve.setY(geom.getY());
            
            // Z
            leve.setZ(Double.valueOf(String.valueOf(feature.getPropertyValue(uiAttZ.getValue().getName().tip().toString()))));
            
            leve.setDesignation(String.valueOf(feature.getPropertyValue(uiAttDesignation.getValue().getName().tip().toString())));
            
            leve.setAuthor(Injector.getSession().getUtilisateur().getId());
            leve.setValid(!(Injector.getSession().getRole()==Role.EXTERN));
            leves.add(leve);
        }
        return leves;
    }
}
