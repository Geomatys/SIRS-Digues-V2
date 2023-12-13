
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.*;
import fr.sirs.theme.ui.pojotable.PojoTableExternalAddable;
import fr.sirs.ui.Growl;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.StreamingIterable;
import fr.sirs.util.javafx.FloatSpinnerValueFactory;
import fr.sirs.util.property.SirsPreferences;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.util.collection.CloseableIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Estelle Idée (Geomatys)
 */
public class FXPrestationsOnTronconsPane extends AbstractFXElementPane<Prestation> {

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;

    // Propriétés de Positionable
    @FXML private BorderPane uiPositionable;
    @FXML private FXValidityPeriodPane uiValidityPeriod;

    // Propriétés de Prestation
    @FXML private TextField uiDesignation;
    @FXML protected TextField ui_libelle;
    @FXML protected Spinner ui_coutMetre;
    @FXML protected Spinner ui_coutGlobal;
    @FXML protected CheckBox ui_realisationInterne;
    @FXML protected CheckBox ui_registreAttribution;
    @FXML protected ComboBox ui_coteId;
    @FXML protected Button ui_coteId_link;
    @FXML protected ComboBox ui_positionId;
    @FXML protected Button ui_positionId_link;
    @FXML protected ComboBox ui_sourceId;
    @FXML protected Button ui_sourceId_link;
    @FXML protected ComboBox ui_typePrestationId;
    @FXML protected Button ui_typePrestationId_link;
    @FXML protected ComboBox ui_marcheId;
    @FXML protected Button ui_marcheId_link;
    @FXML protected FXFreeTab ui_evenementHydrauliqueIds;
    protected ListeningPojoTable evenementHydrauliqueIdsTable;
    @FXML protected FXFreeTab ui_intervenantsIds;
    protected ListeningPojoTable intervenantsIdsTable;
    @FXML protected FXFreeTab ui_rapportEtudeIds;
    protected ListeningPojoTable rapportEtudeIdsTable;
    @FXML protected FXFreeTab ui_documentGrandeEchelleIds;
    protected ListeningPojoTable documentGrandeEchelleIdsTable;
    @FXML protected FXFreeTab ui_observations;
    protected PojoTable observationsTable;
    @FXML protected FXFreeTab ui_globalPrestationIds;
    protected ListeningPojoTable globalPrestationIdsTable;

    // Propriétés de AvecGeometrie

    // Propriétés de AvecSettableGeometrie

    // Propriétés de Objet
    @FXML protected TextArea ui_commentaire;
    @FXML protected ComboBox ui_linearId;
    @FXML protected Button ui_linearId_link;

    // Propriétés de ObjetPhotographiable
//    @FXML protected FXFreeTab ui_photos;
//    protected PojoTable photosTable;

    // Propriétés de AvecObservations

