
package fr.sirs.other;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import static fr.sirs.Session.Role.ADMIN;
import static fr.sirs.Session.Role.EXTERNE;
import static fr.sirs.Session.Role.USER;
import fr.sirs.core.component.ContactRepository;
import fr.sirs.core.component.OrganismeRepository;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Organisme;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXContactPane extends AbstractFXElementPane<Contact> {
    
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
    @FXML private TextField uiCodePostale;
    @FXML private TextField uiCommune;
    
    @FXML private Tab uiOrganismeTab;
    
    private final PojoTable organismeTable;
    
    private final ContactRepository contactRepository;
    private final OrganismeRepository orgRepository;
    
    final ObservableList orgsOfContact = FXCollections.observableArrayList();
    
    final HashSet<Organisme> modifiedOrgs = new HashSet<>();
    
    /**
     *
     * @param contact
     */
    public FXContactPane(Contact contact) {
        SIRS.loadFXML(this);
        
        final Session session = Injector.getSession();
        this.contactRepository = session.getContactRepository();
        this.orgRepository = session.getOrganismeRepository();
        
        uiMode.setAllowedRoles(ADMIN, USER, EXTERNE);
        final BooleanProperty editProp = uiMode.editionState();
        uiNom.editableProperty().bind(editProp);
        uiPrenom.editableProperty().bind(editProp);
        uiService.editableProperty().bind(editProp);
        uiFonction.editableProperty().bind(editProp);
        uiTelephone.editableProperty().bind(editProp);
        uiFax.editableProperty().bind(editProp);
        uiEmail.editableProperty().bind(editProp);
        uiAdresse.editableProperty().bind(editProp);
        uiCodePostale.editableProperty().bind(editProp);
        uiCommune.editableProperty().bind(editProp);
        
        organismeTable = new ContactOrganismeTable();
        uiOrganismeTab.setContent(organismeTable);
        // /!\ If you remove "and" condition, you must add null check in below ContactOrganismeTable.
        organismeTable.editableProperty().bind(editProp.and(elementProperty.isNotNull()));
        
        uiMode.setSaveAction(this::save);
        setElement(contact);
        
        orgsOfContact.addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                    // We check only removed elements, because new ones do not 
                // have an attached organism.
                final Iterator<ContactOrganisme> it = c.getRemoved().iterator();
                while (it.hasNext()) {
                    final ContactOrganisme co = it.next();
                    if (co.getParent() != null) {
                        modifiedOrgs.add((Organisme) co.getParent());
                    }
                }
            }
        });
    }
    
    private void save(){
        if (elementProperty.get() != null) {
            contactRepository.update(elementProperty.get());
        }
        modifiedOrgs.stream().forEach((org) -> orgRepository.update(org));
        modifiedOrgs.clear();
    }

    @Override
    public void preSave() {        
        final Contact contact = elementProperty.get();
        
        orgsOfContact.clear();
        modifiedOrgs.clear();
        
        // We should not need to unbind fields, as they use weak listeners.
        if (contact == null) return;
        
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
               
        // Retrieve all organisms current contact is / was part of.
        if (contact.getId() != null) {
            for (final Organisme org : orgRepository.getAll()) {
                orgsOfContact.addAll(org.contactOrganisme.filtered((ContactOrganisme co) -> {
                    return contact.getId().equals(co.getContactId());
                }));
            }
        }
    }
 
    /**
     * Table listant les rattachement du contact courant aux organismes connus.
     * Aucune opération de sauvegarde n'est effectuée ici, elles seront appliquées
     * lors de la sauvegarde globale du panneau.
     */
    private final class ContactOrganismeTable extends PojoTable {

        public ContactOrganismeTable() {
            super(ContactOrganisme.class, "Liste des organismes");
            editableProperty().bind(uiMode.editionState());
            setTableItems(() -> orgsOfContact);          
        }

        @Override
        protected void editPojo(Object pojo) {
            if (!(pojo instanceof ContactOrganisme)) {
                return;
            }
            final ContactOrganisme co = (ContactOrganisme) pojo;
            co.contactIdProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                if (!elementProperty.get().getId().equals(newValue)) {
                    orgsOfContact.remove(co);
                }
            });
            final Tab tab = new FXFreeTab("Rattachement");
            tab.setContent(new FXContactOrganismePane(co));
            session.getFrame().addTab(tab);
        }
        
        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            if (event.getRowValue() instanceof ContactOrganisme) {
                final ContactOrganisme co = (ContactOrganisme) event.getRowValue();
                if (co.getParent() != null) {
                    modifiedOrgs.add((Organisme)co.getParent());
                }
            }
        }
        
        @Override
        protected void deletePojos(Element... pojos) {
            orgsOfContact.removeAll(pojos);
        }

        @Override
        protected Object createPojo() {
            final ContactOrganisme co = new ContactOrganisme();
            co.setContactId(elementProperty.get().getId());
            co.setDateDebutIntervenant(LocalDateTime.now());
            orgsOfContact.add(co);
            return co;
        }
    }
    
}
