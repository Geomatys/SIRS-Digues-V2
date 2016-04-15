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
package fr.sirs;

import fr.sirs.core.SirsCore;
import java.net.URL;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXAboutPane extends BorderPane {
    
    private static final String FRANCE_DIGUES_URL = "http://sirs-digues.info";
    
    @FXML
    private Hyperlink uiCommunityLink;

    @FXML
    private Label uiVersionLabel;
    
    
    @FXML
    private Hyperlink uiMajLink;
        
    @FXML
    private ProgressIndicator uiMajProgress;
    
    public FXAboutPane() {
        SIRS.loadFXML(this);
        uiVersionLabel.setText(SIRS.getVersion());
        uiCommunityLink.setUnderline(true);
        uiCommunityLink.setOnAction((ActionEvent event) -> {
            try {
                SIRS.browseURL(new URL(FRANCE_DIGUES_URL), "Site communautaire");
            } catch (Exception ex) {
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            }
        });
        
        // Update check process
        uiMajLink.setVisible(false);
        final Task<SirsCore.UpdateInfo> checkUpdate = SirsCore.checkUpdate();
        uiMajProgress.visibleProperty().bind(checkUpdate.runningProperty());
        checkUpdate.setOnSucceeded(event -> {
            Object value = event.getSource().getValue();
            if (value instanceof SirsCore.UpdateInfo) {
                final SirsCore.UpdateInfo info = (SirsCore.UpdateInfo) value;
                Platform.runLater(() -> {
                    uiMajLink.setTooltip(new Tooltip("vers " + info.distantVersion));
                    uiMajLink.setVisible(true);
                    uiMajLink.setOnAction(ae -> SIRS.browseURL(info.updateURL, "Mise à jour"));
                });
            }
        });
    }

}