    /**
     * Constructor. Initialize part of the UI which will not require update when
     * element edited change.
     */
    protected FXPrestationsOnTronconsPane() {
        SIRS.loadFXML(this, Prestation.class);
        final Session session = Injector.getBean(Session.class);
        previewRepository = session.getPreviews();
        elementProperty().addListener(this::initFields);

//        uiPositionable.disableFieldsProperty().bind(disableFieldsProperty());
//        uiPositionable.positionableProperty().bind(elementProperty());
        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

        /*
         * Disabling rules.
         */
        uiDesignation.disableProperty().bind(disableFieldsProperty());
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_coutMetre.disableProperty().bind(disableFieldsProperty());
        ui_coutMetre.setEditable(true);
        ui_coutMetre.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_coutGlobal.disableProperty().bind(disableFieldsProperty());
        ui_coutGlobal.setEditable(true);
        ui_coutGlobal.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_realisationInterne.disableProperty().bind(disableFieldsProperty());
        ui_registreAttribution.disableProperty().bind(disableFieldsProperty());
        ui_coteId.disableProperty().bind(disableFieldsProperty());
        ui_coteId_link.setVisible(false);
        ui_positionId.disableProperty().bind(disableFieldsProperty());
        ui_positionId_link.setVisible(false);
        ui_sourceId.disableProperty().bind(disableFieldsProperty());
        ui_sourceId_link.setVisible(false);
        ui_typePrestationId.disableProperty().bind(disableFieldsProperty());
        ui_typePrestationId_link.setVisible(false);
        ui_marcheId.disableProperty().bind(disableFieldsProperty());
        ui_marcheId_link.disableProperty().bind(ui_marcheId.getSelectionModel().selectedItemProperty().isNull());
        ui_marcheId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_marcheId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_marcheId.getSelectionModel().getSelectedItem()));

        ui_evenementHydrauliqueIds.setContent(() -> {
            evenementHydrauliqueIdsTable = new ListeningPojoTable(EvenementHydraulique.class, null, elementProperty());
            evenementHydrauliqueIdsTable.editableProperty().bind(disableFieldsProperty().not());
            evenementHydrauliqueIdsTable.createNewProperty().set(false);
            updateEvenementHydrauliqueIdsTable(session, elementProperty.get());
            return evenementHydrauliqueIdsTable;
        });
        ui_evenementHydrauliqueIds.setClosable(false);

        ui_intervenantsIds.setContent(() -> {
            intervenantsIdsTable = new ListeningPojoTable(Contact.class, null, elementProperty());
            intervenantsIdsTable.editableProperty().bind(disableFieldsProperty().not());
            intervenantsIdsTable.createNewProperty().set(false);
            updateIntervenantsIdsTable(session, elementProperty.get());
            return intervenantsIdsTable;
        });
        ui_intervenantsIds.setClosable(false);

        ui_rapportEtudeIds.setContent(() -> {
            rapportEtudeIdsTable = new ListeningPojoTable(RapportEtude.class, null, elementProperty());
            rapportEtudeIdsTable.editableProperty().bind(disableFieldsProperty().not());
            rapportEtudeIdsTable.createNewProperty().set(false);
            updateRapportEtudeIdsTable(session, elementProperty.get());
            return rapportEtudeIdsTable;
        });
        ui_rapportEtudeIds.setClosable(false);

        ui_documentGrandeEchelleIds.setContent(() -> {
            documentGrandeEchelleIdsTable = new ListeningPojoTable(DocumentGrandeEchelle.class, null, elementProperty());
            documentGrandeEchelleIdsTable.editableProperty().bind(disableFieldsProperty().not());
            documentGrandeEchelleIdsTable.createNewProperty().set(false);
            updateDocumentGrandeEchelleIdsTable(session, elementProperty.get());
            return documentGrandeEchelleIdsTable;
        });
        ui_documentGrandeEchelleIds.setClosable(false);

        ui_observations.setContent(() -> {
            observationsTable = new PojoTableExternalAddable(ObservationPrestation.class, elementProperty());
            observationsTable.editableProperty().bind(disableFieldsProperty().not());
            updateObservationsTable(elementProperty.get());
            return observationsTable;
        });
        ui_observations.setClosable(false);

        ui_globalPrestationIds.setContent(() -> {
            globalPrestationIdsTable = new ListeningPojoTable(GlobalPrestation.class, null, elementProperty());
            globalPrestationIdsTable.editableProperty().bind(disableFieldsProperty().not());
            globalPrestationIdsTable.createNewProperty().set(false);
            updateGlobalPrestationIdsTable(session, elementProperty.get());
            return globalPrestationIdsTable;
        });
        ui_globalPrestationIds.setClosable(false);

        ui_commentaire.setWrapText(true);
        ui_commentaire.editableProperty().bind(disableFieldsProperty().not());
        ui_linearId.disableProperty().bind(disableFieldsProperty());
        ui_linearId_link.disableProperty().bind(ui_linearId.getSelectionModel().selectedItemProperty().isNull());
        ui_linearId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_linearId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_linearId.getSelectionModel().getSelectedItem()));

//        ui_photos.setContent(() -> {
//            photosTable = new PojoTable(Photo.class, null, elementProperty());
//            photosTable.editableProperty().bind(disableFieldsProperty().not());
//            updatePhotosTable(session, elementProperty.get());
//            return photosTable;
//        });
//        ui_photos.setClosable(false);

        // Prise en compte du changement de tronçon.
