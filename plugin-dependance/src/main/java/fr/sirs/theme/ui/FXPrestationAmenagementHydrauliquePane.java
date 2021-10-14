
package fr.sirs.theme.ui;

import fr.sirs.theme.ui.*;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.javafx.FloatSpinnerValueFactory;
import fr.sirs.util.FXFreeTab;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class FXPrestationAmenagementHydrauliquePane extends AbstractFXElementPane<PrestationAmenagementHydraulique> {
    
    protected final Previews previewRepository;
    protected LabelMapper labelMapper;
    
    @FXML private FXValidityPeriodPane uiValidityPeriod;
    
    // Propriétés de PrestationAmenagementHydraulique
    @FXML protected TextField ui_libelle;
    @FXML protected Spinner ui_coutMetre;
    @FXML protected Spinner ui_coutGlobal;
    @FXML protected CheckBox ui_realisationInterne;
    @FXML protected Spinner ui_cote;
    @FXML protected Spinner ui_mesureDiverse;
    @FXML protected TextArea ui_commentaire;
    @FXML protected ComboBox ui_sourceId;
    @FXML protected Button ui_sourceId_link;
    @FXML protected ComboBox ui_typePrestationId;
    @FXML protected Button ui_typePrestationId_link;
    @FXML protected ComboBox ui_marcheId;
    @FXML protected Button ui_marcheId_link;
    @FXML protected FXFreeTab ui_desordreIds;
    protected ListeningPojoTable desordreIdsTable;
    @FXML protected FXFreeTab ui_ouvrageAssocieAmenagementHydrauliqueIds;
    protected ListeningPojoTable ouvrageAssocieAmenagementHydrauliqueIdsTable;
    @FXML protected FXFreeTab ui_photos;
    protected PojoTable photosTable;
    
    // Propriétés de AvecGeometrie
    
    // Propriétés de AvecSettableGeometrie
    
    // Propriétés de AbstractAmenagementHydraulique
    @FXML protected ComboBox ui_amenagementHydrauliqueId;
    @FXML protected Button ui_amenagementHydrauliqueId_link;
    
    @FXML FXPositionDependancePane uiPosition;
    
    /**
     * Constructor. Initialize part of the UI which will not require update when
     * element edited change.
     */
    protected FXPrestationAmenagementHydrauliquePane() {
        SIRS.loadFXML(this, PrestationAmenagementHydraulique.class);
        final Session session = Injector.getBean(Session.class);
        previewRepository = session.getPreviews();
        elementProperty().addListener(this::initFields);
        
        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());
        
        /*
        * Disabling rules.
        */
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_coutMetre.disableProperty().bind(disableFieldsProperty());
        ui_coutMetre.setEditable(true);
        ui_coutMetre.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_coutGlobal.disableProperty().bind(disableFieldsProperty());
        ui_coutGlobal.setEditable(true);
        ui_coutGlobal.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_realisationInterne.disableProperty().bind(disableFieldsProperty());
        ui_cote.disableProperty().bind(disableFieldsProperty());
        ui_cote.setEditable(true);
        ui_cote.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_mesureDiverse.disableProperty().bind(disableFieldsProperty());
        ui_mesureDiverse.setEditable(true);
        ui_mesureDiverse.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_commentaire.setWrapText(true);
        ui_commentaire.editableProperty().bind(disableFieldsProperty().not());
        ui_sourceId.disableProperty().bind(disableFieldsProperty());
        ui_sourceId_link.setVisible(false);
        ui_typePrestationId.disableProperty().bind(disableFieldsProperty());
        ui_typePrestationId_link.setVisible(false);
        ui_marcheId.disableProperty().bind(disableFieldsProperty());
        ui_marcheId_link.disableProperty().bind(ui_marcheId.getSelectionModel().selectedItemProperty().isNull());
        ui_marcheId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_marcheId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_marcheId.getSelectionModel().getSelectedItem()));
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());
        
        uiPosition.dependanceProperty().bind(elementProperty);
        
        ui_desordreIds.setContent(() -> {
            desordreIdsTable = new ListeningPojoTable(Desordre.class, null, elementProperty());
            desordreIdsTable.editableProperty().bind(disableFieldsProperty().not());
            desordreIdsTable.createNewProperty().set(false);
            updateDesordreIdsTable(session, elementProperty.get());
            return desordreIdsTable;
        });
        ui_desordreIds.setClosable(false);
        
        ui_ouvrageAssocieAmenagementHydrauliqueIds.setContent(() -> {
            ouvrageAssocieAmenagementHydrauliqueIdsTable = new ListeningPojoTable(OuvrageAssocieAmenagementHydraulique.class, null, elementProperty());
            ouvrageAssocieAmenagementHydrauliqueIdsTable.editableProperty().bind(disableFieldsProperty().not());
            ouvrageAssocieAmenagementHydrauliqueIdsTable.createNewProperty().set(false);
            updateOuvrageAssocieAmenagementHydrauliqueIdsTable(session, elementProperty.get());
            return ouvrageAssocieAmenagementHydrauliqueIdsTable;
        });
        ui_ouvrageAssocieAmenagementHydrauliqueIds.setClosable(false);
        
        ui_photos.setContent(() -> {
            photosTable = new PojoTable(PhotoDependance.class, null, elementProperty());
            photosTable.editableProperty().bind(disableFieldsProperty().not());
            updatePhotosTable(session, elementProperty.get());
            return photosTable;
        });
        ui_photos.setClosable(false);
        ui_amenagementHydrauliqueId.disableProperty().bind(disableFieldsProperty());
        ui_amenagementHydrauliqueId_link.disableProperty().bind(ui_amenagementHydrauliqueId.getSelectionModel().selectedItemProperty().isNull());
        ui_amenagementHydrauliqueId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_amenagementHydrauliqueId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_amenagementHydrauliqueId.getSelectionModel().getSelectedItem()));
    }
    
    public FXPrestationAmenagementHydrauliquePane(final PrestationAmenagementHydraulique prestationAmenagementHydraulique){
        this();
        this.elementProperty().set(prestationAmenagementHydraulique);
    }
    
    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends PrestationAmenagementHydraulique > observableElement, PrestationAmenagementHydraulique oldElement, PrestationAmenagementHydraulique newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
            // Propriétés de PrestationAmenagementHydraulique
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
            ui_libelle.setText(null);
            ui_coutMetre.getValueFactory().valueProperty().unbindBidirectional(oldElement.coutMetreProperty());
            ui_coutMetre.getValueFactory().setValue(0);
            ui_coutGlobal.getValueFactory().valueProperty().unbindBidirectional(oldElement.coutGlobalProperty());
            ui_coutGlobal.getValueFactory().setValue(0);
            ui_realisationInterne.selectedProperty().unbindBidirectional(oldElement.realisationInterneProperty());
            ui_realisationInterne.setSelected(false);
            ui_cote.getValueFactory().valueProperty().unbindBidirectional(oldElement.coteProperty());
            ui_cote.getValueFactory().setValue(0);
            ui_mesureDiverse.getValueFactory().valueProperty().unbindBidirectional(oldElement.mesureDiverseProperty());
            ui_mesureDiverse.getValueFactory().setValue(0);
            ui_commentaire.textProperty().unbindBidirectional(oldElement.commentaireProperty());
            ui_commentaire.setText(null);
            // Propriétés de AvecGeometrie
            // Propriétés de AvecSettableGeometrie
            // Propriétés de AbstractAmenagementHydraulique
        }
        
        final Session session = Injector.getBean(Session.class);
        
        if (newElement == null) {
            
            ui_sourceId.setItems(null);
            ui_typePrestationId.setItems(null);
            ui_marcheId.setItems(null);
            ui_amenagementHydrauliqueId.setItems(null);
        } else {
            
            
            /*
            * Bind control properties to Element ones.
            */
            // Propriétés de PrestationAmenagementHydraulique
            // * libelle
            ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
            // * coutMetre
            ui_coutMetre.getValueFactory().valueProperty().bindBidirectional(newElement.coutMetreProperty());
            // * coutGlobal
            ui_coutGlobal.getValueFactory().valueProperty().bindBidirectional(newElement.coutGlobalProperty());
            // * realisationInterne
            ui_realisationInterne.selectedProperty().bindBidirectional(newElement.realisationInterneProperty());
            // * cote
            ui_cote.getValueFactory().valueProperty().bindBidirectional(newElement.coteProperty());
            // * mesureDiverse
            ui_mesureDiverse.getValueFactory().valueProperty().bindBidirectional(newElement.mesureDiverseProperty());
            // * commentaire
            ui_commentaire.textProperty().bindBidirectional(newElement.commentaireProperty());
            final AbstractSIRSRepository<RefSource> sourceIdRepo = session.getRepositoryForClass(RefSource.class);
            SIRS.initCombo(ui_sourceId, SIRS.observableList(sourceIdRepo.getAll()), newElement.getSourceId() == null? null : sourceIdRepo.get(newElement.getSourceId()));
            final AbstractSIRSRepository<RefPrestation> typePrestationIdRepo = session.getRepositoryForClass(RefPrestation.class);
            SIRS.initCombo(ui_typePrestationId, SIRS.observableList(typePrestationIdRepo.getAll()), newElement.getTypePrestationId() == null? null : typePrestationIdRepo.get(newElement.getTypePrestationId()));
            {
                final Preview linearPreview = newElement.getMarcheId() == null ? null : previewRepository.get(newElement.getMarcheId());
                SIRS.initCombo(ui_marcheId, SIRS.observableList(
                        previewRepository.getByClass(linearPreview == null ? Marche.class : linearPreview.getJavaClassOr(Marche.class))).sorted(), linearPreview);
            }
            // Propriétés de AvecGeometrie
            // Propriétés de AvecSettableGeometrie
            // Propriétés de AbstractAmenagementHydraulique
            Preview linearPreview = newElement.getAmenagementHydrauliqueId() == null ? null : previewRepository.get(newElement.getAmenagementHydrauliqueId());
            final List<Preview> byClass = previewRepository.getByClass(linearPreview == null ? AmenagementHydraulique.class : linearPreview.getJavaClassOr(AmenagementHydraulique.class));
            final List<Preview> withoutEmptyPreview = byClass.stream().filter(p -> p.getElementId() != null).collect(Collectors.toList());
            final ObservableList<Preview> sorted = SIRS.observableList(withoutEmptyPreview).sorted();

            if (linearPreview == null && sorted.size() >= 1) {
                linearPreview = sorted.get(0);
            }
            SIRS.initCombo(ui_amenagementHydrauliqueId, sorted, linearPreview);
        }
        
        updateDesordreIdsTable(session, newElement);
        updateOuvrageAssocieAmenagementHydrauliqueIdsTable(session, newElement);
        updatePhotosTable(session, newElement);
    }
    
    
    protected void updateDesordreIdsTable(final Session session, final PrestationAmenagementHydraulique newElement) {
        if (desordreIdsTable == null)
            return;
        
        if (newElement == null) {
            desordreIdsTable.setTableItems(null);
        } else {
            desordreIdsTable.setParentElement(null);
            final AbstractSIRSRepository<Desordre> desordreIdsRepo = session.getRepositoryForClass(Desordre.class);
            desordreIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getDesordreIds(), desordreIdsRepo));
            desordreIdsTable.setObservableListToListen(newElement.getDesordreIds());
        }
    }
    
    
    protected void updateOuvrageAssocieAmenagementHydrauliqueIdsTable(final Session session, final PrestationAmenagementHydraulique newElement) {
        if (ouvrageAssocieAmenagementHydrauliqueIdsTable == null)
            return;
        
        if (newElement == null) {
            ouvrageAssocieAmenagementHydrauliqueIdsTable.setTableItems(null);
        } else {
            ouvrageAssocieAmenagementHydrauliqueIdsTable.setParentElement(null);
            final AbstractSIRSRepository<OuvrageAssocieAmenagementHydraulique> ouvrageAssocieAmenagementHydrauliqueIdsRepo = session.getRepositoryForClass(OuvrageAssocieAmenagementHydraulique.class);
            ouvrageAssocieAmenagementHydrauliqueIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOuvrageAssocieAmenagementHydrauliqueIds(), ouvrageAssocieAmenagementHydrauliqueIdsRepo));
            ouvrageAssocieAmenagementHydrauliqueIdsTable.setObservableListToListen(newElement.getOuvrageAssocieAmenagementHydrauliqueIds());
        }
    }
    
    
    protected void updatePhotosTable(final Session session, final PrestationAmenagementHydraulique newElement) {
        if (photosTable == null)
            return;
        
        if (newElement == null) {
            photosTable.setTableItems(null);
        } else {
            photosTable.setParentElement(newElement);
            photosTable.setTableItems(()-> (ObservableList) newElement.getPhotos());
        }
    }
    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final PrestationAmenagementHydraulique element = (PrestationAmenagementHydraulique) elementProperty().get();
        
        
        element.setCommentaire(ui_commentaire.getText());
        
        
        Object cbValue;
        cbValue = ui_sourceId.getValue();
        if (cbValue instanceof Preview) {
            element.setSourceId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setSourceId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setSourceId(null);
        }
        cbValue = ui_typePrestationId.getValue();
        if (cbValue instanceof Preview) {
            element.setTypePrestationId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypePrestationId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypePrestationId(null);
        }
        cbValue = ui_marcheId.getValue();
        if (cbValue instanceof Preview) {
            element.setMarcheId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setMarcheId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setMarcheId(null);
        }
        if (desordreIdsTable != null) {
            // Manage opposite references for Desordre...
            final List<String> currentDesordreIdsList = new ArrayList<>();
            for(final Element elt : desordreIdsTable.getAllValues()){
                final Desordre desordre = (Desordre) elt;
                currentDesordreIdsList.add(desordre.getId());
            }
            element.setDesordreIds(currentDesordreIdsList);
            
        }
        if (ouvrageAssocieAmenagementHydrauliqueIdsTable != null) {
            // Manage opposite references for OuvrageAssocieAmenagementHydraulique...
            final List<String> currentOuvrageAssocieAmenagementHydrauliqueIdsList = new ArrayList<>();
            for(final Element elt : ouvrageAssocieAmenagementHydrauliqueIdsTable.getAllValues()){
                final OuvrageAssocieAmenagementHydraulique ouvrageAssocieAmenagementHydraulique = (OuvrageAssocieAmenagementHydraulique) elt;
                currentOuvrageAssocieAmenagementHydrauliqueIdsList.add(ouvrageAssocieAmenagementHydraulique.getId());
            }
            element.setOuvrageAssocieAmenagementHydrauliqueIds(currentOuvrageAssocieAmenagementHydrauliqueIdsList);
            
        }
        cbValue = ui_amenagementHydrauliqueId.getValue();
        if (cbValue instanceof Preview) {
            element.setAmenagementHydrauliqueId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setAmenagementHydrauliqueId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setAmenagementHydrauliqueId(null);
        }
    }
}
