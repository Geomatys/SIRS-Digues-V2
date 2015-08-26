package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.AbstractDependance;
import fr.sirs.map.FXMapTab;
import fr.sirs.plugin.dependance.map.DependanceEditHandler;
import fr.sirs.ui.Growl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
 * Panneau de positionnement d'une dépendance, permettant d'éditer sa géométrie sur la carte ou
 * d'en importer une pour cette dépendance.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class FXPositionDependancePane extends BorderPane {
    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);

    /**
     * La dépendance à éditer.
     */
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
        tab.getMap().getUiMap().setHandler(new DependanceEditHandler(dependance.get()));
    }

    /**
     * Importe une géométrie provenant d'un fichier SHP comme géométrie de la dépendance.
     */
    @FXML
    public void importGeometry() {
        final FileChooser fileChooser = new FileChooser();
        // Demande du fichier SHP à considérer
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

            // Affichage d'une popup présentant les features contenues dans le SHP
            final Stage stage = new Stage();
            final HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(10));
            HBox.setHgrow(hBox, Priority.ALWAYS);
            final Button validateBtn = new Button("Valider");
            validateBtn.setOnAction(event -> stage.close());
            hBox.getChildren().add(validateBtn);
            final BorderPane mainPane = new BorderPane(shpTable, null, null, hBox, null);
            final Scene scene = new Scene(mainPane);
            stage.setScene(scene);
            stage.setTitle("Choisir une géométrie à importer");
            stage.getIcons().add(SIRS.ICON);
            stage.showAndWait();

            // La feature sélectionnée dans la table de la popup précédente sera utilisée comme géométrie de cette dépendance.
            final FeatureCollection ids = shpStore.createSession(true).getFeatureCollection(
                    QueryBuilder.filtered(name, mapLayer.getSelectionFilter()));
            try (final FeatureIterator it = ids.iterator()) {
                if (it.hasNext()) {
                    final Feature feature = it.next();
                    Geometry geom = (Geometry) feature.getDefaultGeometryProperty().getValue();
                    geom = JTS.transform(geom, Injector.getSession().getProjection());
                    dependance.get().setGeometry(geom);

                    final Growl successGrowl = new Growl(Growl.Type.INFO, "Géométrie importée avec succès");
                    successGrowl.showAndFade();
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