//        ui_linearId.setOnAction((event) -> uiPositionable.updateLinear(ui_linearId.getValue()));

        // The Prestation shall automatically be added to the SE registre depending on the type of Prestation.
        ui_typePrestationId.valueProperty().addListener(autoSelectRegistreListener());

        this.elementProperty().set(new Prestation());
    }

    /**
     * Change listener on typePrestationId to auto select/deselect the registreAttribution checkbox.
     * @return the @{@link ChangeListener}
     */
    private ChangeListener<RefPrestation> autoSelectRegistreListener() {
        return (obs, oldValue, newValue) -> {
            if (newValue != null) {
                elementProperty().get().setRegistreAttribution(FXPrestationPane.isAutoSelectedRegistre(newValue.getId()));
            }
        };
    }

    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends Prestation > observableElement, Prestation oldElement, Prestation newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de Prestation
            uiDesignation.textProperty().unbindBidirectional(oldElement.designationProperty());
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
            ui_libelle.setText(null);
            ui_coutMetre.getValueFactory().valueProperty().unbindBidirectional(oldElement.coutMetreProperty());
            ui_coutMetre.getValueFactory().setValue(0);
            ui_coutGlobal.getValueFactory().valueProperty().unbindBidirectional(oldElement.coutGlobalProperty());
            ui_coutGlobal.getValueFactory().setValue(0);
            ui_realisationInterne.selectedProperty().unbindBidirectional(oldElement.realisationInterneProperty());
            ui_realisationInterne.setSelected(false);
            ui_registreAttribution.selectedProperty().unbindBidirectional(oldElement.registreAttributionProperty());
            ui_registreAttribution.setSelected(false);
        // Propriétés de AvecGeometrie
        // Propriétés de AvecSettableGeometrie
        // Propriétés de Objet
            ui_commentaire.textProperty().unbindBidirectional(oldElement.commentaireProperty());
            ui_commentaire.setText(null);
        // Propriétés de ObjetPhotographiable
        // Propriétés de AvecObservations
        }

        final Session session = Injector.getBean(Session.class);

        if (newElement == null) {
            uiDesignation.textProperty().set(null);
            ui_coteId.setItems(null);
            ui_positionId.setItems(null);
            ui_sourceId.setItems(null);
            ui_typePrestationId.setItems(null);
            ui_marcheId.setItems(null);
            ui_linearId.setItems(null);
        } else {


            /*
             * Bind control properties to Element ones.
             */
            // Propriétés de Prestation
            // * libelle
            uiDesignation.textProperty().bindBidirectional(newElement.designationProperty());
            ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
            // * coutMetre
            ui_coutMetre.getValueFactory().valueProperty().bindBidirectional(newElement.coutMetreProperty());
            // * coutGlobal
            ui_coutGlobal.getValueFactory().valueProperty().bindBidirectional(newElement.coutGlobalProperty());
            // * realisationInterne
            ui_realisationInterne.selectedProperty().bindBidirectional(newElement.realisationInterneProperty());
            // * registreAttribution
            ui_registreAttribution.selectedProperty().bindBidirectional(newElement.registreAttributionProperty());

            final AbstractSIRSRepository<RefCote> coteIdRepo = session.getRepositoryForClass(RefCote.class);
            SIRS.initCombo(ui_coteId, SIRS.observableList(coteIdRepo.getAll()), (newElement.getCoteId() == null || newElement.getCoteId().trim().isEmpty()) ? null : coteIdRepo.get(newElement.getCoteId()));
            final AbstractSIRSRepository<RefPosition> positionIdRepo = session.getRepositoryForClass(RefPosition.class);
            SIRS.initCombo(ui_positionId, SIRS.observableList(positionIdRepo.getAll()), (newElement.getPositionId() == null || newElement.getPositionId().trim().isEmpty()) ? null : positionIdRepo.get(newElement.getPositionId()));
            final AbstractSIRSRepository<RefSource> sourceIdRepo = session.getRepositoryForClass(RefSource.class);
            SIRS.initCombo(ui_sourceId, SIRS.observableList(sourceIdRepo.getAll()), (newElement.getSourceId() == null || newElement.getSourceId().trim().isEmpty()) ? null : sourceIdRepo.get(newElement.getSourceId()));
            final AbstractSIRSRepository<RefPrestation> typePrestationIdRepo = session.getRepositoryForClass(RefPrestation.class);
            SIRS.initCombo(ui_typePrestationId, SIRS.observableList(typePrestationIdRepo.getAll()), (newElement.getTypePrestationId() == null || newElement.getTypePrestationId().trim().isEmpty()) ? null : typePrestationIdRepo.get(newElement.getTypePrestationId()));
            {
                final Preview linearPreview = newElement.getMarcheId() == null ? null : previewRepository.get(newElement.getMarcheId());
                // HACK-REDMINE-4408 : hide archived troncons from selection lists
                SIRS.initCombo(ui_marcheId, SIRS.observableList(
                    previewRepository.getByClass(linearPreview == null ? Marche.class : linearPreview.getJavaClassOr(Marche.class))).sorted(),
                    linearPreview, SirsPreferences.getHideArchivedProperty(), true);
            }

            // Propriétés de AvecGeometrie
            // Propriétés de AvecSettableGeometrie
            // Propriétés de Objet
            // * commentaire
            ui_commentaire.textProperty().bindBidirectional(newElement.commentaireProperty());
            {
                final Preview linearPreview = newElement.getLinearId() == null ? null : previewRepository.get(newElement.getLinearId());
                // HACK-REDMINE-4408 : hide archived troncons from selection lists
                SIRS.initCombo(ui_linearId, SIRS.observableList(
                    previewRepository.getByClass(linearPreview == null ? TronconDigue.class : linearPreview.getJavaClassOr(TronconDigue.class))).sorted(),
                    linearPreview, SirsPreferences.getHideArchivedProperty(), true);
            }
            // Propriétés de ObjetPhotographiable
            // Propriétés de AvecObservations
        }

        updateEvenementHydrauliqueIdsTable(session, newElement);
        updateIntervenantsIdsTable(session, newElement);
        updateRapportEtudeIdsTable(session, newElement);
        updateDocumentGrandeEchelleIdsTable(session, newElement);
        updateObservationsTable(newElement);
        updateGlobalPrestationIdsTable(session, newElement);
