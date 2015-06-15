
package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Positionable;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collection;
import java.util.EventObject;
import java.util.Set;
import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
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
import org.opengis.util.GenericName;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXImportCoordinate extends FXAbstractImportCoordinate {
    
    @FXML private ComboBox<PropertyType> uiAttX;
    @FXML private ComboBox<PropertyType> uiAttY;
    
    private final ObjectProperty<Feature> selectionProperty = new SimpleObjectProperty<>();
    private final Positionable positionable;
    
    public FXImportCoordinate(Positionable pos) {
        super();
        this.positionable = pos;
        
        uiAttX.setConverter(stringConverter);
        uiAttY.setConverter(stringConverter);
        
        uiPaneImport.disableProperty().bind(selectionProperty.isNull());        
    }

    @FXML
    void openFeatureStore(ActionEvent event) {
        final String url = uiPath.getText();
        final File file = new File(uiPath.getText());
        
        uiPaneConfig.setDisable(true);
        
        selectionProperty.set(null);
        
        try{
            if(url.toLowerCase().endsWith(".shp")){
                store = new ShapefileFeatureStore(file.toURI().toURL(), "no namespace");
                
            }else if(url.toLowerCase().endsWith(".txt") || url.toLowerCase().endsWith(".csv")){
                final char separator = (uiSeparator.getText().isEmpty()) ? ';' : uiSeparator.getText().charAt(0);
                store = new CSVFeatureStore(file, "no namespace", separator);
                
            }else{
                final Alert alert = new Alert(Alert.AlertType.ERROR, "Le fichier sélectionné n'est pas un shp, csv ou txt", ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();
                return;
            }
            
            final Session session = store.createSession(true);
            final Set<GenericName> typeNames = store.getNames();
            if (typeNames == null || typeNames.isEmpty()) {
                throw new IllegalArgumentException("Impossible de trouver des données dans le fichier d'entrée.");
            }
            final GenericName typeName = typeNames.iterator().next();
            final FeatureCollection col = session.getFeatureCollection(QueryBuilder.all(typeName));
            final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col, RandomStyleBuilder.createDefaultVectorStyle(col.getFeatureType()));
            uiTable.init(layer);
            
            // Activate property choice only when no geometry field can be found.
            if (col.getFeatureType().getGeometryDescriptor() == null) {
                uiPaneConfig.setDisable(false);
            } else {
                uiPaneConfig.setDisable(true);
            }
            
            //liste des propriétés
            final Collection<? extends PropertyType> properties = col.getFeatureType().getProperties(true);
            uiAttX.setItems(FXCollections.observableArrayList(properties));
            uiAttY.setItems(FXCollections.observableArrayList(properties));
            
            if(!properties.isEmpty()){
                uiAttX.getSelectionModel().clearAndSelect(0);
                uiAttY.getSelectionModel().clearAndSelect(0);
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
                    
                    selectionProperty.set(null);
                    final Id filter = layer.getSelectionFilter();
                    try {
                        final FeatureCollection selection = layer.getCollection().subCollection(QueryBuilder.filtered(typeName, filter));
                        final FeatureIterator iterator = selection.iterator();
                        while(iterator.hasNext()){
                            selectionProperty.set(iterator.next());
                            break;
                        }
                        iterator.close();
                    } catch (DataStoreException ex) {
                        SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                    }
                }
            });
            
        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            final Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }
        
    }
    
    @FXML
    void importStart(ActionEvent event) {
        final Point pt = getSelectionPoint();
        if(pt==null) return;
        positionable.setPositionDebut(pt);
    }

    @FXML
    void importEnd(ActionEvent event) {
        final Point pt = getSelectionPoint();
        if(pt==null) return;
        positionable.setPositionFin(pt);
    }
    
    private Point getSelectionPoint(){
        final Feature feature = selectionProperty.get();
        
        Point geom;
        final CoordinateReferenceSystem dataCrs;
                
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
            final fr.sirs.Session session = Injector.getSession();
            final MathTransform trs = CRS.findMathTransform(dataCrs, session.getProjection(), true);
            geom = (Point) JTS.transform(geom, trs);
            JTS.setCRS(geom, session.getProjection());

        }catch(TransformException | FactoryException ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            final Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
        }

        return geom;
    }
}
