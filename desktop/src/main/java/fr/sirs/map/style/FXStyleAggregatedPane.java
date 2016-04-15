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
package fr.sirs.map.style;

import java.util.List;
import javafx.scene.control.SeparatorMenuItem;
import org.geotoolkit.gui.javafx.layer.style.FXStyleClassifRangePane;
import org.geotoolkit.gui.javafx.style.FXStyleElementController;
import org.geotoolkit.gui.javafx.style.FXStyleElementEditor;
import org.geotoolkit.gui.javafx.style.FXStyleTree;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.internal.GeotkFX;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXStyleAggregatedPane extends org.geotoolkit.gui.javafx.layer.style.FXStyleAggregatedPane{

    public void initialize() {
        menuItems.add(new FXStyleTree.ShowStylePaneAction(new FXStyleClassifRangePane(),GeotkFX.getString(FXStyleClassifRangePane.class,"title")));
        menuItems.add(new FXStyleTree.ShowStylePaneAction(new FXStyleClassifSinglePane(),GeotkFX.getString(org.geotoolkit.gui.javafx.layer.style.FXStyleClassifSinglePane.class,"title")));
        menuItems.add(new SeparatorMenuItem());
        menuItems.add(new FXStyleTree.NewFTSAction());
        menuItems.add(new FXStyleTree.NewRuleAction());
        final List<FXStyleElementController> editors = FXStyleElementEditor.findEditorsForType(Symbolizer.class);
        for(FXStyleElementController editor : editors){
            menuItems.add(new FXStyleTree.NewSymbolizerAction(editor));
        }
        menuItems.add(new SeparatorMenuItem());
        menuItems.add(new FXStyleTree.DuplicateAction());
        menuItems.add(new FXStyleTree.DeleteAction());

        FXUtilities.hideTableHeader(tree);
    }

}