//        updatePhotosTable(session, newElement);
    }

    protected void updateEvenementHydrauliqueIdsTable(final Session session, final Prestation newElement) {
        if (evenementHydrauliqueIdsTable == null)
            return;

        if (newElement == null) {
            evenementHydrauliqueIdsTable.setTableItems(null);
        } else {
            evenementHydrauliqueIdsTable.setParentElement(null);
            final AbstractSIRSRepository<EvenementHydraulique> evenementHydrauliqueIdsRepo = session.getRepositoryForClass(EvenementHydraulique.class);
            final ObservableList<String> observableList = newElement.getEvenementHydrauliqueIds();
            evenementHydrauliqueIdsTable.setTableItems(()-> SIRS.toElementList(observableList, evenementHydrauliqueIdsRepo));
            evenementHydrauliqueIdsTable.setObservableListToListen(observableList);
        }
    }


    protected void updateIntervenantsIdsTable(final Session session, final Prestation newElement) {
        if (intervenantsIdsTable == null)
            return;

        if (newElement == null) {
            intervenantsIdsTable.setTableItems(null);
        } else {
            intervenantsIdsTable.setParentElement(null);
            final AbstractSIRSRepository<Contact> intervenantsIdsRepo = session.getRepositoryForClass(Contact.class);
            final ObservableList<String> observableList = newElement.getIntervenantsIds();
            intervenantsIdsTable.setTableItems(()-> SIRS.toElementList(observableList, intervenantsIdsRepo));
            intervenantsIdsTable.setObservableListToListen(observableList);
        }
    }


    protected void updateRapportEtudeIdsTable(final Session session, final Prestation newElement) {
        if (rapportEtudeIdsTable == null)
            return;

        if (newElement == null) {
            rapportEtudeIdsTable.setTableItems(null);
        } else {
            rapportEtudeIdsTable.setParentElement(null);
            final AbstractSIRSRepository<RapportEtude> rapportEtudeIdsRepo = session.getRepositoryForClass(RapportEtude.class);
            final ObservableList<String> observableList = newElement.getRapportEtudeIds();
            rapportEtudeIdsTable.setTableItems(()-> SIRS.toElementList(observableList, rapportEtudeIdsRepo));
            rapportEtudeIdsTable.setObservableListToListen(observableList);
        }
    }


    protected void updateDocumentGrandeEchelleIdsTable(final Session session, final Prestation newElement) {
        if (documentGrandeEchelleIdsTable == null)
            return;

        if (newElement == null) {
            documentGrandeEchelleIdsTable.setTableItems(null);
        } else {
            documentGrandeEchelleIdsTable.setParentElement(null);
            final AbstractSIRSRepository<DocumentGrandeEchelle> documentGrandeEchelleIdsRepo = session.getRepositoryForClass(DocumentGrandeEchelle.class);
            final ObservableList<String> observableList = newElement.getDocumentGrandeEchelleIds();
            documentGrandeEchelleIdsTable.setTableItems(()-> SIRS.toElementList(observableList, documentGrandeEchelleIdsRepo));
            documentGrandeEchelleIdsTable.setObservableListToListen(observableList);
        }
    }


    protected void updateObservationsTable(final Prestation newElement) {
        if (observationsTable == null)
            return;

        if (newElement == null) {
            observationsTable.setTableItems(null);
        } else {
            observationsTable.setParentElement(newElement);
            observationsTable.setTableItems(()-> (ObservableList) newElement.getObservations());
        }
    }

    protected void updateGlobalPrestationIdsTable(final Session session, final Prestation newElement) {
        if (globalPrestationIdsTable == null)
            return;

        if (newElement == null) {
            globalPrestationIdsTable.setTableItems(null);
        } else {
            globalPrestationIdsTable.setParentElement(null);
            final AbstractSIRSRepository<GlobalPrestation> globalPrestationIdsRepo = session.getRepositoryForClass(GlobalPrestation.class);
            final ObservableList<String> observableList = newElement.getGlobalPrestationIds();
            globalPrestationIdsTable.setTableItems(()-> SIRS.toElementList(observableList, globalPrestationIdsRepo));
            globalPrestationIdsTable.setObservableListToListen(observableList);
        }
    }


