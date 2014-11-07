
package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.SIRS;
import fr.sirs.core.model.Positionable;
import fr.sirs.util.SirsListCell;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collection;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.csv.CSVFeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
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
 * @author Johann Sorel (Geomatys)
 */
public class FXImportCoordinate extends BorderPane {
    
    @FXML private TextField uiPath;
    @FXML private TextField uiSeparator;
    
    @FXML private ComboBox<CoordinateReferenceSystem> uiCRS;
    @FXML private ComboBox<PropertyType> uiAttX;
    @FXML private ComboBox<PropertyType> uiAttY;
    @FXML private FXFeatureTable uiTable;
    
    @FXML private GridPane uiPaneConfig;
    @FXML private GridPane uiPaneImport;

    private FeatureStore store;
    
    private final ObjectProperty<Feature> selectionProperty = new SimpleObjectProperty<>();
    private final Positionable positionable;
    
    public FXImportCoordinate(Positionable pos) {
        SIRS.loadFXML(this);
        this.positionable = pos;
        
        uiCRS.setItems(FXCollections.observableArrayList(FXPositionnablePane.CRS_RGF93, FXPositionnablePane.CRS_WGS84));
        uiCRS.setCellFactory((ListView<CoordinateReferenceSystem> param) -> new SirsListCell());
        uiCRS.setButtonCell(new SirsListCell());
        uiCRS.getSelectionModel().clearAndSelect(0);
        uiAttX.setCellFactory((ListView<PropertyType> param) -> new SirsListCell());
        uiAttX.setButtonCell(new SirsListCell());
        uiAttY.setCellFactory((ListView<PropertyType> param) -> new SirsListCell());
        uiAttY.setButtonCell(new SirsListCell());
        
        uiPaneConfig.setDisable(true);
        uiTable.setEditable(false);
        uiTable.setLoadAll(true);
        
        uiPaneImport.disableProperty().bind(selectionProperty.isNull());        
    }

    @FXML
    void openFileChooser(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File prevPath = getPreviousPath();
        if (prevPath != null) {
            fileChooser.setInitialDirectory(prevPath);
        }
        final File file = fileChooser.showOpenDialog(getScene().getWindow());
        if(file!=null){
            setPreviousPath(file.getParentFile());
            uiPath.setText(file.getAbsolutePath());
        }
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
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
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
            final MathTransform trs = CRS.findMathTransform(dataCrs, FXPositionnablePane.CRS_RGF93, true);
            geom = (Point) JTS.transform(geom, trs);
            JTS.setCRS(geom, FXPositionnablePane.CRS_RGF93);

        }catch(TransformException | FactoryException ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }

        return geom;
    }
    
    
    private static File getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(FXImportCoordinate.class);
        final String str = prefs.get("path", null);
        if(str!=null){
            final File file = new File(str);
            if(file.isDirectory()){
                return file;
            }
        }
        return null;
    }

    private static void setPreviousPath(final File path) {
        final Preferences prefs = Preferences.userNodeForPackage(FXImportCoordinate.class);
        prefs.put("path", path.getAbsolutePath());
    }
    
}
