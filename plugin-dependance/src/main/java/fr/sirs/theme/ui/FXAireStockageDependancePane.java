
package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.core.model.AireStockageDependance;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.geotoolkit.display.MeasureUtilities;

import javax.measure.unit.SI;
import java.text.NumberFormat;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXAireStockageDependancePane extends FXAireStockageDependancePaneStub {

    @FXML FXPositionDependancePane uiPosition;

    @FXML GridPane uiGridAttributes;

    private final Label lblGeomSize = new Label();
    private final Label geomSize = new Label();

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXAireStockageDependancePane() {
        super();
		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());

        uiPosition.dependanceProperty().bind(elementProperty);

        uiGridAttributes.add(lblGeomSize, 2, 0);
        uiGridAttributes.add(geomSize, 3, 0);
    }

    private void setGeometrySize(final Geometry geometry) {
        if (geometry != null) {
            if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
                lblGeomSize.setText("Surface");
                geomSize.setText(NumberFormat.getNumberInstance().format(
                        MeasureUtilities.calculateArea(geometry, Injector.getSession().getProjection(), SI.SQUARE_METRE)) +" m2");
            } else {
                lblGeomSize.setText("Longueur");
                geomSize.setText(NumberFormat.getNumberInstance().format(
                        MeasureUtilities.calculateLenght(geometry,
                                Injector.getSession().getProjection(), SI.METRE)) +" m");
            }
        }
    }
    
    public FXAireStockageDependancePane(final AireStockageDependance aireStockageDependance){
        this();
        this.elementProperty().set(aireStockageDependance);
    }

    @Override
    protected void initFields(ObservableValue<? extends AireStockageDependance> observableElement, AireStockageDependance oldElement, AireStockageDependance newElement) {
        super.initFields(observableElement, oldElement, newElement);

        final Geometry geometry = elementProperty.get().getGeometry();
        setGeometrySize(geometry);
        this.elementProperty.get().geometryProperty().addListener(new ChangeListener<Geometry>() {
            @Override
            public void changed(ObservableValue<? extends Geometry> observable, Geometry oldValue, Geometry newValue) {
                setGeometrySize(newValue);
            }
        });
    }
}
