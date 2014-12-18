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
import fr.sirs.core.model.Organisme;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXContactOrganismePane extends AbstractFXElementPane<ContactOrganisme> {
    
    @FXML private Label uiDocId;
    @FXML private FXEditMode uiMode;
    
    @FXML ComboBox<Contact> uiContactChoice;
    @FXML ComboBox<Organisme> uiOrganismeChoice;
    
    @FXML DatePicker uiDebutDatePicker;
    @FXML DatePicker uiFinDatePicker;
    
    private final OrganismeRepository orgRepository;
    private final ContactRepository contactRepository;
    
    private Organisme originalOrg;
    
    public FXContactOrganismePane(ContactOrganisme co) {
        SIRS.loadFXML(this);
        
        final Session session = Injector.getSession();
        orgRepository = session.getOrganismeRepository();
        contactRepository = session.getContactRepository();
        
        uiMode.setAllowedRoles(ADMIN, USER, EXTERNE);
        final BooleanBinding editProp = uiMode.editionState().not();
        uiContactChoice.disableProperty().bind(editProp);
        uiOrganismeChoice.disableProperty().bind(editProp);
        uiDebutDatePicker.disableProperty().bind(editProp);
        uiFinDatePicker.disableProperty().bind(editProp);
        
        initContacts(uiContactChoice);
        initOrganismes(uiOrganismeChoice);
        
        uiMode.setSaveAction(this::save);
        elementProperty.addListener((ObservableValue<? extends ContactOrganisme> observable, ContactOrganisme oldValue, ContactOrganisme newValue) -> {
            initPane();
        });
        setElement(co);
    }
    
    public void initPane() {
        final ContactOrganisme contactOrganisme = elementProperty.get();
        
        if (contactOrganisme == null) return;
        
        originalOrg = (Organisme) contactOrganisme.getParent();
        
        uiOrganismeChoice.getSelectionModel().select(originalOrg);
        uiContactChoice.getSelectionModel().select(
                contactRepository.get(contactOrganisme.getContactId()));
        if (contactOrganisme.getDateDebutIntervenant() != null) {
            uiDebutDatePicker.valueProperty().set(contactOrganisme.getDateDebutIntervenant().toLocalDate());
        }
        if (contactOrganisme.getDateFinIntervenant() != null) {
            uiFinDatePicker.valueProperty().set(contactOrganisme.getDateFinIntervenant().toLocalDate());
        }
    }
       
    private void save() {        
        if (originalOrg != null) {
            orgRepository.update(originalOrg);
        }
        
        final ContactOrganisme contactOrganisme = elementProperty.get();
        if (contactOrganisme != null) {
            final Organisme newOrg = (Organisme) contactOrganisme.getParent();
            if (newOrg != null && !newOrg.equals(originalOrg)) {
                orgRepository.update(newOrg);
            }

            originalOrg = newOrg;
        }
    }

    private void initContacts(ComboBox contactComboBox) {
        ObservableList<Contact> allContacts = FXCollections.observableArrayList(contactRepository.getAll());
        contactComboBox.setItems(allContacts);
        new ComboBoxCompletion(contactComboBox);
        contactComboBox.setConverter(new SirsStringConverter());
    }
    
    private void initOrganismes(ComboBox orgComboBox) {
        ObservableList<Organisme> allOrganisms = FXCollections.observableArrayList(orgRepository.getAll());
        orgComboBox.setItems(allOrganisms);
        new ComboBoxCompletion(orgComboBox);
        orgComboBox.setConverter(new SirsStringConverter());
    }

    @Override
    public void preSave() {
        final ContactOrganisme contactOrganisme = elementProperty().get();
        if (contactOrganisme == null) return;
        if (uiOrganismeChoice.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Le champs Organisme ne doit pas être vide !", ButtonType.OK).showAndWait();
            return;
        } 
        if (uiContactChoice.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Le champs Contact ne doit pas être vide !", ButtonType.OK).showAndWait();
            return;
        }
        
        contactOrganisme.setParent(uiOrganismeChoice.getValue());
        contactOrganisme.setContactId(uiContactChoice.getValue().getId());
        
        if (uiFinDatePicker.getValue() != null) {
            contactOrganisme.setDateDebutIntervenant(LocalDateTime.of(
                    uiDebutDatePicker.getValue(), LocalTime.MIN));
        }
        if (uiFinDatePicker.getValue() != null) {
            contactOrganisme.setDateFinIntervenant(LocalDateTime.of(
                    uiFinDatePicker.getValue(), LocalTime.MIN));
        }
    }
}
