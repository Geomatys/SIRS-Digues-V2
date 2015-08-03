package fr.sirs.plugins.mobile;

import fr.sirs.SIRS;
import fr.sirs.util.property.SirsPreferences;
import java.nio.file.Path;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoImportPane extends StackPane {

    @FXML
    private Label uiSourceName;

    @FXML
    private Label uiSourceType;

    @FXML
    private Label uiSourceUsableSpace;

    @FXML
    private ProgressIndicator uiSoureSpaceProgress;

    @FXML
    private Label uiRootLabel;

    @FXML
    private Hyperlink uiChooseSubDir;

    @FXML
    private Label uiSubDirLabel;

    @FXML
    private ProgressIndicator uiDestSpaceProgress;

    @FXML
    private ProgressBar uiImportProgress;

    /**
     * Source directory in which we'll find photos to transfer.
     */
    private final SimpleObjectProperty<Path> sourceDirProperty = new SimpleObjectProperty<>();

    /**
     * Destination root path, as it should be defined in {@link SirsPreferences.PROPERTIES#DOCUMENT_ROOT}
     */
    private final SimpleObjectProperty<Path> rootDirProperty = new SimpleObjectProperty<>();

    /**
     * A sub-directory of {@link #rootDirProperty} to put imported photos into.
     */
    private final SimpleObjectProperty<Path> subDirProperty = new SimpleObjectProperty<>();

    public PhotoImportPane() {
        super();
        SIRS.loadFXML(this);

        final BooleanBinding noRootConfigured = rootDirProperty.isNull();
        uiChooseSubDir.disableProperty().bind(noRootConfigured);
        uiSubDirLabel.disableProperty().bind(noRootConfigured);

        uiRootLabel.textProperty().bind(rootDirProperty.asString());
        uiSubDirLabel.textProperty().bind(subDirProperty.asString());


    }

    /*
     * UI ACTIONS
     */

    @FXML
    void chooseSource(ActionEvent event) {

    }

    @FXML
    void chooseSubDirectory(ActionEvent event) {

    }

    @FXML
    void configureRoot(ActionEvent event) {

    }

    @FXML
    void importPhotos(ActionEvent event) {

    }
}
