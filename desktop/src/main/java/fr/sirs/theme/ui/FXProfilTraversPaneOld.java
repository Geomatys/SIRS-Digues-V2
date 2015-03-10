
package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXProfilTraversPaneOld extends AbstractFXElementPane<ProfilTravers> {
        
    private final BooleanProperty disableFields = new SimpleBooleanProperty();
    
    private final PojoTable levesTable = new PojoTable(LeveProfilTravers.class, "Liste des levés du profil");
    
    
    @FXML private TextField uiLibelle;
    @FXML private VBox uiVbox;
    
    private FXProfilTraversPaneOld(){
        this(null);
    }
    
    public FXProfilTraversPaneOld(final ProfilTravers profilTravers){
        SIRS.loadFXML(this, ProfilTravers.class);
        uiVbox.getChildren().add(levesTable);
        levesTable.editableProperty().bind(disableProperty().not().and(elementProperty.isNotNull()));
        
        levesTable.parentElementProperty().bind(elementProperty);
        elementProperty.addListener(this::initFields);
        setElement(profilTravers);
    }       
            
    private void initFields(ObservableValue<? extends ProfilTravers> observable, ProfilTravers oldValue, ProfilTravers newValue) {
        if (oldValue != null) {
            uiLibelle.textProperty().unbindBidirectional(oldValue.libelleProperty());
        }
        if (newValue != null) {
            uiLibelle.textProperty().bindBidirectional(newValue.libelleProperty());
            levesTable.setTableItems(()-> (ObservableList) newValue.getLeves());
        } else {
            uiLibelle.textProperty().set("");
            levesTable.setTableItems(()-> null);
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
        }
    }
}
