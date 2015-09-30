package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Organisme;
import java.time.LocalDate;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXOrganismePane extends AbstractFXElementPane<Organisme> {

    @FXML private GridPane uiDescriptionGrid;
    @FXML private GridPane uiAdresseGrid;
    
    @FXML private TextField uiRaisonSocialeTextField;
    @FXML private TextField uiStatutJuridiqueTextField;
    @FXML private TextField uifaxOrgTextField;
    @FXML private TextField uiTelTextField;
    @FXML private TextField uiEmailTextField;
    @FXML private TextField uiAdresseTextField;
    @FXML private TextField uiCodePostalTextField;
    @FXML private TextField uiCommuneTextField;
    
    @FXML private DatePicker uiDebutDatePicker;
    @FXML private DatePicker uiFinDatePicker;
    
    @FXML private Tab uiContactOrganismesTab;
    
    private final PojoTable contactOrganismeTable;

    public FXOrganismePane(Organisme organisme) {
        SIRS.loadFXML(this);

        for (final Node child : uiDescriptionGrid.getChildren()) {
            if (!(child instanceof Label)) {
                child.disableProperty().bind(disableFieldsProperty());
            }
        }
        for (final Node child : uiAdresseGrid.getChildren()) {
            if (!(child instanceof Label)) {
                child.disableProperty().bind(disableFieldsProperty());
            }
        }
        
        contactOrganismeTable = new PojoTable(ContactOrganisme.class, "Contacts rattachés");
        contactOrganismeTable.parentElementProperty().bind(elementProperty);
        uiContactOrganismesTab.setContent(contactOrganismeTable);
        
        elementProperty.addListener(this::initPane);
        setElement(organisme);
    }
    
    private void initPane(ObservableValue<? extends Organisme> observable, Organisme oldValue, Organisme newValue) {

        if (oldValue != null) {
            uiRaisonSocialeTextField.textProperty().unbindBidirectional(oldValue.nomProperty());
            uiStatutJuridiqueTextField.textProperty().unbindBidirectional(oldValue.statutJuridiqueProperty());
            uifaxOrgTextField.textProperty().unbindBidirectional(oldValue.faxProperty());
            uiTelTextField.textProperty().unbindBidirectional(oldValue.telephoneProperty());
            uiEmailTextField.textProperty().unbindBidirectional(oldValue.emailProperty());
            uiAdresseTextField.textProperty().unbindBidirectional(oldValue.adresseProperty());
            uiCodePostalTextField.textProperty().unbindBidirectional(oldValue.codePostalProperty());
            uiCommuneTextField.textProperty().unbindBidirectional(oldValue.communeProperty());
        }
        
        final Organisme organisme;
        if (newValue == null) {
            organisme = Injector.getSession().getRepositoryForClass(Organisme.class).create();
        } else {
            organisme = newValue;
        }
        
        uiRaisonSocialeTextField.textProperty().bindBidirectional(organisme.nomProperty());
        uiStatutJuridiqueTextField.textProperty().bindBidirectional(organisme.statutJuridiqueProperty());
        uifaxOrgTextField.textProperty().bindBidirectional(organisme.faxProperty());
        uiTelTextField.textProperty().bindBidirectional(organisme.telephoneProperty());
        uiEmailTextField.textProperty().bindBidirectional(organisme.emailProperty());
        uiAdresseTextField.textProperty().bindBidirectional(organisme.adresseProperty());
        uiCodePostalTextField.textProperty().bindBidirectional(organisme.codePostalProperty());
        uiCommuneTextField.textProperty().bindBidirectional(organisme.communeProperty());
        
        if (organisme.getDate_debut() != null) {
            uiDebutDatePicker.valueProperty().set(organisme.getDate_debut());
        }
        uiDebutDatePicker.valueProperty().addListener((ObservableValue<? extends LocalDate> observableDate, LocalDate oldDate, LocalDate newDate) -> {
            if (newDate == null) {
                organisme.date_debutProperty().set(null);
            } else {
                organisme.date_debutProperty().set(newDate);
            }
        });
        
        if (organisme.getDate_fin() != null) {
            uiFinDatePicker.valueProperty().set(organisme.getDate_fin());
        }
        uiFinDatePicker.valueProperty().addListener((ObservableValue<? extends LocalDate> observableDate, LocalDate oldDate, LocalDate newDate) -> {
            if (newDate == null) {
                organisme.date_finProperty().set(null);
            } else {
                organisme.date_finProperty().set(newDate);
            }
        });
        
        contactOrganismeTable.setTableItems(()-> (ObservableList) organisme.contactOrganisme);
    }

    @Override
    public void preSave() {
        // nothing to do, all is done by JavaFX bindings.
    }
}
