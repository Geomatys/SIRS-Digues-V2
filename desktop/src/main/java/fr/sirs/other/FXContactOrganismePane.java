package fr.sirs.other;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.ContactRepository;
import fr.sirs.core.component.OrganismeRepository;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Organisme;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXContactOrganismePane extends BorderPane {

    private ContactOrganisme contactOrganisme;
    
    @FXML private Label uiDocId;
    @FXML private FXEditMode uiMode;
    
    @FXML ComboBox<Contact> uiContactChoice;
    @FXML ComboBox<Organisme> uiOrganismeChoice;
    
    @FXML DatePicker uiDebutDatePicker;
    @FXML DatePicker uiFinDatePicker;
    
    @FXML FXEditMode uiEditMode;
    
    private final OrganismeRepository orgRepository;
    private final ContactRepository contactRepository;
    
    private Organisme originalOrg;
    
    public FXContactOrganismePane(ContactOrganisme co) {
        SIRS.loadFXML(this);
        
        final Session session = Injector.getSession();
        orgRepository = session.getOrganismeRepository();
        contactRepository = session.getContactRepository();
        
        final BooleanBinding editProp = uiMode.editionState().not();
        uiContactChoice.disableProperty().bind(editProp);
        uiOrganismeChoice.disableProperty().bind(editProp);
        uiDebutDatePicker.disableProperty().bind(editProp);
        uiFinDatePicker.disableProperty().bind(editProp);
        
        initContacts(uiContactChoice);
        initOrganismes(uiOrganismeChoice);
        
        uiMode.setSaveAction(this::save);
        setContactOrganisme(co);
    }
    
    public void setContactOrganisme(final ContactOrganisme co) {
        contactOrganisme = co;
        
        if (co == null) return;
        
        originalOrg = (Organisme) contactOrganisme.getParent();
        
        uiOrganismeChoice.getSelectionModel().select(originalOrg);
        uiContactChoice.getSelectionModel().select(
                contactRepository.get(contactOrganisme.getContactId()));
        if (co.getDateDebutIntervenant() != null) {
            uiDebutDatePicker.valueProperty().set(co.getDateDebutIntervenant().toLocalDate());
        }
        if (co.getDateFinIntervenant() != null) {
            uiFinDatePicker.valueProperty().set(co.getDateFinIntervenant().toLocalDate());
        }
    }
       
    private void save() {
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
        
        if (originalOrg != null) {
            orgRepository.update(originalOrg);
        }
        
        final Organisme newOrg = (Organisme) contactOrganisme.getParent();
        if (newOrg != null && !newOrg.equals(originalOrg)) {
            orgRepository.update(newOrg);
        }
        
        originalOrg = newOrg;
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
}
