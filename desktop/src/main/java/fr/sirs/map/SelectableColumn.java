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
package fr.sirs.map;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.MouseEvent;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.gui.javafx.contexttree.MapItemSelectableColumn;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SelectableColumn extends TreeTableColumn<MapItem, Boolean> {
    private static final Tooltip LOCK_TOOLTIP = new Tooltip(
            GeotkFX.getString(MapItemSelectableColumn.class, "lockTooltip"));
    private static final Tooltip UNLOCK_TOOLTIP = new Tooltip(
            GeotkFX.getString(MapItemSelectableColumn.class, "unlockTooltip"));
    
    public SelectableColumn() {
        setEditable(true);
        setPrefWidth(26);
        setMinWidth(26);
        setMaxWidth(26);

        setCellValueFactory((TreeTableColumn.CellDataFeatures<MapItem, Boolean> param) -> {
            final SimpleBooleanProperty value = new SimpleBooleanProperty();
            if (param.getValue() != null && param.getValue().getValue() != null) {
                value.set(isSelectable(param.getValue().getValue()));
            }
            return value;
        });
        
        setCellFactory((TreeTableColumn<MapItem, Boolean> param) -> new SelectableCell());
    }

    private static final class SelectableCell extends TreeTableCell<MapItem, Boolean> {
                
        public SelectableCell() {
            setFont(FXUtilities.FONTAWESOME);
            setOnMouseClicked(this::mouseClicked);
            textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                if (FontAwesomeIcons.ICON_LOCK.equals(newValue)) {
                    setTooltip(UNLOCK_TOOLTIP);
                } else {
                    setTooltip(LOCK_TOOLTIP);
                }
            });
        }

        private void mouseClicked(MouseEvent event){
            if(isEditing() && itemProperty().get() != null) {
                final MapItem mitem = getTreeTableRow().getItem();
                if (mitem != null) {
                    final boolean newValue = !itemProperty().get();
                    setSelectable(mitem, newValue);
                    itemProperty().set(newValue);
                }
            }
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
            } else {
                setText(item? FontAwesomeIcons.ICON_UNLOCK : FontAwesomeIcons.ICON_LOCK);
            }
        }
    }
    
        private static void setSelectable(final MapItem target, final Boolean selectable) {
            if (target instanceof MapLayer) {
                ((MapLayer)target).setSelectable(selectable);
            } else {
                for (final MapItem child : target.items()) {
                    setSelectable(child, selectable);
                }
            }
        }
                
        private static boolean isSelectable(final MapItem item) {
            if (item instanceof MapLayer) {
                return ((MapLayer)item).isSelectable();
            } else {
                for (MapItem child : item.items()) {
                    if (isSelectable(child)) {
                        return true;
                    }
                }
            }
            return false;
        }
}
