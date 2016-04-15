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
package fr.sirs.plugin.berge.map;

import fr.sirs.SIRS;
import fr.sirs.core.model.Berge;
import fr.sirs.map.TronconEditHandler;
import fr.sirs.plugin.berge.PluginBerge;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class BergeEditHandler extends TronconEditHandler {

    public BergeEditHandler(FXMap map) {
        super(map);
    }

    protected void init() {
        this.layerName = PluginBerge.LAYER_BERGE_NAME;
        this.tronconClass = Berge.class;
        try {
            this.style = PluginBerge.createBergeStyle();
        } catch (URISyntaxException | CQLException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        this.typeName = "berge";
        this.maleGender = false;
        this.showRive = true;
    }
}

