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
package fr.sirs.plugin.lit.map;

import fr.sirs.SIRS;
import fr.sirs.core.model.Lit;
import fr.sirs.core.model.TronconLit;
import fr.sirs.map.TronconEditHandler;
import fr.sirs.plugin.lit.PluginLit;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class LitEditHandler extends TronconEditHandler {

    public LitEditHandler(FXMap map) {
        super(map);
    }
    
    @Override
    protected void init() {
        this.layerName = PluginLit.LAYER_NAME;
        this.tronconClass = TronconLit.class;
        try {
            this.style = PluginLit.createLitStyle();
        } catch (URISyntaxException | CQLException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        this.typeName = "tronçon de lit";
        this.maleGender = true;
        this.parentClass = Lit.class;
        this.showRive = false;
        this.parentLabel = "au lit";
    }
    
}
