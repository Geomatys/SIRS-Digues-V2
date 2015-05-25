
package fr.sirs.map.style;

import fr.sirs.Injector;
import fr.sirs.core.model.Preview;
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
    protected MutableRule createRule(PropertyName property, Object obj, int idx) {
        String desc = String.valueOf(obj);
        final Preview lbl = Injector.getSession().getPreviews().get(desc);
        if(lbl!=null){
            desc = lbl.getLibelle();
        }

        final MutableStyleFactory sf = GeotkFX.getStyleFactory();
        final FilterFactory ff = GeotkFX.getFilterFactory();

        final MutableRule r = sf.rule(createSymbolizer(idx));
        r.setFilter(ff.equals(property, ff.literal(obj)));
        r.setDescription(sf.description(desc,desc));
        r.setName(desc);
        return r;
    }

}
