
package fr.sirs.map.style;

import fr.sirs.Injector;
import fr.sirs.core.model.PreviewLabel;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXStyleClassifSinglePane extends org.geotoolkit.gui.javafx.layer.style.FXStyleClassifSinglePane{

    public FXStyleClassifSinglePane(){
        super();
    }

    @Override
    protected MutableRule createRule(PropertyName property, Object obj) {
        final PreviewLabel lbl = Injector.getSession().getPreviewLabelRepository().get(String.valueOf(obj));
        String desc = obj.toString();
        if(lbl!=null){
            desc = lbl.getLabel();
        }

        final MutableStyleFactory sf = GeotkFX.getStyleFactory();
        final FilterFactory ff = GeotkFX.getFilterFactory();

        final MutableRule r = sf.rule(createSymbolizer());
        r.setFilter(ff.equals(property, ff.literal(obj)));
        r.setDescription(sf.description(desc,desc));
        r.setName(desc);
        return r;
    }

}
