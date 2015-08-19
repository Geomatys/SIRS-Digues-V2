package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static fr.sirs.SIRS.CRS_WGS84;
import static fr.sirs.SIRS.ICON_VIEWOTHER_WHITE;

/**
 *
 */
public class FXPositionDependancePane extends BorderPane {

    private final CoordinateReferenceSystem baseCrs = Injector.getSession().getProjection();

    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);

    @FXML private ComboBox<CoordinateReferenceSystem> uiCRSs;
    @FXML private Button uiView;

    public FXPositionDependancePane() {
        SIRS.loadFXML(this);

        uiView.setGraphic(new ImageView(ICON_VIEWOTHER_WHITE));

        //liste par défaut des systemes de coordonnées
        final ObservableList<CoordinateReferenceSystem> crss = FXCollections.observableArrayList();
        crss.add(CRS_WGS84);
        crss.add(baseCrs);
        uiCRSs.setItems(crss);
        uiCRSs.getSelectionModel().clearAndSelect(1);
        uiCRSs.disableProperty().bind(disableFieldsProperty);
        uiCRSs.setConverter(new SirsStringConverter());
    }

    @FXML
    public void viewAllSR() {

    }

    public BooleanProperty disableFieldsProperty(){
        return disableFieldsProperty;
    }
}
