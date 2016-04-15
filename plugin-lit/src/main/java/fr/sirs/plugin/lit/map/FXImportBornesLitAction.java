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

import fr.sirs.Injector;
import fr.sirs.core.model.TronconLit;
import fr.sirs.map.FXImportBornesPane;
import java.awt.Dimension;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author guilhem
 */
public class FXImportBornesLitAction extends FXMapAction {
    // For unknown reason, making icon 16*16 as other buttons on toolbar makes it one pixel less tall on screen.
    public static final Image ICON = SwingFXUtils.toFXImage(GeotkFX.getBufferedImage("add-vector", new Dimension(16, 17)), null);

        
    public FXImportBornesLitAction(FXMap map) {
        super(map, "Importer des bornes",
                "Import de bornes depuis un fichier Shapefile.", ICON);
        disabledProperty().bind(Injector.getSession().geometryEditionProperty().not());
    }
    
    @Override
    public void accept(ActionEvent t) {
        FXImportBornesPane.showImportDialog("tronçon de lit", TronconLit.class);
    }

}
