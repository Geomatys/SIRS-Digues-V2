
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.util.SirsStringConverter;
import java.io.File;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class FXAbstractImportCoordinate extends BorderPane {
    
    @FXML protected TextField uiPath;
    @FXML protected TextField uiSeparator;
    
    @FXML protected ComboBox<CoordinateReferenceSystem> uiCRS;
    @FXML protected FXFeatureTable uiTable;
    
    @FXML protected GridPane uiPaneConfig;
    @FXML protected GridPane uiPaneImport;

    protected FeatureStore store;
    
    final SirsStringConverter stringConverter = new SirsStringConverter();
    
    public FXAbstractImportCoordinate() {
        SIRS.loadFXML(this);
        
        uiCRS.setItems(FXCollections.observableArrayList(Injector.getSession().getProjection(), FXPositionablePane.CRS_WGS84));
        uiCRS.setConverter(stringConverter);
        uiCRS.getSelectionModel().clearAndSelect(0);
        
        uiPaneConfig.setDisable(true);
        uiTable.setEditable(false);
        uiTable.setLoadAll(true);
               
    }

    @FXML
    protected void openFileChooser(ActionEvent event) {
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
    
    private static File getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(FXAbstractImportCoordinate.class);
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
        final Preferences prefs = Preferences.userNodeForPackage(FXAbstractImportCoordinate.class);
        prefs.put("path", path.getAbsolutePath());
    }
    
}
