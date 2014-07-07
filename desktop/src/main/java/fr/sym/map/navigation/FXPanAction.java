/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author husky
 */
public final class FXPanAction extends AbstractAction {
    private final boolean infoOnClick;
    public static final Image ICON = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ARROWS, 16, FontAwesomeIcons.DEFAULT_COLOR), null);
    private final FXMap map;

    public FXPanAction(FXMap map, boolean infoOnClick) {
        super(MessageBundle.getString("map_pan"));
        this.infoOnClick = infoOnClick;
        this.map = map;
        graphicProperty().setValue(new ImageView(ICON));
    }

    @Override
    public void handle(ActionEvent event) {
        if (map != null) {
            map.setHandler(new FXPanHandler(map, infoOnClick));
        }
    }
    
}
