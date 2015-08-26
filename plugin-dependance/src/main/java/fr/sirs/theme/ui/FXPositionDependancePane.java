package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.AbstractDependance;
import fr.sirs.map.FXMapTab;
import fr.sirs.plugin.dependance.map.DependanceEditHandler;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.GenericName;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;


/**
 *
 */
public class FXPositionDependancePane extends BorderPane {
    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);
    private final ObjectProperty<AbstractDependance> dependance = new SimpleObjectProperty<>();

    public FXPositionDependancePane() {
        SIRS.loadFXML(this);
    }

    public AbstractDependance getDependance() {
        return dependance.get();
    }

    public ObjectProperty<AbstractDependance> dependanceProperty() {
        return dependance;
    }

    @FXML
    public void drawOnMap() {
        final FXMapTab tab = Injector.getSession().getFrame().getMapTab();
        tab.show();
        tab.getMap().getUiMap().setHandler(new DependanceEditHandler());
    }

    @FXML
    public void importGeometry() {
        final FileChooser fileChooser = new FileChooser();
        final File shpFile = fileChooser.showOpenDialog(null);
        final FXFeatureTable shpTable = new FXFeatureTable();
        shpTable.setLoadAll(true);


        try {
            final URL shpUrl = shpFile.toURI().toURL();
            final FeatureStore shpStore = new ShapefileFeatureStore(shpUrl);

            final GenericName name = shpStore.getNames().iterator().next();
            final FeatureMapLayer mapLayer = MapBuilder.createFeatureLayer(
                    shpStore.createSession(true).getFeatureCollection(QueryBuilder.all(name)));
            shpTable.init(mapLayer);

            final Stage stage = new Stage();
            final Scene scene = new Scene(shpTable);
            stage.setScene(scene);
            stage.showAndWait();

            final FeatureCollection ids = shpStore.createSession(true).getFeatureCollection(
                    QueryBuilder.filtered(name, mapLayer.getSelectionFilter()));
            try (final FeatureIterator it = ids.iterator()) {
                if (it.hasNext()) {
                    final Feature feature = it.next();
                    Geometry geom = (Geometry) feature.getDefaultGeometryProperty().getValue();
                    geom = JTS.transform(geom, Injector.getSession().getProjection());
                    dependance.get().setGeometry(geom);
                }
            }
        } catch (DataStoreException | TransformException | FactoryException | MalformedURLException ex) {
            GeotkFX.newExceptionDialog(ex.getLocalizedMessage(), ex);
            SIRS.LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }

    public BooleanProperty disableFieldsProperty(){
        return disableFieldsProperty;
    }
}
