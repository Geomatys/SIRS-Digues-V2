
package fr.sirs.other;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Element;
import fr.sirs.theme.ui.PojoTable;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXContactPane extends BorderPane {
    
    
    @FXML private Label uiDocId;
    @FXML private FXEditMode uiMode;
    
    @FXML private TextField uiNom;
    @FXML private TextField uiPrenom;
    @FXML private TextField uiService;
    @FXML private TextField uiFonction;
    @FXML private TextField uiTelephone;
    @FXML private TextField uiFax;
    @FXML private TextField uiEmail;
    
    @FXML private TextField uiAdresse;
    @FXML private TextField uiComplement;
    @FXML private TextField uiCodePostale;
    @FXML private TextField uiCommune;
    
    @FXML private Tab uiOrganismeTab;
    
    private final PojoTable organismeTable = new OrganismeTable();
    
    private Contact contact;
    
    public FXContactPane(Contact contact) {
        SIRS.loadFXML(this);
        
        final BooleanProperty editProp = uiMode.editionState();
        uiNom.editableProperty().bind(editProp);
        uiPrenom.editableProperty().bind(editProp);
        uiService.editableProperty().bind(editProp);
        uiFonction.editableProperty().bind(editProp);
        uiTelephone.editableProperty().bind(editProp);
        uiFax.editableProperty().bind(editProp);
        uiEmail.editableProperty().bind(editProp);
        uiAdresse.editableProperty().bind(editProp);
        uiComplement.editableProperty().bind(editProp);
        uiCodePostale.editableProperty().bind(editProp);
        uiCommune.editableProperty().bind(editProp);
        
        uiOrganismeTab.setContent(organismeTable);
        organismeTable.editableProperty().bind(editProp);
        
        uiMode.setSaveAction(this::save);        
        setContact(contact);
    }
    
    public void setContact(Contact contact){
        this.contact = contact;
        
        uiDocId.setText(contact.getDocumentId());
        
        uiNom.textProperty().bindBidirectional(contact.nomProperty());
        uiPrenom.textProperty().bindBidirectional(contact.prenomProperty());
        uiService.textProperty().bindBidirectional(contact.serviceProperty());
        uiFonction.textProperty().bindBidirectional(contact.fonctionProperty());
        uiTelephone.textProperty().bindBidirectional(contact.telephoneProperty());
        uiFax.textProperty().bindBidirectional(contact.faxProperty());
        uiEmail.textProperty().bindBidirectional(contact.emailProperty());
        uiAdresse.textProperty().bindBidirectional(contact.adresseProperty());
        uiCodePostale.textProperty().bindBidirectional(contact.code_postalProperty());
        uiCommune.textProperty().bindBidirectional(contact.paysProperty());
        
        organismeTable.setTableItems(()->(ObservableList)contact.contactOrganisme);
        
    }
    
    private void save(){
        final Session session = Injector.getSession();
        session.getContactRepository().update(contact);
    }
 
    private final class OrganismeTable extends PojoTable{

        public OrganismeTable() {
            super(ContactOrganisme.class, "Liste des organismes");
        }

        @Override
        protected void editPojo(Element pojo) {
            
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            //objet inclue dans Contact
        }

        @Override
        protected void deletePojos(Element... pojos) {
            for(Element ele : pojos){
                contact.contactOrganisme.remove(ele);
            }
        }
        
    }
    
}
