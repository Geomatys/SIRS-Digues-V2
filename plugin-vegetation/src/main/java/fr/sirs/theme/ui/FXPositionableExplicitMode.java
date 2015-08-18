package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PositionableVegetation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.geometry.jts.JTS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Edition en WKT des geometries
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableExplicitMode extends BorderPane implements FXPositionableMode {

    public static final String MODE = "EXPLICIT";

    private final CoordinateReferenceSystem baseCrs = Injector.getSession().getProjection();
    
    private final ObjectProperty<Positionable> posProperty = new SimpleObjectProperty<>();
    private final BooleanProperty disableProperty = new SimpleBooleanProperty(true);

    @FXML 
    private TextArea uiText;

    private boolean reseting = false;

    public FXPositionableExplicitMode() {
        SIRS.loadFXML(this, Positionable.class);

        uiText.disableProperty().bind(disableProperty);

        final ChangeListener<Geometry> geomListener = new ChangeListener<Geometry>() {
            @Override
            public void changed(ObservableValue<? extends Geometry> observable, Geometry oldValue, Geometry newValue) {
                if(reseting) return;
                updateFields();
            }
        };

        posProperty.addListener(new ChangeListener<Positionable>() {
            @Override
            public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) {
                if(oldValue!=null){
                    oldValue.geometryProperty().removeListener(geomListener);
                }
                if(newValue!=null){
                    newValue.geometryProperty().addListener(geomListener);
                    updateFields();
                }
            }
        });

        uiText.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> coordChange() );
        
    }

    @Override
    public String getID() {
        return MODE;
    }
    
    @Override
    public String getTitle() {
        return "Géométrie";
    }

    @Override
    public Node getFXNode() {
        return this;
    }

    @Override
    public ObjectProperty<Positionable> positionableProperty() {
        return posProperty;
    }

    @Override
    public BooleanProperty disablingProperty() {
        return disableProperty;
    }

    private void updateFields(){
        reseting = true;

        final Positionable pos = posProperty.get();
        final String wkt = pos.getGeometry().toText();

        uiText.setText(wkt);

        reseting = false;
    }

    private void buildGeometry(){

        final String wkt = uiText.getText();
        final WKTReader reader = new WKTReader();
        
        try {
            final Geometry geom = reader.read(wkt);
            JTS.setCRS(geom, baseCrs);

            final PositionableVegetation positionable = (PositionableVegetation) posProperty.get();
            positionable.geometryModeProperty().set(MODE);
            positionable.setGeometry(geom);
            positionable.setExplicitGeometry(geom);

            uiText.getStyleClass().remove("unvalid");
            uiText.getStyleClass().add("valid");

        } catch (ParseException ex) {
            uiText.getStyleClass().remove("valid");
            uiText.getStyleClass().add("unvalid");
        }

    }

    private void coordChange(){
        if(reseting) return;
        
        reseting = true;
        buildGeometry();
        reseting = false;
    }


}
