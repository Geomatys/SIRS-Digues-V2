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

import fr.sirs.Injector;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Preview;
import java.util.logging.Level;
import org.ektorp.DocumentNotFoundException;
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
        try {
            final Preview lbl = Injector.getSession().getPreviews().get(desc);
            if (lbl != null) {
                desc = lbl.getLibelle();
            }
        } catch (DocumentNotFoundException e) {
            SirsCore.LOGGER.log(Level.FINE, "No document found for id : " + desc, e);
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
