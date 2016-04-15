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
package fr.sirs.plugin.berge.ui;

import com.sun.javafx.binding.Logging;
import fr.sirs.SIRS;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Berge;
import fr.sirs.core.model.PositionDocument;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import fr.sirs.plugin.berge.util.TabContent;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.PositionDocumentTheme;
import java.util.List;

/**
 *
 * @author guilhem
 */
public class AbstractDescriptionPane extends BorderPane {

    @FXML
    private ComboBox<Berge> bergeBox;
    
    @FXML
    private BorderPane uiCenter;
    
    public AbstractDescriptionPane() {
       this(null);
    }
     
    public AbstractDescriptionPane(final List<TabContent> contents) {
        SIRS.loadFXML(this);
        
                
        if (contents != null) {
            if (contents.size() > 1) {
                AbstractTheme.ThemeManager[] themes = new AbstractTheme.ThemeManager[contents.size()];
                int i = 0;
                for (TabContent tc : contents) {
                    final AbstractTheme.ThemeManager themeManager;
                    if (!AvecForeignParent.class.isAssignableFrom(tc.tableClass)) {
                        themeManager = PositionDocumentTheme.generateThemeManager(tc.tableName, PositionDocument.class, tc.tableClass);
                    } else {
                        themeManager = AbstractTheme.generateThemeManager(tc.tabName, tc.tableName, tc.tableClass);
                    }
                    themes[i] = themeManager;
                    i++;
                }
                final FXBergeThemePane tab = new FXBergeThemePane(bergeBox, themes);
                uiCenter.setCenter(tab);
            } else {
                final TabContent tc = contents.get(0);
                final AbstractTheme.ThemeManager themeManager = AbstractTheme.generateThemeManager(tc.tableName, tc.tableClass);
                final FXBergeThemePane tab = new FXBergeThemePane(bergeBox, themeManager);
                uiCenter.setCenter(tab);
            }
        }
     }
}
