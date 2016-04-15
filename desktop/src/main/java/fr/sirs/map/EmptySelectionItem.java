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

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.geotoolkit.gui.javafx.contexttree.TreeMenuItem;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.CollectionMapLayer;

/**
 * A context action for {@link CollectionMapLayer}, whose job is to clean selection
 * filter on selected layer(s).
 * 
 * Note : Tree items are browsed recursively, so if a parent node is selected, all
 * its children will be impacted.
 * 
 * N.B : Copied from Geotk snapshot. Remove this class when we'll upgrade geotk version.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class EmptySelectionItem  extends TreeMenuItem {
    
    @Override
    public MenuItem init(List<? extends TreeItem> selectedItems) {
        final List<CollectionMapLayer> layers = new ArrayList<>();
        findFeatureLayers(selectedItems, layers);
        
        if (layers.isEmpty()) return null;
        
        final MenuItem item = new MenuItem("Enlever la surbrillance", new ImageView(GeotkFX.ICON_UNLINK));
        item.setOnAction((ActionEvent e)-> {
            for (final CollectionMapLayer layer : layers) {
                layer.setSelectionFilter(null);
            }
        });
        return item;
    }
    
    /**
     * Scan all tree items in the input list to find ones whose value is a {@link CollectionMapLayer}
     * @param selected
     * @param toKeep 
     */
    private static void findFeatureLayers(final List<? extends TreeItem> selected, final List<CollectionMapLayer> toKeep) {
        for (final TreeItem item : selected) {
            if (item.getValue() instanceof CollectionMapLayer) {
                final CollectionMapLayer layer = (CollectionMapLayer) item.getValue();
                if (layer.getSelectionFilter() != null) {
                    toKeep.add((CollectionMapLayer) item.getValue());
                }
            } else if (!item.isLeaf()) {
                findFeatureLayers(item.getChildren(), toKeep);
            }
        }
    }

}