//    protected void updatePhotosTable(final Session session, final Prestation newElement) {
//        if (photosTable == null)
//            return;
//
//        if (newElement == null) {
//            photosTable.setTableItems(null);
//        } else {
//            photosTable.setParentElement(newElement);
//            photosTable.setTableItems(()-> (ObservableList) newElement.getPhotos());
//        }
//    }


    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final Prestation element = elementProperty.get();
        element.setCommentaire(ui_commentaire.getText());

//        uiPositionable.preSave();

        Object cbValue;
        cbValue = ui_coteId.getValue();
        if (cbValue instanceof Preview) {
            element.setCoteId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setCoteId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setCoteId(null);
        }
        cbValue = ui_positionId.getValue();
        if (cbValue instanceof Preview) {
            element.setPositionId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setPositionId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setPositionId(null);
        }
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
        if (evenementHydrauliqueIdsTable != null) {

            // Manage opposite references for EvenementHydraulique...
            final List<String> currentEvenementHydrauliqueIdsList = new ArrayList<>();
            for (final Element elt : evenementHydrauliqueIdsTable.getAllValues()) {
                final EvenementHydraulique evenementHydraulique = (EvenementHydraulique) elt;

                currentEvenementHydrauliqueIdsList.add(evenementHydraulique.getId());
            }
            element.setEvenementHydrauliqueIds(currentEvenementHydrauliqueIdsList);

        }
        if (intervenantsIdsTable != null) {

            // Manage opposite references for Contact...
            final List<String> currentContactIdsList = new ArrayList<>();
            for (final Element elt : intervenantsIdsTable.getAllValues()) {
                final Contact contact = (Contact) elt;

                currentContactIdsList.add(contact.getId());
            }
            element.setIntervenantsIds(currentContactIdsList);

        }
        if (rapportEtudeIdsTable != null) {
            final AbstractSIRSRepository<RapportEtude> rapportEtudeRepository = session.getRepositoryForClass(RapportEtude.class);


            // Manage opposite references for RapportEtude...
            /*
            * En cas de reference opposee on se prepare a stocker les objets
            * "opposes" pour les mettre � jour.
            */
            final List<RapportEtude> currentRapportEtudeList = new ArrayList<>();
            final List<String> currentRapportEtudeIdsList = new ArrayList<>();
            for (final Element elt : rapportEtudeIdsTable.getAllValues()) {
                final RapportEtude rapportEtude = (RapportEtude) elt;

                currentRapportEtudeIdsList.add(rapportEtude.getId());
                currentRapportEtudeList.add(rapportEtude);

                // Addition
                if(!rapportEtude.getPrestationIds().contains(element.getId())){
                    rapportEtude.getPrestationIds().add(element.getId());
                }
            }
            rapportEtudeRepository.executeBulk(currentRapportEtudeList);
            element.setRapportEtudeIds(currentRapportEtudeIdsList);

            // Deletion
            final StreamingIterable<RapportEtude> listRapportEtude = rapportEtudeRepository.getAllStreaming();
            try (final CloseableIterator<RapportEtude> it = listRapportEtude.iterator()) {
                while (it.hasNext()) {
                    final RapportEtude i = it.next();
                    if(i.getPrestationIds().contains(element.getId())
                        || element.getRapportEtudeIds().contains(i.getId())){
                        if(!rapportEtudeIdsTable.getAllValues().contains(i)){
                            element.getRapportEtudeIds().remove(i.getId()); //Normalement inutile du fait du  clear avant les op�rations d'ajout
                            i.getPrestationIds().remove(element.getId());
                            rapportEtudeRepository.update(i);
                        }
                    }
                }
            }
        }
        if (documentGrandeEchelleIdsTable != null) {

            // Manage opposite references for DocumentGrandeEchelle...
            final List<String> currentDocumentGrandeEchelleIdsList = new ArrayList<>();
            for (final Element elt : documentGrandeEchelleIdsTable.getAllValues()) {
                final DocumentGrandeEchelle documentGrandeEchelle = (DocumentGrandeEchelle) elt;

                currentDocumentGrandeEchelleIdsList.add(documentGrandeEchelle.getId());
            }
            element.setDocumentGrandeEchelleIds(currentDocumentGrandeEchelleIdsList);

        }
        if (globalPrestationIdsTable != null) {
            final AbstractSIRSRepository<GlobalPrestation> globalPrestationRepository = session.getRepositoryForClass(GlobalPrestation.class);


            // Manage opposite references for GlobalPrestation...
            /*
            * En cas de reference opposee on se prepare a stocker les objets
            * "opposes" pour les mettre � jour.
            */
            final List<GlobalPrestation> currentGlobalPrestationList = new ArrayList<>();
            final List<String> currentGlobalPrestationIdsList = new ArrayList<>();
            for (final Element elt : globalPrestationIdsTable.getAllValues()) {
                final GlobalPrestation globalPrestation = (GlobalPrestation) elt;

                currentGlobalPrestationIdsList.add(globalPrestation.getId());
                currentGlobalPrestationList.add(globalPrestation);

                // Addition
                if(!globalPrestation.getPrestationIds().contains(element.getId())){
                    globalPrestation.getPrestationIds().add(element.getId());
                }
            }
            globalPrestationRepository.executeBulk(currentGlobalPrestationList);
            element.setGlobalPrestationIds(currentGlobalPrestationIdsList);

            // Deletion
            final StreamingIterable<GlobalPrestation> listGlobalPrestation = globalPrestationRepository.getAllStreaming();
            try (final CloseableIterator<GlobalPrestation> it = listGlobalPrestation.iterator()) {
                while (it.hasNext()) {
                    final GlobalPrestation i = it.next();
                    if(i.getPrestationIds().contains(element.getId())
                        || element.getGlobalPrestationIds().contains(i.getId())){
                        if(!globalPrestationIdsTable.getAllValues().contains(i)){
                            element.getGlobalPrestationIds().remove(i.getId()); //Normalement inutile du fait du  clear avant les op�rations d'ajout
                            i.getPrestationIds().remove(element.getId());
                            globalPrestationRepository.update(i);
                        }
                    }
                }
            }
        }
        cbValue = ui_linearId.getValue();
        if (cbValue instanceof Preview) {
            element.setLinearId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setLinearId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setLinearId(null);
        }
    }

    public void save() {
        final AbstractSIRSRepository<Prestation> repo = Injector.getSession().getRepositoryForClass(Prestation.class);
        final List<Prestation> createdPrestations = new ArrayList<>();

        try {
            final Prestation prestation = repo.create();
            prestation.setLinearId(((Preview)ui_linearId.getValue()).getElementId());
            repo.add(prestation);

            final Prestation presta = elementProperty.get();
            presta.setId(prestation.getId());
            presta.setRevision(prestation.getRevision());
            presta.setValid(prestation.getValid());
            presta.setDocumentId(prestation.getDocumentId());
            presta.setHorodatageStatusId(prestation.getHorodatageStatusId());
            presta.setAuthor(prestation.getAuthor());

            this.preSave();

            createdPrestations.add(presta);

            repo.update(presta);

            final Growl growlInfo = new Growl(Growl.Type.INFO, "Enregistrement effectué.");
            growlInfo.showAndFade();
        } catch (Exception e) {
            final Growl growlError = new Growl(Growl.Type.ERROR, "Erreur survenue pendant l'enregistrement.");
            growlError.showAndFade();
            GeotkFX.newExceptionDialog("L'élément ne peut être sauvegardé.", e).show();
            SIRS.LOGGER.log(Level.WARNING, e.getMessage(), e);
            createdPrestations.forEach(p -> repo.remove(p));
        }

    }

    /**
     * Method called when closing the FXFreeTab containing this Pane.
     * <p>
     * This method is necessary as when reopening the element's pane,
     * a new tab is created and the old one remains existing with its listeners,
     * leading to as many calls to the listeners as the tab was closed/reopened
     * <p>
     * Method to be overriden in case more listeners are added in the FXPane of the element.
     */
    @Override
    public void removeListenersBeforeClosingTab() {
            uiValidityPeriod.removeListeners();
    }
}
