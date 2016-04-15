/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.util;

import java.util.function.Function;
import java.util.function.Predicate;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;

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
