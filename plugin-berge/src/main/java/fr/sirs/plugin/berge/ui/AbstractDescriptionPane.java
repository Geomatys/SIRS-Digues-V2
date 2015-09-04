
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
                final FXBergeThemePane tab = new FXBergeThemePane(bergeBox, themes);
                this.setCenter(tab);
            } else {
                final TabContent tc = contents.get(0);
                final AbstractTheme.ThemeManager themeManager = AbstractTheme.generateThemeManager(tc.tableName, tc.tableClass);
                final FXBergeThemePane tab = new FXBergeThemePane(bergeBox, themeManager);
                this.setCenter(tab);
            }
        }
     }
}
