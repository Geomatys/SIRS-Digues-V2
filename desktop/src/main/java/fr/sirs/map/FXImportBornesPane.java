package fr.sirs.map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.CRS_WGS84;

import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.SystemeReperageRepository;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.GeometryDescriptor;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.LayerListener;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.util.collection.CollectionChangeEvent;
import org.opengis.feature.AttributeType;
import org.opengis.feature.PropertyType;
import org.opengis.filter.Id;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.GenericName;

/**
 * Un panneau permettant d'ouvrir un fichier Shapefile (.shp) pour en extraire 
 * des points puis les convertir en bornes.
 * 
 * TODO : Etendre la gestion aux format CSV (Ré-activation du choix du CRS, etc.)
 * 
 * TODO : Fermer Feature store / nettoyer les ressources quand on ferme la fenêtre.
 * 
 * @author Alexis Manin (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class FXImportBornesPane extends BorderPane {

    @FXML private TextField uiPath;
    @FXML private TextField uiSeparator;
    @FXML private Label uiSeparatorLabel;
    @FXML private ComboBox<CoordinateReferenceSystem> uiCRS;
    @FXML private ComboBox<PropertyType> uiAttX;
    @FXML private ComboBox<PropertyType> uiAttY;
    @FXML private FXFeatureTable uiTable;
    @FXML private ComboBox uiLibelleBox;
    @FXML private ComboBox<SystemeReperage> uiSRBox;
    @FXML private ComboBox<PropertyType> uiPRBox;
    @FXML private ComboBox<PropertyType> uiCodeBox;
    @FXML private ComboBox<Preview> uiTronconBox;
    @FXML private GridPane uiPaneConfig;
    @FXML private GridPane uiPaneImport;    
    @FXML private Button uiImportButton;

    private FeatureMapLayer loadedData;
    private FeatureCollection selection;
    
    private final String typeName;
    private final Class<? extends TronconDigue> typeClass;

    public FXImportBornesPane(final String typeName, final Class typeClass) {
        SIRS.loadFXML(this);
        
        this.typeName  = typeName;
        this.typeClass = typeClass;

        final SirsStringConverter stringConverter = new SirsStringConverter();
        uiCRS.setItems(FXCollections.observableArrayList(Injector.getSession().getProjection(), CRS_WGS84));
        uiCRS.setConverter(stringConverter);
        uiCRS.getSelectionModel().clearAndSelect(0);
        uiAttX.setConverter(stringConverter);
        uiAttY.setConverter(stringConverter);
        uiLibelleBox.setConverter(stringConverter);
        uiSRBox.setConverter(stringConverter);
        uiPRBox.setConverter(stringConverter);
        uiCodeBox.setConverter(stringConverter);

        // TODO : make visible if we activate back csv import.
        uiSeparator.setVisible(false);
        uiSeparator.managedProperty().bind(uiSeparator.visibleProperty());
        uiSeparatorLabel.setVisible(false);
        uiSeparatorLabel.managedProperty().bind(uiSeparatorLabel.visibleProperty());

        uiPaneConfig.setVisible(false);
        uiPaneConfig.managedProperty().bind(uiPaneConfig.visibleProperty());

        uiTable.setEditable(false);
        uiTable.setLoadAll(true);
        
        uiImportButton.setTooltip(new Tooltip("Importer la séléction"));
        uiImportButton.setDisable(true);
        
        uiTronconBox.setItems(FXCollections.observableList(
                Injector.getSession().getPreviews().getByClass(typeClass)));
        uiTronconBox.setConverter(stringConverter);
        uiTronconBox.valueProperty().addListener(this::updateSrList);

    }

    private void updateSrList(ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue){
        if(newValue==null){
            uiSRBox.setItems(FXCollections.emptyObservableList());
        } else {
            final fr.sirs.Session session = Injector.getSession();
            final TronconDigue troncon = session.getRepositoryForClass(typeClass).get(newValue.getElementId());
            final List<SystemeReperage> srs = ((SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class)).getByLinear(troncon);
            uiSRBox.setItems(FXCollections.observableArrayList(srs));
            uiSRBox.getSelectionModel().selectFirst();

            final String defaultSRID = troncon.getSystemeRepDefautId();
            if (defaultSRID != null) {
                for (final SystemeReperage sr : srs) {
                    if (defaultSRID.equals(sr.getId())) {
                        uiSRBox.getSelectionModel().select(sr);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    void openFileChooser(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File prevPath = getPreviousPath();
        if (prevPath != null) {
            fileChooser.setInitialDirectory(prevPath);
        }
        final File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            setPreviousPath(file.getParentFile());
            uiPath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void openFeatureStore(ActionEvent event) {
        final File file = new File(uiPath.getText());

        uiPaneConfig.setDisable(true);
        uiImportButton.setDisable(true);

        final Task<FeatureCollection> openTask = new Task<FeatureCollection>() {
            @Override
            protected FeatureCollection call() throws Exception {
                updateTitle("Lecture d'un fichier vectoriel.");
//            TODO : Uncomment if we want to activate back csv / txt import
//            if(url.toLowerCase().endsWith(".shp")){
                final FeatureStore store = new ShapefileFeatureStore(file.toURI().toURL(), "no namespace");
//            }else if(url.toLowerCase().endsWith(".txt") || url.toLowerCase().endsWith(".csv")){
//                final char separator = (uiSeparator.getText().isEmpty()) ? ';' : uiSeparator.getText().charAt(0);
//                store = new CSVFeatureStore(file, "no namespace", separator);
//                uiPaneConfig.setDisable(false);
//            }else{
//                new Alert(Alert.AlertType.ERROR, "Le fichier sélectionné n'est pas un shp, csv ou txt", ButtonType.OK).showAndWait();
//                return;
//            }
                final Session session = store.createSession(true);
                final Set<GenericName> names = store.getNames();
                if (names == null || names.isEmpty()) {
                    throw new IllegalArgumentException("Aucune donnée vectorielle trouvée dans le fichier.");
                }
                
                // On s'assure que le fichier en entrée contient des points.
                final GenericName n = names.iterator().next();
                GeometryDescriptor geometryDescriptor = store.getFeatureType(n).getGeometryDescriptor();
                if (geometryDescriptor == null || !Geometry.class.isAssignableFrom(geometryDescriptor.getType().getBinding())) {
                    throw new IllegalArgumentException("Aucune donnée vectorielle trouvée dans le fichier.");
                }
                
                return session.getFeatureCollection(QueryBuilder.all(names.iterator().next()));
            }
        };

        final FeatureCollection col;
        try {
            col = TaskManager.INSTANCE.submit(openTask).get();
        } catch (Exception e) {
            GeotkFX.newExceptionDialog("Impossible d'ouvrir le fichier séléctionné.", e).show();
            return;
        }
        FeatureType fType = col.getFeatureType();
        loadedData = MapBuilder.createFeatureLayer(col, RandomStyleBuilder.createDefaultVectorStyle(fType));
        uiTable.init(loadedData);

        //liste des propriétés
        final ObservableList<PropertyType> properties = FXCollections.observableArrayList(fType.getProperties(true));
        uiAttX.setItems(properties);
        uiAttY.setItems(properties);
        
        final ObservableList stringProperties = properties.filtered((PropertyType p) -> {
            return (p instanceof AttributeType) && 
                    CharSequence.class.isAssignableFrom(
                            ((AttributeType)p).getValueClass());
        });
        final ObservableList numberProperties = properties.filtered((PropertyType p) -> {
            return (p instanceof AttributeType) &&
                    Number.class.isAssignableFrom(
                            ((AttributeType)p).getValueClass());
        });
        uiLibelleBox.setItems(stringProperties);
        uiCodeBox.setItems(stringProperties);
        uiPRBox.setItems(numberProperties);
        
        if (!properties.isEmpty()) {
            uiAttX.getSelectionModel().clearAndSelect(0);
            uiAttY.getSelectionModel().clearAndSelect(0);
        }

        //on ecoute la selection
        loadedData.addLayerListener(new LayerListener() {
            @Override
            public void styleChange(MapLayer source, EventObject event) {
            }

            @Override
            public void itemChange(CollectionChangeEvent<MapItem> event) {
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!FeatureMapLayer.SELECTION_FILTER_PROPERTY.equals(evt.getPropertyName())) {
                    return;
                }

                final Id filter = loadedData.getSelectionFilter();
                try {
                    selection = loadedData.getCollection().subCollection(QueryBuilder.filtered(fType.getName(), filter));
                    if (selection == null || selection.isEmpty()) {
                        uiImportButton.setDisable(true);
                    } else {
                        uiImportButton.setDisable(false);
                    }
                } catch (DataStoreException ex) {
                    GeotkFX.newExceptionDialog("Une erreur est survenue lors de la mise à jour de la sélection.", ex).show();
                }
            }
        });
    }

    @FXML
    void cancelImport(ActionEvent event) {
        this.getScene().getWindow().hide();
    }

    @FXML
    void importBornes(ActionEvent event) {
        if (selection == null || selection.isEmpty()) {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune borne à importer.", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        final Object selectedTd = uiTronconBox.getSelectionModel().getSelectedItem();
        if (selectedTd == null) {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Veuillez sélectionner un " + typeName + ".", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        Object selectedCode = uiCodeBox.getSelectionModel().getSelectedItem();
        final String codeProperty;
        if (selectedCode instanceof PropertyType) {
            codeProperty = ((PropertyType) selectedCode).getName().head().toString();
        } else {
            codeProperty = null;
        }
        
        Object selectedLibelle = uiLibelleBox.getSelectionModel().getSelectedItem();
        final String libelleProperty;
        if (selectedLibelle instanceof PropertyType) {
            libelleProperty = ((PropertyType) selectedLibelle).getName().head().toString();
        } else {
            libelleProperty = null;
        }

        Object selectedPR = uiPRBox.getSelectionModel().getSelectedItem();
        final String prProperty;
        if (selectedPR instanceof PropertyType) {
            prProperty = ((PropertyType) selectedPR).getName().head().toString();
        } else {
            prProperty = null;
        }
        
        final SystemeReperage sr = uiSRBox.getValue();

        final Task importTask = new Task() {
            @Override
            protected Object call() throws Exception {
                final fr.sirs.Session session = Injector.getSession();

                final TronconDigue troncon;
                final AbstractSIRSRepository tdRepo = Injector.getSession().getRepositoryForClass(typeClass);
                if (selectedTd instanceof TronconDigue) {
                    troncon = (TronconDigue) selectedTd;
                } else if (selectedTd instanceof Preview) {
                    troncon = (TronconDigue) tdRepo.get(((Preview) selectedTd).getDocId());
                } else {
                    throw new IllegalStateException("Unknown object type for parameter " + typeName);
                }

                final MathTransform trs = CRS.findMathTransform(
                        selection.getFeatureType().getCoordinateReferenceSystem(), // TODO : replace CRS with the one in uiCRS for CSV files.
Injector.getSession().getProjection(),
                        true);
                final boolean isIdentity = trs.isIdentity();

                boolean mustUpdateTroncon = false;
                try (final FeatureIterator it = selection.iterator()) {
                    final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
                    while (it.hasNext()) {
                        Feature current = it.next();
                        // We can cast here because we checked property type at loading. 
                        Geometry value = (Geometry) current.getDefaultGeometryProperty().getValue();
//                // TODO : use following code for CSV files.
//            final String attX = String.valueOf(feature.getPropertyValue(uiAttX.getValue().getName().tip().toString()));
//            final String attY = String.valueOf(feature.getPropertyValue(uiAttY.getValue().getName().tip().toString()));
//            geom = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(Double.valueOf(attX), Double.valueOf(attY)));
                        if (value instanceof Point) {
                            final Point borneGeom;
                            if (isIdentity) {
                                borneGeom = (Point) value;
                            } else {
                                borneGeom = (Point) JTS.transform((Point) value, trs);
                                JTS.setCRS(borneGeom, Injector.getSession().getProjection());
                            }
                            BorneDigue newBorn = borneRepo.create();
                            newBorn.setGeometry(borneGeom);
                            if (codeProperty != null) {
                                newBorn.setDesignation(current.getPropertyValue(codeProperty).toString());
                            }
                            if (libelleProperty != null) {
                                newBorn.setLibelle(current.getPropertyValue(libelleProperty).toString());
                            }

                            borneRepo.add(newBorn);
                            mustUpdateTroncon = (troncon.getBorneIds().add(newBorn.getId()) || mustUpdateTroncon);

                            if(sr!=null && prProperty!=null){
                                //reference dans le SR
                                final SystemeReperageBorne srb = session.getElementCreator().createElement(SystemeReperageBorne.class);
                                srb.borneIdProperty().set(newBorn.getDocumentId());
                                srb.setValeurPR(((Number)current.getPropertyValue(prProperty)).floatValue());
                                sr.systemeReperageBornes.add(srb);
                            }

                        } else {
                    // TODO : store unmanaged feature libelle to alert user at
                            // the end of import.
                        }
                    }
                } finally {
                    if (mustUpdateTroncon) {
                        tdRepo.update(troncon);
                    }
                }

                //sauvegarde du SR
                if(sr!=null){
                    ((SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class)).update(sr, troncon);
                }

                return mustUpdateTroncon;
            }
        };
        try {
            final Alert alert;
            if (Boolean.TRUE.equals(TaskManager.INSTANCE.submit(importTask).get())) {
                alert = new Alert(Alert.AlertType.INFORMATION, "L'import est terminé.", ButtonType.OK);
            } else {
                alert = new Alert(Alert.AlertType.WARNING, "Aucune borne n'a pu être importée.", ButtonType.OK);
            }
            alert.setResizable(true);
            alert.showAndWait();
        } catch (Exception ex) {
            GeotkFX.newExceptionDialog("Une erreur s'est produite pendant l'import des bornes.", ex).show();
        }
    }

    private static File getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(FXImportBornesPane.class);
        final String str = prefs.get("path", null);
        if (str != null) {
            final File file = new File(str);
            if (file.isDirectory()) {
                return file;
            }
        }
        return null;
    }

    private static void setPreviousPath(final File path) {
        final Preferences prefs = Preferences.userNodeForPackage(FXImportBornesPane.class);
        prefs.put("path", path.getAbsolutePath());
    }

    public static void showImportDialog(String typeName, Class typeClass) {
        final FXImportBornesPane panel = new FXImportBornesPane(typeName, typeClass);
        final Stage dialog = new Stage();
        dialog.setTitle("Import de bornes");
        dialog.getIcons().add(SIRS.ICON);
        dialog.setResizable(true);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(panel));
        dialog.initOwner(Injector.getSession().getFrame().getScene().getWindow());
        dialog.sizeToScene();
        dialog.showAndWait();
    }
}
