
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconThemePane extends BorderPane {

    @FXML private BorderPane uiCenter;
    @FXML private ComboBox<TronconDigue> uiTronconChoice;
        
    public FXTronconThemePane(AbstractTronconTheme.ThemeGroup ... groups) {
        SIRS.loadFXML(this);
        
        if(groups.length==1){
            final DefaultTronconPojoTable table = new DefaultTronconPojoTable(groups[0]);
            table.tronconPropoerty().bindBidirectional(uiTronconChoice.valueProperty());
            uiCenter.setCenter(table);
        }else{
            final TabPane pane = new TabPane();
            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            for(int i=0;i<groups.length;i++){
                final DefaultTronconPojoTable table = new DefaultTronconPojoTable(groups[i]);
                table.tronconPropoerty().bindBidirectional(uiTronconChoice.valueProperty());
                final Tab tab = new Tab(groups[i].getName());
                tab.setContent(table);
                pane.getTabs().add(tab);
            }
            uiCenter.setCenter(pane);
        }
        
        //chargement de la liste des troncons disponibles
        final Session session = Injector.getBean(Session.class);
        final List<TronconDigue> troncons = session.getTronconDigueRepository().getAll();
        uiTronconChoice.setItems(FXCollections.observableList(troncons));        
        uiTronconChoice.setConverter(new StringConverter<TronconDigue>() {
            @Override
            public String toString(TronconDigue object) {
                if(object==null) return "";
                else return object.getLibelle();
            }
            @Override
            public TronconDigue fromString(String string) {
                if(string==null) return null;
                final ObservableList<TronconDigue> items = uiTronconChoice.getItems();
                for(TronconDigue troncon : items){
                    if(troncon!=null && troncon.getLibelle().toLowerCase().startsWith(string.toLowerCase())){
                        return troncon;
                    }
                }
                return null;
            }
        });
        
        new AutoCompleteComboBoxListener<>(uiTronconChoice);
        
        if(!troncons.isEmpty()){
            uiTronconChoice.getSelectionModel().select(troncons.get(0));
        }
    }
    
    public static class AutoCompleteComboBoxListener<T> implements EventHandler<KeyEvent> {

        private final ComboBox comboBox;
        private final ObservableList<T> data;
        private boolean moveCaretToPos = false;
        private int caretPos;

        public AutoCompleteComboBoxListener(final ComboBox comboBox) {
            this.comboBox = comboBox;
            this.data = comboBox.getItems();
            this.comboBox.setEditable(true);
            this.comboBox.setOnKeyPressed((KeyEvent t) -> {comboBox.hide();});
            this.comboBox.setOnKeyReleased(AutoCompleteComboBoxListener.this);
        }

        @Override
        public void handle(KeyEvent event) {

            final KeyCode code = event.getCode();
            final String text = comboBox.getEditor().getText();
            
            if (code == KeyCode.UP) {
                caretPos = -1;
                moveCaret(text.length());
                return;
            } else if (code == KeyCode.DOWN) {
                if (!comboBox.isShowing()) {
                    comboBox.show();
                }
                caretPos = -1;
                moveCaret(text.length());
                return;
            } else if (code == KeyCode.BACK_SPACE) {
                moveCaretToPos = true;
                caretPos = comboBox.getEditor().getCaretPosition();
            } else if (code == KeyCode.DELETE) {
                moveCaretToPos = true;
                caretPos = comboBox.getEditor().getCaretPosition();
            }

            if (code == KeyCode.RIGHT || code == KeyCode.LEFT
              ||event.isControlDown() || code == KeyCode.HOME
              ||code == KeyCode.END   || code == KeyCode.TAB) {
                return;
            }

            ObservableList list = FXCollections.observableArrayList();
            for (int i = 0; i < data.size(); i++) {
                final String dataStr = comboBox.getConverter().toString(data.get(i));
                if (dataStr.toLowerCase().startsWith(
                        AutoCompleteComboBoxListener.this.comboBox
                        .getEditor().getText().toLowerCase())) {
                    list.add(data.get(i));
                }
            }

            comboBox.setItems(list);
            comboBox.getEditor().setText(text);
            if (!moveCaretToPos) {
                caretPos = -1;
            }
            moveCaret(text.length());
            if (!list.isEmpty()) {
                comboBox.show();
            }
        }

        private void moveCaret(int textLength) {
            if (caretPos == -1) {
                comboBox.getEditor().positionCaret(textLength);
            } else {
                comboBox.getEditor().positionCaret(caretPos);
            }
            moveCaretToPos = false;
        }

    }
    
    
}
