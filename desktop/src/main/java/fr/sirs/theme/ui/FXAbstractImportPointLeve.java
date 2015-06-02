
package fr.sirs.theme.ui;

import fr.sirs.core.model.PointZ;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.geotoolkit.feature.Feature;
import org.opengis.feature.PropertyType;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class FXAbstractImportPointLeve<T extends PointZ> extends FXAbstractImportCoordinate {
    
    protected final ObservableList<Feature> selectionProperty = FXCollections.observableArrayList();
    protected final PojoTable pojoTable;
    @FXML protected ComboBox<PropertyType> uiAttDesignation;
    @FXML protected ComboBox<PropertyType> uiAttZ;
    
    FXAbstractImportPointLeve(final PojoTable pojoTable) {
        super();
        this.pojoTable = pojoTable;
        uiAttDesignation.setConverter(stringConverter);
        uiAttZ.setConverter(stringConverter);
    }
    
    @FXML
    void importSelection(ActionEvent event) {
        final ObservableList<T> pt = getSelectionPoint();
        if(pt==null || pt.isEmpty()) return;
        pojoTable.getAllValues().addAll(pt);
    }
    
    protected abstract ObservableList<T> getSelectionPoint();
}
