
package fr.sirs.theme.ui;

import fr.sirs.theme.ui.*;
import fr.sirs.theme.ui.pojotable.PojoTableExternalAddable;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.javafx.FloatSpinnerValueFactory;
import fr.sirs.util.StreamingIterable;
import fr.sirs.util.FXFreeTab;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;

import org.geotoolkit.util.collection.CloseableIterator;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class FXOuvrageAssocieAmenagementHydrauliquePane extends AbstractFXElementPane<OuvrageAssocieAmenagementHydraulique> {

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;


    // Propriétés de OuvrageAssocieAmenagementHydraulique
    @FXML protected Spinner ui_superficie;
    @FXML protected Spinner ui_hauteur;
    @FXML protected Spinner ui_profondeur;
    @FXML protected Spinner ui_nombre;
    @FXML protected CheckBox ui_ouvrageDerversant;
    @FXML protected Spinner ui_numCouche;
    @FXML protected Spinner ui_diametre;
    @FXML protected Spinner ui_cote;
    @FXML protected Spinner ui_section;
    @FXML protected ComboBox ui_typeOuvrage;
    @FXML protected ComboBox ui_materiauId;
    @FXML protected ComboBox ui_sourceId;
    @FXML protected ComboBox ui_etat;
    @FXML protected ComboBox ui_fonctionnement;
    @FXML protected Button ui_fonctionnement_link;
    @FXML protected FXFreeTab ui_amenagementHydrauliqueAssocieIds;
    protected ListeningPojoTable amenagementHydrauliqueAssocieIdsTable;
    @FXML protected FXFreeTab ui_desordreDependanceAssocieIds;
    protected ListeningPojoTable desordreDependanceAssocieIdsTable;
    @FXML protected FXFreeTab ui_proprietaireIds;
    protected ListeningPojoTable proprietaireIdsTable;
    @FXML protected FXFreeTab ui_gestionnaireIds;
    protected ListeningPojoTable gestionnaireIdsTable;
    @FXML protected FXFreeTab ui_observations;
    protected PojoTable observationsTable;
    @FXML protected FXFreeTab ui_photos;
    protected PojoTable photosTable;

    // Propriétés de AvecGeometrie

    // Propriétés de AvecSettableGeometrie

    // Propriétés de DescriptionAmenagementHydraulique
    @FXML protected ComboBox ui_amenagementHydrauliqueId;
    @FXML protected Button ui_amenagementHydrauliqueId_link;

    @FXML FXPositionDependancePane uiPosition;

    /**
     * Constructor. Initialize part of the UI which will not require update when
     * element edited change.
     */
    protected FXOuvrageAssocieAmenagementHydrauliquePane() {
        SIRS.loadFXML(this, OuvrageAssocieAmenagementHydraulique.class);
        final Session session = Injector.getBean(Session.class);
        previewRepository = session.getPreviews();
        elementProperty().addListener(this::initFields);


        /*
         * Disabling rules.
         */
        ui_superficie.disableProperty().bind(disableFieldsProperty());
        ui_superficie.setEditable(true);
        ui_superficie.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_hauteur.disableProperty().bind(disableFieldsProperty());
        ui_hauteur.setEditable(true);
        ui_hauteur.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_profondeur.disableProperty().bind(disableFieldsProperty());
        ui_profondeur.setEditable(true);
        ui_profondeur.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_nombre.disableProperty().bind(disableFieldsProperty());
        ui_nombre.setEditable(true);
        ui_nombre.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        ui_ouvrageDerversant.disableProperty().bind(disableFieldsProperty());
        ui_numCouche.disableProperty().bind(disableFieldsProperty());
        ui_numCouche.setEditable(true);
        ui_numCouche.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        ui_diametre.disableProperty().bind(disableFieldsProperty());
        ui_diametre.setEditable(true);
        ui_diametre.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_cote.disableProperty().bind(disableFieldsProperty());
        ui_cote.setEditable(true);
        ui_cote.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_section.disableProperty().bind(disableFieldsProperty());
        ui_section.setEditable(true);
        ui_section.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_typeOuvrage.disableProperty().bind(disableFieldsProperty());
        ui_materiauId.disableProperty().bind(disableFieldsProperty());
        ui_sourceId.disableProperty().bind(disableFieldsProperty());
        ui_etat.disableProperty().bind(disableFieldsProperty());
        ui_fonctionnement.disableProperty().bind(disableFieldsProperty());
        ui_fonctionnement_link.setVisible(false);
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());

        uiPosition.dependanceProperty().bind(elementProperty);

        ui_amenagementHydrauliqueAssocieIds.setContent(() -> {
        amenagementHydrauliqueAssocieIdsTable = new ListeningPojoTable(AmenagementHydraulique.class, null, elementProperty());
        amenagementHydrauliqueAssocieIdsTable.editableProperty().bind(disableFieldsProperty().not());
        amenagementHydrauliqueAssocieIdsTable.createNewProperty().set(false);
        updateAmenagementHydrauliqueAssocieIdsTable(session, elementProperty.get());
        return amenagementHydrauliqueAssocieIdsTable;
        });
        ui_amenagementHydrauliqueAssocieIds.setClosable(false);

        ui_desordreDependanceAssocieIds.setContent(() -> {
        desordreDependanceAssocieIdsTable = new ListeningPojoTable(DesordreDependance.class, null, elementProperty());
        desordreDependanceAssocieIdsTable.editableProperty().bind(disableFieldsProperty().not());
        desordreDependanceAssocieIdsTable.createNewProperty().set(false);
        updateDesordreDependanceAssocieIdsTable(session, elementProperty.get());
        return desordreDependanceAssocieIdsTable;
        });
        ui_desordreDependanceAssocieIds.setClosable(false);

        ui_proprietaireIds.setContent(() -> {
        proprietaireIdsTable = new ListeningPojoTable(Contact.class, null, elementProperty());
        proprietaireIdsTable.editableProperty().bind(disableFieldsProperty().not());
        proprietaireIdsTable.createNewProperty().set(false);
        updateProprietaireIdsTable(session, elementProperty.get());
        return proprietaireIdsTable;
        });
        ui_proprietaireIds.setClosable(false);

        ui_gestionnaireIds.setContent(() -> {
        gestionnaireIdsTable = new ListeningPojoTable(Organisme.class, null, elementProperty());
        gestionnaireIdsTable.editableProperty().bind(disableFieldsProperty().not());
        gestionnaireIdsTable.createNewProperty().set(false);
        updateGestionnaireIdsTable(session, elementProperty.get());
        return gestionnaireIdsTable;
        });
        ui_gestionnaireIds.setClosable(false);

        ui_observations.setContent(() -> {
        observationsTable = new PojoTableExternalAddable(Observation.class, elementProperty());
        observationsTable.editableProperty().bind(disableFieldsProperty().not());
        updateObservationsTable(session, elementProperty.get());
        return observationsTable;
        });
        ui_observations.setClosable(false);

        ui_photos.setContent(() -> {
        photosTable = new PojoTable(Photo.class, null, elementProperty());
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

    public FXOuvrageAssocieAmenagementHydrauliquePane(final OuvrageAssocieAmenagementHydraulique ouvrageAssocieAmenagementHydraulique){
        this();
        this.elementProperty().set(ouvrageAssocieAmenagementHydraulique);
    }

    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends OuvrageAssocieAmenagementHydraulique > observableElement, OuvrageAssocieAmenagementHydraulique oldElement, OuvrageAssocieAmenagementHydraulique newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de OuvrageAssocieAmenagementHydraulique
            ui_superficie.getValueFactory().valueProperty().unbindBidirectional(oldElement.superficieProperty());
            ui_superficie.getValueFactory().setValue(0);
            ui_hauteur.getValueFactory().valueProperty().unbindBidirectional(oldElement.hauteurProperty());
            ui_hauteur.getValueFactory().setValue(0);
            ui_profondeur.getValueFactory().valueProperty().unbindBidirectional(oldElement.profondeurProperty());
            ui_profondeur.getValueFactory().setValue(0);
            ui_nombre.getValueFactory().valueProperty().unbindBidirectional(oldElement.nombreProperty());
            ui_nombre.getValueFactory().setValue(0);
            ui_ouvrageDerversant.selectedProperty().unbindBidirectional(oldElement.ouvrageDerversantProperty());
            ui_ouvrageDerversant.setSelected(false);
            ui_numCouche.getValueFactory().valueProperty().unbindBidirectional(oldElement.numCoucheProperty());
            ui_numCouche.getValueFactory().setValue(0);
            ui_diametre.getValueFactory().valueProperty().unbindBidirectional(oldElement.diametreProperty());
            ui_diametre.getValueFactory().setValue(0);
            ui_cote.getValueFactory().valueProperty().unbindBidirectional(oldElement.coteProperty());
            ui_cote.getValueFactory().setValue(0);
            ui_section.getValueFactory().valueProperty().unbindBidirectional(oldElement.sectionProperty());
            ui_section.getValueFactory().setValue(0);
        // Propriétés de AvecGeometrie
        // Propriétés de AvecSettableGeometrie
        // Propriétés de DescriptionAmenagementHydraulique
        }

        final Session session = Injector.getBean(Session.class);

        if (newElement == null) {

                ui_typeOuvrage.setItems(null);
                ui_materiauId.setItems(null);
                ui_sourceId.setItems(null);
                ui_etat.setItems(null);
                ui_fonctionnement.setItems(null);
                ui_amenagementHydrauliqueId.setItems(null);
        } else {


        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de OuvrageAssocieAmenagementHydraulique
        // * superficie
        ui_superficie.getValueFactory().valueProperty().bindBidirectional(newElement.superficieProperty());
        // * hauteur
        ui_hauteur.getValueFactory().valueProperty().bindBidirectional(newElement.hauteurProperty());
        // * profondeur
        ui_profondeur.getValueFactory().valueProperty().bindBidirectional(newElement.profondeurProperty());
        // * nombre
        ui_nombre.getValueFactory().valueProperty().bindBidirectional(newElement.nombreProperty());
        // * ouvrageDerversant
        ui_ouvrageDerversant.selectedProperty().bindBidirectional(newElement.ouvrageDerversantProperty());
        // * numCouche
        ui_numCouche.getValueFactory().valueProperty().bindBidirectional(newElement.numCoucheProperty());
        // * diametre
        ui_diametre.getValueFactory().valueProperty().bindBidirectional(newElement.diametreProperty());
        // * cote
        ui_cote.getValueFactory().valueProperty().bindBidirectional(newElement.coteProperty());
        // * section
        ui_section.getValueFactory().valueProperty().bindBidirectional(newElement.sectionProperty());
            final AbstractSIRSRepository<RefOuvrageHydrauliqueAssocie> typeOuvrageRepo = session.getRepositoryForClass(RefOuvrageHydrauliqueAssocie.class);
            SIRS.initCombo(ui_typeOuvrage, SIRS.observableList(typeOuvrageRepo.getAll()), newElement.getTypeOuvrage() == null? null : typeOuvrageRepo.get(newElement.getTypeOuvrage()));
            final AbstractSIRSRepository<RefMateriau> materiauIdRepo = session.getRepositoryForClass(RefMateriau.class);
            SIRS.initCombo(ui_materiauId, SIRS.observableList(materiauIdRepo.getAll()), newElement.getMateriauId() == null? null : materiauIdRepo.get(newElement.getMateriauId()));
            final AbstractSIRSRepository<RefSource> sourceIdRepo = session.getRepositoryForClass(RefSource.class);
            SIRS.initCombo(ui_sourceId, SIRS.observableList(sourceIdRepo.getAll()), newElement.getSourceId() == null? null : sourceIdRepo.get(newElement.getSourceId()));
            final AbstractSIRSRepository<RefEtat> etatRepo = session.getRepositoryForClass(RefEtat.class);
            SIRS.initCombo(ui_etat, SIRS.observableList(etatRepo.getAll()), newElement.getEtat() == null? null : etatRepo.get(newElement.getEtat()));
            final AbstractSIRSRepository<RefFonctionnementOAAH> fonctionnementRepo = session.getRepositoryForClass(RefFonctionnementOAAH.class);
            SIRS.initCombo(ui_fonctionnement, SIRS.observableList(fonctionnementRepo.getAll()), newElement.getFonctionnement() == null? null : fonctionnementRepo.get(newElement.getFonctionnement()));
        // Propriétés de AvecGeometrie
        // Propriétés de AvecSettableGeometrie
        // Propriétés de DescriptionAmenagementHydraulique
        {
            final Preview linearPreview = newElement.getAmenagementHydrauliqueId() == null ? null : previewRepository.get(newElement.getAmenagementHydrauliqueId());
            SIRS.initCombo(ui_amenagementHydrauliqueId, SIRS.observableList(
                previewRepository.getByClass(linearPreview == null ? AmenagementHydraulique.class : linearPreview.getJavaClassOr(AmenagementHydraulique.class))).sorted(), linearPreview);
        }
        }

        updateAmenagementHydrauliqueAssocieIdsTable(session, newElement);
        updateDesordreDependanceAssocieIdsTable(session, newElement);
        updateProprietaireIdsTable(session, newElement);
        updateGestionnaireIdsTable(session, newElement);
        updateObservationsTable(session, newElement);
        updatePhotosTable(session, newElement);
    }


    protected void updateAmenagementHydrauliqueAssocieIdsTable(final Session session, final OuvrageAssocieAmenagementHydraulique newElement) {
            if (amenagementHydrauliqueAssocieIdsTable == null)
                return;

            if (newElement == null) {
                amenagementHydrauliqueAssocieIdsTable.setTableItems(null);
            } else {
        amenagementHydrauliqueAssocieIdsTable.setParentElement(null);
        final AbstractSIRSRepository<AmenagementHydraulique> amenagementHydrauliqueAssocieIdsRepo = session.getRepositoryForClass(AmenagementHydraulique.class);
        amenagementHydrauliqueAssocieIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getAmenagementHydrauliqueAssocieIds(), amenagementHydrauliqueAssocieIdsRepo));
            amenagementHydrauliqueAssocieIdsTable.setObservableListToListen(newElement.getAmenagementHydrauliqueAssocieIds());
        }
    }


    protected void updateDesordreDependanceAssocieIdsTable(final Session session, final OuvrageAssocieAmenagementHydraulique newElement) {
            if (desordreDependanceAssocieIdsTable == null)
                return;

            if (newElement == null) {
                desordreDependanceAssocieIdsTable.setTableItems(null);
            } else {
        desordreDependanceAssocieIdsTable.setParentElement(null);
        final AbstractSIRSRepository<DesordreDependance> desordreDependanceAssocieIdsRepo = session.getRepositoryForClass(DesordreDependance.class);
        desordreDependanceAssocieIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getDesordreDependanceAssocieIds(), desordreDependanceAssocieIdsRepo));
            desordreDependanceAssocieIdsTable.setObservableListToListen(newElement.getDesordreDependanceAssocieIds());
        }
    }


    protected void updateProprietaireIdsTable(final Session session, final OuvrageAssocieAmenagementHydraulique newElement) {
            if (proprietaireIdsTable == null)
                return;

            if (newElement == null) {
                proprietaireIdsTable.setTableItems(null);
            } else {
        proprietaireIdsTable.setParentElement(null);
        final AbstractSIRSRepository<Contact> proprietaireIdsRepo = session.getRepositoryForClass(Contact.class);
        proprietaireIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getProprietaireIds(), proprietaireIdsRepo));
            proprietaireIdsTable.setObservableListToListen(newElement.getProprietaireIds());
        }
    }


    protected void updateGestionnaireIdsTable(final Session session, final OuvrageAssocieAmenagementHydraulique newElement) {
            if (gestionnaireIdsTable == null)
                return;

            if (newElement == null) {
                gestionnaireIdsTable.setTableItems(null);
            } else {
        gestionnaireIdsTable.setParentElement(null);
        final AbstractSIRSRepository<Organisme> gestionnaireIdsRepo = session.getRepositoryForClass(Organisme.class);
        gestionnaireIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getGestionnaireIds(), gestionnaireIdsRepo));
            gestionnaireIdsTable.setObservableListToListen(newElement.getGestionnaireIds());
        }
    }


    protected void updateObservationsTable(final Session session, final OuvrageAssocieAmenagementHydraulique newElement) {
            if (observationsTable == null)
                return;

            if (newElement == null) {
                observationsTable.setTableItems(null);
            } else {
        observationsTable.setParentElement(newElement);
        observationsTable.setTableItems(()-> (ObservableList) newElement.getObservations());
        }
    }


    protected void updatePhotosTable(final Session session, final OuvrageAssocieAmenagementHydraulique newElement) {
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
        final OuvrageAssocieAmenagementHydraulique element = (OuvrageAssocieAmenagementHydraulique) elementProperty().get();




        Object cbValue;
        cbValue = ui_typeOuvrage.getValue();
        if (cbValue instanceof Preview) {
            element.setTypeOuvrage(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypeOuvrage(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypeOuvrage(null);
        }
        cbValue = ui_materiauId.getValue();
        if (cbValue instanceof Preview) {
            element.setMateriauId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setMateriauId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setMateriauId(null);
        }
        cbValue = ui_sourceId.getValue();
        if (cbValue instanceof Preview) {
            element.setSourceId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setSourceId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setSourceId(null);
        }
        cbValue = ui_etat.getValue();
        if (cbValue instanceof Preview) {
            element.setEtat(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setEtat(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setEtat(null);
        }
        cbValue = ui_fonctionnement.getValue();
        if (cbValue instanceof Preview) {
            element.setFonctionnement(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setFonctionnement(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setFonctionnement(null);
        }
        if (amenagementHydrauliqueAssocieIdsTable != null) {
        // Manage opposite references for AmenagementHydraulique...
        final List<String> currentAmenagementHydrauliqueIdsList = new ArrayList<>();
        for(final Element elt : amenagementHydrauliqueAssocieIdsTable.getAllValues()){
            final AmenagementHydraulique amenagementHydraulique = (AmenagementHydraulique) elt;
            currentAmenagementHydrauliqueIdsList.add(amenagementHydraulique.getId());
        }
        element.setAmenagementHydrauliqueAssocieIds(currentAmenagementHydrauliqueIdsList);

        }
        if (desordreDependanceAssocieIdsTable != null) {
        /*
        * En cas de reference opposee on se prepare a stocker les objets
        * "opposes" pour les mettre � jour.
        */
        final List<DesordreDependance> currentDesordreDependanceList = new ArrayList<>();
        // Manage opposite references for DesordreDependance...
        /*
        * Si on est sur une reference principale, on a besoin du depot pour
        * supprimer reellement les elements que l'on va retirer du tableau.
        * Si on a une reference opposee, on a besoin du depot pour mettre a jour
        * les objets qui referencent l'objet courant en sens contraire.
        */
        final AbstractSIRSRepository<DesordreDependance> desordreDependanceRepository = session.getRepositoryForClass(DesordreDependance.class);
        final List<String> currentDesordreDependanceIdsList = new ArrayList<>();
        for(final Element elt : desordreDependanceAssocieIdsTable.getAllValues()){
            final DesordreDependance desordreDependance = (DesordreDependance) elt;
            currentDesordreDependanceIdsList.add(desordreDependance.getId());
            currentDesordreDependanceList.add(desordreDependance);

            // Addition
            if(!desordreDependance.getOuvrageAssocieIds().contains(element.getId())){
                desordreDependance.getOuvrageAssocieIds().add(element.getId());
            }
        }
        desordreDependanceRepository.executeBulk(currentDesordreDependanceList);
        element.setDesordreDependanceAssocieIds(currentDesordreDependanceIdsList);

        // Deletion
        final StreamingIterable<DesordreDependance> listDesordreDependance = desordreDependanceRepository.getAllStreaming();
        try (final CloseableIterator<DesordreDependance> it = listDesordreDependance.iterator()) {
            while (it.hasNext()) {
                final DesordreDependance i = it.next();
                if(i.getOuvrageAssocieIds().contains(element.getId())
                    || element.getDesordreDependanceAssocieIds().contains(i.getId())){
                    if(!desordreDependanceAssocieIdsTable.getAllValues().contains(i)){
                        element.getDesordreDependanceAssocieIds().remove(i.getId()); //Normalement inutile du fait du  clear avant les op�rations d'ajout
                        i.getOuvrageAssocieIds().remove(element.getId());
                        desordreDependanceRepository.update(i);
                    }
                }
            }
        }
        }
        if (proprietaireIdsTable != null) {
        // Manage opposite references for Contact...
        final List<String> currentContactIdsList = new ArrayList<>();
        for(final Element elt : proprietaireIdsTable.getAllValues()){
            final Contact contact = (Contact) elt;
            currentContactIdsList.add(contact.getId());
        }
        element.setProprietaireIds(currentContactIdsList);

        }
        if (gestionnaireIdsTable != null) {
        // Manage opposite references for Organisme...
        final List<String> currentOrganismeIdsList = new ArrayList<>();
        for(final Element elt : gestionnaireIdsTable.getAllValues()){
            final Organisme organisme = (Organisme) elt;
            currentOrganismeIdsList.add(organisme.getId());
        }
        element.setGestionnaireIds(currentOrganismeIdsList);

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
