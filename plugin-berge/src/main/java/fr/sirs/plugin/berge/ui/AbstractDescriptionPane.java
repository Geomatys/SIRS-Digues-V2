
package fr.sirs.plugin.berge.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.theme.ui.PojoTable;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.plugin.berge.util.TabContent;
import java.util.List;

/**
 *
 * @author guilhem
 */
public class AbstractDescriptionPane extends BorderPane {

    @FXML
    private ComboBox<?> bergeBox;
    
     public AbstractDescriptionPane() {
        SIRS.loadFXML(this);
     }
     
     public AbstractDescriptionPane(final List<TabContent> contents) {
        SIRS.loadFXML(this);
        
        if (contents.size() > 1) {
            final TabPane tPane = new TabPane();
            for (TabContent tc : contents) {
                final Tab t = new Tab(tc.tabName);
                final PojoTable tab = new PojoTable(tc.tableClass, tc.tableName);
                t.setContent(tab);
                tPane.getTabs().add(t);
            }
            this.setCenter(tPane);
        } else {
            final TabContent tc = contents.get(0);
            final PojoTable tab = new PojoTable(tc.tableClass, tc.tableName);
            this.setCenter(tab);
        }
        
     }
}
