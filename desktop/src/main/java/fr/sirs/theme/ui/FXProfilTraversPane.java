
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXProfilTraversPane extends AbstractFXElementPane<ProfilTravers> {
    
    private final ObservableList<LeveProfilTravers> leves = FXCollections.observableArrayList();
    
    private final BooleanProperty disableFields = new SimpleBooleanProperty();
    
    private final ProfilTraversRepository profilTraversRepository;
    private final LeveProfilTraversTable levesTable = new LeveProfilTraversTable();
    
    
    @FXML private TextField uiLibelle;
    @FXML private VBox uiVbox;
    
    private FXProfilTraversPane(){
        SIRS.loadFXML(this);
        final Session session = Injector.getBean(Session.class);
        profilTraversRepository = session.getProfilTraversRepository();
    }
    
    public FXProfilTraversPane(final ProfilTravers profilTravers){
        this();
        uiVbox.getChildren().add(levesTable);
        levesTable.editableProperty().bind(disableProperty().not().and(elementProperty.isNotNull()));
        
        elementProperty.addListener((ObservableValue<? extends ProfilTravers> observable, ProfilTravers oldValue, ProfilTravers newValue) -> {
            initFields();
        });
        setElement(profilTravers);
    }       
            
    private void initFields(){
        if (elementProperty.get() != null) {
            uiLibelle.textProperty().bindBidirectional(elementProperty.get().libelleProperty());
            leves.setAll(elementProperty.get().getLeves());
        } else {
            uiLibelle.textProperty().unbind();
            uiLibelle.textProperty().set("");
        }
    }

    public BooleanProperty disableFieldsProperty() {
        return disableFields;
    }

    @Override
    public void preSave() {
        final ProfilTravers profilTravers = elementProperty.get();
        if (profilTravers != null) {
            profilTravers.setDateMaj(LocalDateTime.now());
            profilTraversRepository.update(profilTravers);
        }
    }

    private class LeveProfilTraversTable extends PojoTable {

        public LeveProfilTraversTable() {
            super(LeveProfilTravers.class, "Liste des levés de profils en travers");
            setTableItems(()-> (ObservableList) leves);
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            // TODO
        }
                
        @Override
        protected Object createPojo() {
            final LeveProfilTravers leve = new LeveProfilTravers();
            elementProperty.get().getLeves().add(leve);
            return leve;
        }
    }
}
