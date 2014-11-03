
package fr.sym.digue;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.sym.theme.AbstractPojoTable;
import fr.symadrem.sirs.core.component.SystemeReperageRepository;
import fr.symadrem.sirs.core.model.Element;
import fr.symadrem.sirs.core.model.SystemeReperage;
import fr.symadrem.sirs.core.model.SystemeReperageBorne;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import org.geotoolkit.gui.javafx.util.FXDateField;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SystemeReperageController extends BorderPane {
    
    @FXML private TextField uiNom;
    @FXML private HTMLEditor uiComment;
    @FXML private FXDateField uiDate;
    
    private final ObjectProperty<SystemeReperage> srProperty = new SimpleObjectProperty<>();    
    private final BorneTable borneTable = new BorneTable();
    
    public SystemeReperageController(){
        Symadrem.loadFXML(this);
                
        setCenter(borneTable);
        
        srProperty.addListener((ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue) -> {
            if(oldValue!=null){
                uiNom.textProperty().unbindBidirectional(oldValue.nomProperty());
                uiDate.valueProperty().unbindBidirectional(oldValue.dateMajProperty());
                borneTable.getUiTable().setItems(FXCollections.emptyObservableList());
            }
            updateFields();
        });
    }

    public ObjectProperty<SystemeReperage> getSystemeReperageProperty() {
        return srProperty;
    }
    
    private void updateFields(){
        final SystemeReperage sr = srProperty.get();
        if(sr==null) return;
        
        uiNom.textProperty().bindBidirectional(sr.nomProperty());
        uiDate.valueProperty().bindBidirectional(sr.dateMajProperty());
        uiComment.setHtmlText(sr.getCommentaire());
                
        borneTable.getUiTable().setItems(sr.systemereperageborneId);
    }
    
    public void save(){
        final SystemeReperage sr = srProperty.get();
        if(sr==null) return;
        
        sr.setCommentaire(uiComment.getHtmlText());
        final Session session = Injector.getBean(Session.class);
        final SystemeReperageRepository repo = session.getSystemeReperageRepository();
        
        repo.update(sr);
    }
    
    private class BorneTable extends AbstractPojoTable{

        public BorneTable() {
            super(SystemeReperageBorne.class, "Liste des bornes");
        }

        @Override
        protected void deletePojos(Element... pojos) {
            
        }

        @Override
        protected void editPojo(Element pojo) {
            
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            
        }

        @Override
        protected void createPojo() {
            
        }
        
    }
    
}
