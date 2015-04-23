
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
