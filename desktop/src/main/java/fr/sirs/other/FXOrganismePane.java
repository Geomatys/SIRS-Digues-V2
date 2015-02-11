package fr.sirs.other;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.core.model.Role.ADMIN;
import static fr.sirs.core.model.Role.EXTERN;
import static fr.sirs.core.model.Role.USER;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Organisme;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.FXElementPane;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.beans.binding.BooleanBinding;
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
    
    @FXML
    private FXEditMode uiMode;

    @FXML
    private GridPane uiDescriptionGrid;
    @FXML
    private GridPane uiAdresseGrid;
    
    @FXML private TextField uiRaisonSocialeTextField;
    @FXML private TextField uiStatutJuridiqueTextField;
    @FXML private TextField uiTelTextField;
    @FXML private TextField uiEmailTextField;
    @FXML private TextField uiAdresseTextField;
    @FXML private TextField uiCodePostalTextField;
    @FXML private TextField uiCommuneTextField;
    
    @FXML private DatePicker uiDebutDatePicker;
    @FXML private DatePicker uiFinDatePicker;
    
    @FXML private Tab uiContactOrganismesTab;
    
    private final ContactOrganismeTable coTable;

    public FXOrganismePane(Organisme organisme) {
        SIRS.loadFXML(this);

        uiMode.setAllowedRoles(ADMIN, USER, EXTERN);
        final BooleanBinding editProp = uiMode.editionState().not();
        for (final Node child : uiDescriptionGrid.getChildren()) {
            if (!(child instanceof Label)) {
                child.disableProperty().bind(editProp);
            }
        }
        for (final Node child : uiAdresseGrid.getChildren()) {
            if (!(child instanceof Label)) {
                child.disableProperty().bind(editProp);
            }
        }
        
        coTable = new ContactOrganismeTable();
        uiContactOrganismesTab.setContent(coTable);
        setElement(organisme);
    }
    
    public void initPane() {
        final Organisme organisme;
        if (elementProperty.get() == null) {
            organisme = Injector.getSession().getOrganismeRepository().create();
            uiMode.setSaveAction(()->{organisme.setDateMaj(LocalDateTime.now()); Injector.getSession().getOrganismeRepository().add(organisme);});
        } else {
            organisme = elementProperty.get();
            uiMode.setSaveAction(()->{organisme.setDateMaj(LocalDateTime.now()); Injector.getSession().getOrganismeRepository().update(organisme);});
        }
        
        uiRaisonSocialeTextField.textProperty().bindBidirectional(organisme.nomProperty());
        uiStatutJuridiqueTextField.textProperty().bindBidirectional(organisme.statut_juridiqueProperty());
        uiTelTextField.textProperty().bindBidirectional(organisme.telephoneProperty());
        uiEmailTextField.textProperty().bindBidirectional(organisme.emailProperty());
        uiAdresseTextField.textProperty().bindBidirectional(organisme.adresseProperty());
        uiCodePostalTextField.textProperty().bindBidirectional(organisme.code_postalProperty());
        uiCommuneTextField.textProperty().bindBidirectional(organisme.communeProperty());
        
        if (organisme.getDate_debut() != null) {
            uiDebutDatePicker.valueProperty().set(organisme.getDate_debut().toLocalDate());
        }
        uiDebutDatePicker.valueProperty().addListener((ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
            if (newValue == null) {
                organisme.date_debutProperty().set(null);
            } else {
                organisme.date_debutProperty().set(LocalDateTime.of(newValue, LocalTime.MIN));
            }
        });
        
        if (organisme.getDate_fin() != null) {
            uiFinDatePicker.valueProperty().set(organisme.getDate_fin().toLocalDate());
        }
        uiFinDatePicker.valueProperty().addListener((ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
            if (newValue == null) {
                organisme.date_finProperty().set(null);
            } else {
                organisme.date_finProperty().set(LocalDateTime.of(newValue, LocalTime.MIN));
            }
        });
        
        coTable.setTableItems(()-> (ObservableList) organisme.contactOrganisme);
    }

    @Override
    public void preSave() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Table listant les rattachement de l'organisme courant aux contacts connus.
     * Aucune opération de suavegarde n'est effectuée ici, elles seront appliquées
     * lors de la sauvegarde globale du panneau.
     */
    private final class ContactOrganismeTable extends PojoTable {

        public ContactOrganismeTable() {
            super(ContactOrganisme.class, "Liste des organismes");
            editableProperty().bind(uiFicheMode.selectedProperty());
        }

        @Override
        protected void editPojo(Object pojo) {
            if (!(pojo instanceof ContactOrganisme)) {
                return;
            }
            final ContactOrganisme co = (ContactOrganisme) pojo;
            final Tab tab = new FXFreeTab("Rattachement");
            tab.setContent(new FXContactOrganismePane(co));
            session.getFrame().addTab(tab);
        }
        
        @Override
        protected void deletePojos(Element... pojos) {
            ((ObservableList)elementProperty.get().contactOrganisme).removeAll(pojos);
        }

        @Override
        protected Object createPojo() {
            final ContactOrganisme co = new ContactOrganisme();
            co.setParent(elementProperty.get());
            co.setDateDebutIntervenant(LocalDateTime.now());
            
            return co;
        }
    }
}
