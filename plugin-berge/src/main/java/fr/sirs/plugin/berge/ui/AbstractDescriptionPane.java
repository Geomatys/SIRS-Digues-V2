
package fr.sirs.plugin.berge.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.BergeRepository;
import fr.sirs.core.model.Berge;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.theme.ui.PojoTable;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.plugin.berge.util.TabContent;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.ui.ForeignParentPojoTable;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.util.Callback;

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
                //final TabPane tPane = new TabPane();
                AbstractTheme.ThemeManager[] themes = new AbstractTheme.ThemeManager[contents.size()];
                int i = 0;
                for (TabContent tc : contents) {
                    final AbstractTheme.ThemeManager themeManager = AbstractTheme.generateThemeManager(tc.tableName, tc.tableClass);
                    themes[i] = themeManager;
                    i++;
                    //final Tab t = new Tab(tc.tabName);
                    //t.setContent(tab);
                    //tPane.getTabs().add(t);
                }
                final FXBergeThemePane tab = new FXBergeThemePane(bergeBox, themes);
                this.setCenter(tab);
                //this.setCenter(tPane);
            } else {
                final TabContent tc = contents.get(0);
                final AbstractTheme.ThemeManager themeManager = AbstractTheme.generateThemeManager(tc.tableName, tc.tableClass);
                final FXBergeThemePane tab = new FXBergeThemePane(bergeBox, themeManager);
                this.setCenter(tab);
            }
        }
     }
}
