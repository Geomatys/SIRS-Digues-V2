
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.FXFileTextField;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXConventionPane extends AbstractFXElementPane<Convention> {

    private final Previews previewRepository;
    private LabelMapper labelMapper;
    
    @FXML FXValidityPeriodPane uiValidityPeriod;

    // Propriétés de Convention
    @FXML TextField ui_libelle;
    @FXML HTMLEditor ui_commentaire;
    @FXML CheckBox ui_dossierComplet;
    @FXML DatePicker ui_dateSignature;
    @FXML CheckBox ui_taciteReconduction;
    @FXML CheckBox ui_exonerationRedevance;
    @FXML Spinner ui_montantRedevance;
    @FXML ComboBox ui_typeConventionId;
    @FXML Button ui_typeConventionId_link;
    @FXML Tab ui_organismeSignataireIds;
    final ListeningPojoTable organismeSignataireIdsTable;
    @FXML Tab ui_contactSignataireIds;
    final ListeningPojoTable contactSignataireIdsTable;
    @FXML Tab ui_piecesJointes;
    final PojoTable piecesJointesTable;

    // Propriétés de SIRSFileReference

    // Propriétés de SIRSReference

    // Propriétés de SIRSDefaultReference
    @FXML TextField ui_referencePapier;
    @FXML FXFileTextField ui_chemin;

    // Propriétés de SIRSDocument
    @FXML Tab ui_positionDocument;
    final PositionConventionPojoTable positionDocumentTable;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXConventionPane() {
        SIRS.loadFXML(this, Convention.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

		/*
		 * Disabling rules.
		 */
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_dossierComplet.disableProperty().bind(disableFieldsProperty());
        ui_dateSignature.disableProperty().bind(disableFieldsProperty());
        ui_taciteReconduction.disableProperty().bind(disableFieldsProperty());
        ui_exonerationRedevance.disableProperty().bind(disableFieldsProperty());
        ui_montantRedevance.disableProperty().bind(disableFieldsProperty());
        ui_montantRedevance.setEditable(true);
        ui_montantRedevance.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_typeConventionId.disableProperty().bind(disableFieldsProperty());
        ui_typeConventionId_link.setVisible(false);
        organismeSignataireIdsTable = new ListeningPojoTable(Organisme.class, null);
        organismeSignataireIdsTable.editableProperty().bind(disableFieldsProperty().not());
        organismeSignataireIdsTable.createNewProperty().set(false);
        ui_organismeSignataireIds.setContent(organismeSignataireIdsTable);
        ui_organismeSignataireIds.setClosable(false);
        contactSignataireIdsTable = new ListeningPojoTable(Contact.class, null);
        contactSignataireIdsTable.editableProperty().bind(disableFieldsProperty().not());
        contactSignataireIdsTable.createNewProperty().set(false);
        ui_contactSignataireIds.setContent(contactSignataireIdsTable);
        ui_contactSignataireIds.setClosable(false);
        piecesJointesTable = new PojoTable(ConventionPJ.class, null);
        piecesJointesTable.editableProperty().bind(disableFieldsProperty().not());
        ui_piecesJointes.setContent(piecesJointesTable);
        ui_piecesJointes.setClosable(false);
        ui_referencePapier.disableProperty().bind(disableFieldsProperty());
        ui_chemin.disableFieldsProperty.bind(disableFieldsProperty());
        positionDocumentTable = new PositionConventionPojoTable(null);
        positionDocumentTable.editableProperty().bind(disableFieldsProperty().not());
        ui_positionDocument.setContent(positionDocumentTable);
        ui_positionDocument.setClosable(false);
    }
    
    public FXConventionPane(final Convention convention){
        this();
        this.elementProperty().set(convention);
        
        organismeSignataireIdsTable.setObservableListToListen(elementProperty.get().getOrganismeSignataireIds());
        contactSignataireIdsTable.setObservableListToListen(elementProperty.get().getContactSignataireIds());
    }     

    /**
     * Initialize fields at element setting.
     */
    private void initFields(ObservableValue<? extends Convention > observableElement, Convention oldElement, Convention newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de Convention
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
            ui_dossierComplet.selectedProperty().unbindBidirectional(oldElement.dossierCompletProperty());
            ui_dateSignature.valueProperty().unbindBidirectional(oldElement.dateSignatureProperty());
            ui_taciteReconduction.selectedProperty().unbindBidirectional(oldElement.taciteReconductionProperty());
            ui_exonerationRedevance.selectedProperty().unbindBidirectional(oldElement.exonerationRedevanceProperty());

            ui_montantRedevance.getValueFactory().valueProperty().unbindBidirectional(oldElement.montantRedevanceProperty());
        // Propriétés de SIRSFileReference
        // Propriétés de SIRSReference
        // Propriétés de SIRSDefaultReference
            ui_referencePapier.textProperty().unbindBidirectional(oldElement.referencePapierProperty());
            ui_chemin.textProperty().unbindBidirectional(oldElement.cheminProperty());
        // Propriétés de SIRSDocument
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de Convention
        // * libelle
        ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
        // * commentaire
        ui_commentaire.setHtmlText(newElement.getCommentaire());
        // * dossierComplet
        ui_dossierComplet.selectedProperty().bindBidirectional(newElement.dossierCompletProperty());
        // * dateSignature
        ui_dateSignature.valueProperty().bindBidirectional(newElement.dateSignatureProperty());
        // * taciteReconduction
        ui_taciteReconduction.selectedProperty().bindBidirectional(newElement.taciteReconductionProperty());
        // * exonerationRedevance
        ui_exonerationRedevance.selectedProperty().bindBidirectional(newElement.exonerationRedevanceProperty());
        // * montantRedevance
        ui_montantRedevance.getValueFactory().valueProperty().bindBidirectional(newElement.montantRedevanceProperty());

        SIRS.initCombo(ui_typeConventionId, FXCollections.observableArrayList(
            previewRepository.getByClass(RefConvention.class)), 
            newElement.getTypeConventionId() == null? null : previewRepository.get(newElement.getTypeConventionId()));
        organismeSignataireIdsTable.setParentElement(null);
        final AbstractSIRSRepository<Organisme> organismeSignataireIdsRepo = session.getRepositoryForClass(Organisme.class);
        organismeSignataireIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOrganismeSignataireIds(), organismeSignataireIdsRepo));
        contactSignataireIdsTable.setParentElement(null);
        final AbstractSIRSRepository<Contact> contactSignataireIdsRepo = session.getRepositoryForClass(Contact.class);
        contactSignataireIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getContactSignataireIds(), contactSignataireIdsRepo));
        piecesJointesTable.setParentElement(newElement);
        piecesJointesTable.setTableItems(()-> (ObservableList) newElement.getPiecesJointes());
        // Propriétés de SIRSFileReference

        // Propriétés de SIRSReference

        // Propriétés de SIRSDefaultReference
        // * referencePapier
        ui_referencePapier.textProperty().bindBidirectional(newElement.referencePapierProperty());
        // * chemin
        ui_chemin.textProperty().bindBidirectional(newElement.cheminProperty());

        // Propriétés de SIRSDocument

        positionDocumentTable.setPropertyToListen("sirsdocumentProperty", elementProperty().get().getId());
        positionDocumentTable.setTableItems(()-> (ObservableList) SIRS.getPositionDocumentByDocumentId(elementProperty().get().getId()));
    }
    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final Convention element = (Convention) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());


        Object cbValue;
        cbValue = ui_typeConventionId.getValue();
        if (cbValue instanceof Preview) {
            element.setTypeConventionId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypeConventionId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypeConventionId(null);
        }
        // Manage opposite references for Organisme...
        final List<String> currentOrganismeIdsList = new ArrayList<>();
        for(final Element elt : organismeSignataireIdsTable.getAllValues()){
            final Organisme organisme = (Organisme) elt;
            currentOrganismeIdsList.add(organisme.getId());
        }
        element.setOrganismeSignataireIds(currentOrganismeIdsList);
        
        // Manage opposite references for Contact...
        final List<String> currentContactIdsList = new ArrayList<>();
        for(final Element elt : contactSignataireIdsTable.getAllValues()){
            final Contact contact = (Contact) elt;
            currentContactIdsList.add(contact.getId());
        }
        element.setContactSignataireIds(currentContactIdsList);
        
    }
}
