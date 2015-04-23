package fr.sirs.util;

import java.util.function.Function;
import java.util.function.Predicate;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * A simple class which represents a table column whose content is a button.
 * @author Alexis Manin (Geomatys)
 * 
 * @see TableColumn
 */
public class SimpleButtonColumn<S, T> extends TableColumn<S, T> {
    
    private Tooltip cellTooltip;
    
    /**
     * Create a new button column, ready for use.
     * @param buttonIcon Thee icon to set for displayed button. Cannot be null.
     * @param cellValueFactory The factory used to retrieve value of a given cell. 
     * If null, you'll have to set it manually after built, using {@link #setCellValueFactory(javafx.util.Callback) }.
     * @param buttonVisiblePredicate A predicate whose input is cell value. It tells 
     * if the button in the corresponding cell should be visible or not. Cannot be null. 
     * @param buttonAction The action to perform when a button of a cell is pressed, 
     * with input the value given by cellValueFactory. Cannot be null.
     * @param tooltip An optional tooltip for buttons in cells. Can be set later 
     * using {@link #setTooltip(java.lang.String) }.
     */
    public SimpleButtonColumn(
            final Image buttonIcon, 
            final Callback<CellDataFeatures<S, T>, ObservableValue<T>> cellValueFactory, 
            final Predicate<T> buttonVisiblePredicate, 
            final Function<T, T> buttonAction, 
            final String tooltip) {
        super();
        setSortable(false);
        setResizable(false);
        setPrefWidth(24);
        setMinWidth(24);
        setMaxWidth(24);
        setGraphic(new ImageView(buttonIcon));
        
        if (tooltip != null) {
            cellTooltip = new Tooltip(tooltip);
        }

        if (cellValueFactory != null) {
            setCellValueFactory(cellValueFactory);
        }

        setCellFactory((TableColumn<S, T> param) -> {
            ButtonTableCell<S, T> cellButton = new ButtonTableCell<>(
                    false, new ImageView(buttonIcon), buttonVisiblePredicate, buttonAction);
            if (cellTooltip != null) {
                cellButton.setTooltip(cellTooltip);
            }
            return cellButton;
        });
    }
    
    public void setTooltip(final String tooltipText) {
        if (tooltipText != null && !tooltipText.isEmpty()) {
            cellTooltip = new Tooltip(tooltipText);
        }
    }
}
