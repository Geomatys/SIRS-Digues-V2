
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
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.List;
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
        
        List<PojoTable> pojoTables = new ArrayList<>();
                
        if (contents != null) {
            if (contents.size() > 1) {
                final TabPane tPane = new TabPane();
                for (TabContent tc : contents) {
                    final Tab t = new Tab(tc.tabName);
                    final PojoTable tab = new PojoTable(tc.tableClass, tc.tableName);
                    pojoTables.add(tab);
                    t.setContent(tab);
                    tPane.getTabs().add(t);
                }
                this.setCenter(tPane);
            } else {
                final TabContent tc = contents.get(0);
                final PojoTable tab = new PojoTable(tc.tableClass, tc.tableName);
                pojoTables.add(tab);
                this.setCenter(tab);
            }
        }
        
        final BergeRepository repo = Injector.getBean(BergeRepository.class);
        
        // Liste de toutes les berges
        final ObservableList<Berge> all = FXCollections.observableList(repo.getAll());
        // TODO final SuiviBergePane.BergeDocumentListener oblListener = new SuiviBergePane.BergeDocumentListener(all);
        bergeBox.setItems(all);
        
        bergeBox.setCellFactory(new Callback() {
                @Override
                public ListCell call(Object param) {
                    return new ListCell(){
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText("Sélectionner une Berge");
                            } else {
                                final Berge berge = (Berge) item;
                                setText("B : " + berge.getLibelle());
                            }
                        }
                    } ;
                }
            });
        bergeBox.setConverter(new SirsStringConverter());
        
        /*for (PojoTable pt : pojoTables) {
            pt.setParentElement(berge);
        } TODO */
        
     }
}
