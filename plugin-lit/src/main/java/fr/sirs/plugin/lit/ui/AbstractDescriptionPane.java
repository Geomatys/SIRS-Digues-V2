
package fr.sirs.plugin.lit.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.TronconLit;
import fr.sirs.plugin.lit.util.TabContent;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.PositionDocumentTheme;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author guilhem
 */
public class AbstractDescriptionPane extends BorderPane {

    @FXML
    private ComboBox<TronconLit> bergeBox;
    
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
                        themeManager = AbstractTheme.generateThemeManager(tc.tableName, tc.tableClass);
                    }
                    themes[i] = themeManager;
                    i++;
                }
                final FXLitThemePane tab = new FXLitThemePane(bergeBox, themes);
                uiCenter.setCenter(tab);
            } else {
                final TabContent tc = contents.get(0);
                final AbstractTheme.ThemeManager themeManager = AbstractTheme.generateThemeManager(tc.tableName, tc.tableClass);
                final FXLitThemePane tab = new FXLitThemePane(bergeBox, themeManager);
                uiCenter.setCenter(tab);
            }
        }
     }
}