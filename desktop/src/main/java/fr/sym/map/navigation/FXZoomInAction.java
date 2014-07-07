/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package fr.sym.map.navigation;

import fr.sym.map.FXMap;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.action.AbstractAction;
import org.geotoolkit.gui.swing.resource.FontAwesomeIcons;
import org.geotoolkit.gui.swing.resource.IconBuilder;
import org.geotoolkit.gui.swing.resource.MessageBundle;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class FXZoomInAction extends AbstractAction {
    public static final Image ICON = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_SEARCH_PLUS, 16, FontAwesomeIcons.DEFAULT_COLOR), null);
    private final FXMap map;

    public FXZoomInAction(FXMap map) {
        super(MessageBundle.getString("map_zoom_in"));
        this.map = map;
        graphicProperty().setValue(new ImageView(ICON));
    }

    @Override
    public void handle(ActionEvent event) {
        if (map != null) {
            map.setHandler(new FXZoomInHandler(map));
        }
    }
    
}
