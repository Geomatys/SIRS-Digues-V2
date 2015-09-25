package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.AbstractDependance;
import fr.sirs.map.FXMapTab;
import fr.sirs.plugin.dependance.map.DependanceEditHandler;
import fr.sirs.ui.Growl;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.GenericName;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;


/**
 * Panneau de positionnement d'une dépendance, permettant d'éditer sa géométrie sur la carte ou
 * d'en importer une pour cette dépendance.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class FXPositionDependancePane extends BorderPane {
    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);

    @FXML
    private Label uiGeometryPresentLbl;

    @FXML
    private Button uiDrawOnMapBtn;

    @FXML
    private Button uiImportGeometryBtn;

    /**
     * La dépendance à éditer.
     */
    private final ObjectProperty<AbstractDependance> dependance = new SimpleObjectProperty<>();

    public FXPositionDependancePane() {
        SIRS.loadFXML(this);

        uiGeometryPresentLbl.setText("Pas de géométrie");
        uiDrawOnMapBtn.setText("Tracer sur la carte");
        uiImportGeometryBtn.setText("Importer une géométrie");

        dependance.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                uiGeometryPresentLbl.setText("Pas de géométrie");
                uiDrawOnMapBtn.setText("Tracer sur la carte");
                uiImportGeometryBtn.setText("Importer une géométrie");
            } else {
                uiGeometryPresentLbl.setText(newValue.getGeometry() == null ? "Pas de géométrie" : "Géométrie présente");
                uiDrawOnMapBtn.setText(newValue.getGeometry() == null ? "Tracer sur la carte" : "Modifier le tracé");
                uiImportGeometryBtn.setText(newValue.getGeometry() == null ? "Importer une géométrie" : "Réimporter une géométrie");

                newValue.geometryProperty().addListener((observable1, oldValue1, newValue1) -> {
                    uiGeometryPresentLbl.setText(newValue1 == null ? "Pas de géométrie" : "Géométrie présente");
                    uiDrawOnMapBtn.setText(newValue.getGeometry() == null ? "Tracer sur la carte" : "Modifier le tracé");
                    uiImportGeometryBtn.setText(newValue.getGeometry() == null ? "Importer une géométrie" : "Réimporter une géométrie");
                });
            }
        });
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

        if (dependance.get().getGeometry() != null) {
            final JTSEnvelope2D env = JTS.toEnvelope(dependance.get().getGeometry());
            final Envelope selectionEnvelope = SIRS.pseudoBuffer(env);
            Platform.runLater(() -> {
                try {
                    tab.getMap().getUiMap().getCanvas().setVisibleArea(selectionEnvelope);
                } catch (Exception e) {
                    GeotkFX.newExceptionDialog("Impossible de zoomer sur l'étendue de la géométrie", e);
                    SIRS.LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                }
            });
        }
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
