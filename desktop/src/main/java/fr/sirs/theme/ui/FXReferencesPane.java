
package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.Element;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReferencesPane extends BorderPane {

    @FXML private BorderPane uiCenter;
    @FXML private ComboBox<Element> uiTronconChoice;
        
    public FXReferencesPane() {
        SIRS.loadFXML(this);
        
        
        
                
                
//        if(groups.length==1){
//            final DefaultTronconPojoTable table = new DefaultTronconPojoTable(groups[0]);
//            table.tronconPropoerty().bindBidirectional(uiTronconChoice.valueProperty());
//            uiCenter.setCenter(table);
//        }else{
//            final TabPane pane = new TabPane();
//            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
//            for(int i=0;i<groups.length;i++){
//                final DefaultTronconPojoTable table = new DefaultTronconPojoTable(groups[i]);
//                table.tronconPropoerty().bindBidirectional(uiTronconChoice.valueProperty());
//                final Tab tab = new Tab(groups[i].getName());
//                tab.setContent(table);
//                pane.getTabs().add(tab);
//            }
//            uiCenter.setCenter(pane);
//        }
//        
//        //chargement de la liste des troncons disponibles
//        final Session session = Injector.getBean(Session.class);
//        final List<TronconDigue> troncons = session.getTronconDigueRepository().getAll();
//        uiTronconChoice.setItems(FXCollections.observableList(troncons));        
//        uiTronconChoice.setConverter(new StringConverter<TronconDigue>() {
//            @Override
//            public String toString(TronconDigue object) {
//                if(object==null) return "";
//                else return object.getLibelle();
//            }
//            @Override
//            public TronconDigue fromString(String string) {
//                if(string==null) return null;
//                final ObservableList<TronconDigue> items = uiTronconChoice.getItems();
//                for(TronconDigue troncon : items){
//                    if(troncon!=null && troncon.getLibelle().toLowerCase().startsWith(string.toLowerCase())){
//                        return troncon;
//                    }
//                }
//                return null;
//            }
//        });
//        
//        new ComboBoxCompletion(uiTronconChoice);
//        
//        if(!troncons.isEmpty()){
//            uiTronconChoice.getSelectionModel().select(troncons.get(0));
//        }
    }
    
    
    
}
