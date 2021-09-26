
package fr.sirs.theme.ui;

import fr.sirs.theme.ui.*;
import fr.sirs.theme.ui.pojotable.PojoTableExternalAddable;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.javafx.FloatSpinnerValueFactory;
import fr.sirs.util.FXFreeTab;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class FXAmenagementHydrauliquePane extends AbstractFXElementPane<AmenagementHydraulique> {

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;

    @FXML private FXValidityPeriodPane uiValidityPeriod;

    // Propriétés de AmenagementHydraulique
    @FXML protected Spinner ui_superficie;
    @FXML protected Spinner ui_capaciteStockage;
    @FXML protected TextField ui_collectiviteCompetence;
    @FXML protected Spinner ui_profondeurMoyenne;
    @FXML protected ComboBox ui_fonctionnement;
    @FXML protected Button ui_fonctionnement_link;
    @FXML protected ComboBox ui_type;
    @FXML protected Button ui_type_link;
    @FXML protected FXFreeTab ui_desordreIds;
    protected ListeningPojoTable desordreIdsTable;
    @FXML protected FXFreeTab ui_structureIds;
    protected ListeningPojoTable structureIdsTable;
    @FXML protected FXFreeTab ui_ouvrageAssocieIds;
    protected ListeningPojoTable ouvrageAssocieIdsTable;
    @FXML protected FXFreeTab ui_gestionnaireIds;
    protected ListeningPojoTable gestionnaireIdsTable;
    @FXML protected FXFreeTab ui_tronconIds;
    protected ListeningPojoTable tronconIdsTable;
    @FXML protected FXFreeTab ui_observationIds;
    protected PojoTable observationIdsTable;
    @FXML protected FXFreeTab ui_prestationIds;
    protected ListeningPojoTable prestationIdsTable;
    @FXML protected FXFreeTab ui_proprietaireIds;
    protected ListeningPojoTable proprietaireIdsTable;
    @FXML protected FXFreeTab ui_traits;
    protected PojoTable traitsTable;

    // Propriétés de AotCotAssociable

    // Propriétés de AvecGeometrie

    // Propriétés de AvecSettableGeometrie

    // Propriétés de AbstractDependance
    @FXML protected TextArea ui_commentaire;
    @FXML protected TextField ui_libelle;
    // We don't want to use these attibutes
//    @FXML protected FXFreeTab ui_proprietes;
//    protected PojoTable proprietesTable;
//    @FXML protected FXFreeTab ui_gestions;
//    protected PojoTable gestionsTable;
    @FXML protected FXFreeTab ui_photos;
    protected PojoTable photosTable;

    @FXML FXPositionDependancePane uiPosition;

    /**
     * Constructor. Initialize part of the UI which will not require update when
     * element edited change.
     */
    protected FXAmenagementHydrauliquePane() {
        SIRS.loadFXML(this, AmenagementHydraulique.class);
        final Session session = Injector.getBean(Session.class);
        previewRepository = session.getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

        /*
        * Disabling rules.
        */
        ui_superficie.disableProperty().bind(disableFieldsProperty());
        ui_superficie.setEditable(true);
        ui_superficie.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_capaciteStockage.disableProperty().bind(disableFieldsProperty());
        ui_capaciteStockage.setEditable(true);
        ui_capaciteStockage.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_collectiviteCompetence.disableProperty().bind(disableFieldsProperty());
        ui_profondeurMoyenne.disableProperty().bind(disableFieldsProperty());
        ui_profondeurMoyenne.setEditable(true);
        ui_profondeurMoyenne.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_fonctionnement.disableProperty().bind(disableFieldsProperty());
        ui_fonctionnement_link.setVisible(false);
        ui_type.disableProperty().bind(disableFieldsProperty());
        ui_type_link.setVisible(false);
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

        ui_structureIds.setContent(() -> {
            structureIdsTable = new ListeningPojoTable(StructureAmenagementHydraulique.class, null, elementProperty());
            structureIdsTable.editableProperty().bind(disableFieldsProperty().not());
            structureIdsTable.createNewProperty().set(false);
            updateStructureIdsTable(session, elementProperty.get());
            return structureIdsTable;
        });
        ui_structureIds.setClosable(false);

        ui_ouvrageAssocieIds.setContent(() -> {
            ouvrageAssocieIdsTable = new ListeningPojoTable(OuvrageAssocieAmenagementHydraulique.class, null, elementProperty());
            ouvrageAssocieIdsTable.editableProperty().bind(disableFieldsProperty().not());
            ouvrageAssocieIdsTable.createNewProperty().set(false);
            updateOuvrageAssocieIdsTable(session, elementProperty.get());
            return ouvrageAssocieIdsTable;
        });
        ui_ouvrageAssocieIds.setClosable(false);

        ui_gestionnaireIds.setContent(() -> {
            gestionnaireIdsTable = new ListeningPojoTable(Organisme.class, null, elementProperty());
            gestionnaireIdsTable.editableProperty().bind(disableFieldsProperty().not());
            gestionnaireIdsTable.createNewProperty().set(false);
            updateGestionnaireIdsTable(session, elementProperty.get());
            return gestionnaireIdsTable;
        });
        ui_gestionnaireIds.setClosable(false);

        ui_tronconIds.setContent(() -> {
            tronconIdsTable = new ListeningPojoTable(TronconDigue.class, null, elementProperty());
            tronconIdsTable.editableProperty().bind(disableFieldsProperty().not());
            tronconIdsTable.fichableProperty().set(false);
            tronconIdsTable.createNewProperty().set(false);
            updateTronconIdsTable(session, elementProperty.get());
            return tronconIdsTable;
        });
        ui_tronconIds.setClosable(false);

        ui_observationIds.setContent(() -> {
            observationIdsTable = new PojoTableExternalAddable(Observation.class, elementProperty());
            observationIdsTable.editableProperty().bind(disableFieldsProperty().not());
            updateObservationIdsTable(session, elementProperty.get());
            return observationIdsTable;
        });
        ui_observationIds.setClosable(false);
        
        ui_prestationIds.setContent(() -> {
            prestationIdsTable = new ListeningPojoTable(Prestation.class, null, elementProperty());
            prestationIdsTable.editableProperty().bind(disableFieldsProperty().not());
            prestationIdsTable.createNewProperty().set(false);
            updatePrestationIdsTable(session, elementProperty.get());
            return prestationIdsTable;
        });
        ui_prestationIds.setClosable(false);

        ui_proprietaireIds.setContent(() -> {
            proprietaireIdsTable = new ListeningPojoTable(Contact.class, null, elementProperty());
            proprietaireIdsTable.editableProperty().bind(disableFieldsProperty().not());
            proprietaireIdsTable.createNewProperty().set(false);
            updateProprietaireIdsTable(session, elementProperty.get());
            return proprietaireIdsTable;
        });
        ui_proprietaireIds.setClosable(false);
        ui_commentaire.setWrapText(true);
        ui_commentaire.editableProperty().bind(disableFieldsProperty().not());
        ui_libelle.disableProperty().bind(disableFieldsProperty());

//        ui_proprietes.setContent(() -> {
//            proprietesTable = new PojoTable(ProprieteObjet.class, null, elementProperty());
//            proprietesTable.editableProperty().bind(disableFieldsProperty().not());
//            updateProprietesTable(session, elementProperty.get());
//            return proprietesTable;
//        });
//        ui_proprietes.setClosable(false);

//        ui_gestions.setContent(() -> {
//            gestionsTable = new PojoTable(GestionObjet.class, null, elementProperty());
//            gestionsTable.editableProperty().bind(disableFieldsProperty().not());
//            updateGestionsTable(session, elementProperty.get());
//            return gestionsTable;
//        });
//        ui_gestions.setClosable(false);

        ui_photos.setContent(() -> {
            photosTable = new PojoTable(PhotoDependance.class, null, elementProperty());
            photosTable.editableProperty().bind(disableFieldsProperty().not());
            updatePhotosTable(session, elementProperty.get());
            return photosTable;
        });
        ui_photos.setClosable(false);

        ui_traits.setContent(() -> {
            traitsTable = new TraitTable(elementProperty());
            traitsTable.editableProperty().bind(disableFieldsProperty().not());
            updateTraitIdsTable(session, elementProperty.get());
            return traitsTable;
        });
        ui_traits.setClosable(false);
    }

    public FXAmenagementHydrauliquePane(final AmenagementHydraulique amenagementHydraulique){
        this();
        this.elementProperty().set(amenagementHydraulique);
    }

    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends AmenagementHydraulique > observableElement, AmenagementHydraulique oldElement, AmenagementHydraulique newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
            // Propriétés de AmenagementHydraulique
            ui_superficie.getValueFactory().valueProperty().unbindBidirectional(oldElement.superficieProperty());
            ui_superficie.getValueFactory().setValue(0);
            ui_capaciteStockage.getValueFactory().valueProperty().unbindBidirectional(oldElement.capaciteStockageProperty());
            ui_capaciteStockage.getValueFactory().setValue(0);
            ui_collectiviteCompetence.textProperty().unbindBidirectional(oldElement.collectiviteCompetenceProperty());
            ui_collectiviteCompetence.setText(null);
            ui_profondeurMoyenne.getValueFactory().valueProperty().unbindBidirectional(oldElement.profondeurMoyenneProperty());
            ui_profondeurMoyenne.getValueFactory().setValue(0);
            // Propriétés de AotCotAssociable
            // Propriétés de AvecGeometrie
            // Propriétés de AvecSettableGeometrie
            // Propriétés de AbstractDependance
            ui_commentaire.textProperty().unbindBidirectional(oldElement.commentaireProperty());
            ui_commentaire.setText(null);
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
            ui_libelle.setText(null);
        }

        final Session session = Injector.getBean(Session.class);
        
        if (newElement == null) {

            ui_fonctionnement.setItems(null);
            ui_type.setItems(null);
        } else {

            /*
            * Bind control properties to Element ones.
            */
            // Propriétés de AmenagementHydraulique
            // * superficie
            ui_superficie.getValueFactory().valueProperty().bindBidirectional(newElement.superficieProperty());
            // * capaciteStockage
            ui_capaciteStockage.getValueFactory().valueProperty().bindBidirectional(newElement.capaciteStockageProperty());
            // * collectiviteCompetence
            ui_collectiviteCompetence.textProperty().bindBidirectional(newElement.collectiviteCompetenceProperty());
            // * profondeurMoyenne
            ui_profondeurMoyenne.getValueFactory().valueProperty().bindBidirectional(newElement.profondeurMoyenneProperty());
            final AbstractSIRSRepository<RefFonctionnementAH> fonctionnementRepo = session.getRepositoryForClass(RefFonctionnementAH.class);
            SIRS.initCombo(ui_fonctionnement, SIRS.observableList(fonctionnementRepo.getAll()), newElement.getFonctionnement() == null? null : fonctionnementRepo.get(newElement.getFonctionnement()));
            final AbstractSIRSRepository<RefTypeAmenagementHydraulique> typeRepo = session.getRepositoryForClass(RefTypeAmenagementHydraulique.class);
            SIRS.initCombo(ui_type, SIRS.observableList(typeRepo.getAll()), newElement.getType() == null? null : typeRepo.get(newElement.getType()));
            // Propriétés de AotCotAssociable
            // Propriétés de AvecGeometrie
            // Propriétés de AvecSettableGeometrie
            // Propriétés de AbstractDependance
            // * commentaire
            ui_commentaire.textProperty().bindBidirectional(newElement.commentaireProperty());
            // * libelle
            ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
        }

        updateDesordreIdsTable(session, newElement);
        updateStructureIdsTable(session, newElement);
        updateOuvrageAssocieIdsTable(session, newElement);
        updateGestionnaireIdsTable(session, newElement);
        updateTronconIdsTable(session, newElement);
        updateObservationIdsTable(session, newElement);
        updatePrestationIdsTable(session, newElement);
        updateProprietaireIdsTable(session, newElement);
//        updateProprietesTable(session, newElement);
//        updateGestionsTable(session, newElement);
        updatePhotosTable(session, newElement);
        updateTraitIdsTable(session, newElement);
    }

    protected void updateDesordreIdsTable(final Session session, final AmenagementHydraulique newElement) {
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

    protected void updateStructureIdsTable(final Session session, final AmenagementHydraulique newElement) {
        if (structureIdsTable == null)
            return;

        if (newElement == null) {
            structureIdsTable.setTableItems(null);
        } else {
            structureIdsTable.setParentElement(null);
            final AbstractSIRSRepository<StructureAmenagementHydraulique> structureIdsRepo = session.getRepositoryForClass(StructureAmenagementHydraulique.class);
            structureIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getStructureIds(), structureIdsRepo));
            structureIdsTable.setObservableListToListen(newElement.getStructureIds());
        }
    }

    protected void updateOuvrageAssocieIdsTable(final Session session, final AmenagementHydraulique newElement) {
        if (ouvrageAssocieIdsTable == null)
            return;

        if (newElement == null) {
            ouvrageAssocieIdsTable.setTableItems(null);
        } else {
            ouvrageAssocieIdsTable.setParentElement(null);
            final AbstractSIRSRepository<OuvrageAssocieAmenagementHydraulique> ouvrageAssocieIdsRepo = session.getRepositoryForClass(OuvrageAssocieAmenagementHydraulique.class);
            ouvrageAssocieIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOuvrageAssocieIds(), ouvrageAssocieIdsRepo));
            ouvrageAssocieIdsTable.setObservableListToListen(newElement.getOuvrageAssocieIds());
        }
    }

    protected void updateGestionnaireIdsTable(final Session session, final AmenagementHydraulique newElement) {
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

    protected void updateTronconIdsTable(final Session session, final AmenagementHydraulique newElement) {
        if (tronconIdsTable == null)
            return;

        if (newElement == null) {
            tronconIdsTable.setTableItems(null);
        } else {
            tronconIdsTable.setParentElement(null);
            final AbstractSIRSRepository<TronconDigue> tronconIdsRepo = session.getRepositoryForClass(TronconDigue.class);
            tronconIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getTronconIds(), tronconIdsRepo));
            tronconIdsTable.setObservableListToListen(newElement.getTronconIds());
        }
    }

    protected void updateObservationIdsTable(final Session session, final AmenagementHydraulique newElement) {
        if (observationIdsTable == null)
            return;

        if (newElement == null) {
            observationIdsTable.setTableItems(null);
        } else {
            observationIdsTable.setParentElement(newElement);
            observationIdsTable.setTableItems(()-> (ObservableList) newElement.getObservationIds());
        }
    }

    protected void updatePrestationIdsTable(final Session session, final AmenagementHydraulique newElement) {
        if (prestationIdsTable == null)
            return;

        if (newElement == null) {
            prestationIdsTable.setTableItems(null);
        } else {
            prestationIdsTable.setParentElement(null);
            final AbstractSIRSRepository<Prestation> prestationIdsRepo = session.getRepositoryForClass(Prestation.class);
            prestationIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getPrestationIds(), prestationIdsRepo));
            prestationIdsTable.setObservableListToListen(newElement.getPrestationIds());
        }
    }

    protected void updateTraitIdsTable(final Session session, final AmenagementHydraulique newElement) {
        if (traitsTable == null)
            return;

        if (newElement == null) {
            traitsTable.setTableItems(null);
        } else {
            traitsTable.setParentElement(null);
            final AbstractSIRSRepository<TraitAmenagementHydraulique> traitRepo = session.getRepositoryForClass(TraitAmenagementHydraulique.class);
            traitsTable.setTableItems(()-> SIRS.toElementList(newElement.getTraitIds(), traitRepo));
        }
    }

    protected void updateProprietaireIdsTable(final Session session, final AmenagementHydraulique newElement) {
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

//    protected void updateProprietesTable(final Session session, final AmenagementHydraulique newElement) {
//        if (proprietesTable == null)
//            return;
//        
//        if (newElement == null) {
//            proprietesTable.setTableItems(null);
//        } else {
//            proprietesTable.setParentElement(newElement);
//            proprietesTable.setTableItems(()-> (ObservableList) newElement.getProprietes());
//        }
//    }

//    protected void updateGestionsTable(final Session session, final AmenagementHydraulique newElement) {
//        if (gestionsTable == null)
//            return;
//
//        if (newElement == null) {
//            gestionsTable.setTableItems(null);
//        } else {
//            gestionsTable.setParentElement(newElement);
//            gestionsTable.setTableItems(()-> (ObservableList) newElement.getGestions());
//        }
//    }

    protected void updatePhotosTable(final Session session, final AmenagementHydraulique newElement) {
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
        final AmenagementHydraulique element = (AmenagementHydraulique) elementProperty().get();

        element.setCommentaire(ui_commentaire.getText());

        Object cbValue;
        cbValue = ui_fonctionnement.getValue();
        if (cbValue instanceof Preview) {
            element.setFonctionnement(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setFonctionnement(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setFonctionnement(null);
        }
        cbValue = ui_type.getValue();
        if (cbValue instanceof Preview) {
            element.setType(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setType(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setType(null);
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
        if (structureIdsTable != null) {
            // Manage opposite references for StructureAmenagementHydraulique...
            final List<String> currentStructureAmenagementHydrauliqueIdsList = new ArrayList<>();
            for(final Element elt : structureIdsTable.getAllValues()){
                final StructureAmenagementHydraulique structureAmenagementHydraulique = (StructureAmenagementHydraulique) elt;
                currentStructureAmenagementHydrauliqueIdsList.add(structureAmenagementHydraulique.getId());
            }
            element.setStructureIds(currentStructureAmenagementHydrauliqueIdsList);

        }
        if (ouvrageAssocieIdsTable != null) {
            // Manage opposite references for OuvrageAssocieAmenagementHydraulique...
            final List<String> currentOuvrageAssocieAmenagementHydrauliqueIdsList = new ArrayList<>();
            final List<OuvrageAssocieAmenagementHydraulique> oppositeOAAHs = new ArrayList<>();
            for(final Element elt : ouvrageAssocieIdsTable.getAllValues()){
                final OuvrageAssocieAmenagementHydraulique ouvrageAssocieAmenagementHydraulique = (OuvrageAssocieAmenagementHydraulique) elt;
                currentOuvrageAssocieAmenagementHydrauliqueIdsList.add(ouvrageAssocieAmenagementHydraulique.getId());
                
                if (!ouvrageAssocieAmenagementHydraulique.getAmenagementHydrauliqueAssocieIds().contains(element.getId())) {
                    ouvrageAssocieAmenagementHydraulique.getAmenagementHydrauliqueAssocieIds().add(element.getId());
                }
                oppositeOAAHs.add(ouvrageAssocieAmenagementHydraulique);
            }
            element.setOuvrageAssocieIds(currentOuvrageAssocieAmenagementHydrauliqueIdsList);
            Injector.getSession().getRepositoryForClass(OuvrageAssocieAmenagementHydraulique.class).executeBulk(oppositeOAAHs);

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
        if (tronconIdsTable != null) {
            /*
            * En cas de reference opposee on se prepare a stocker les objets
            * "opposes" pour les mettre � jour.
            */
            final List<TronconDigue> currentTronconDigueList = new ArrayList<>();
            /*
            * En cas d'écrasement de référence dans le troncon opposé, on
            * s'assure de détruire le lien coté AH.
            */
            final List<AmenagementHydraulique> currentOppositeAhList = new ArrayList<>();
            // Manage opposite references for TronconDigue...
            /*
            * Si on est sur une reference principale, on a besoin du depot pour
            * supprimer reellement les elements que l'on va retirer du tableau.
            * Si on a une reference opposee, on a besoin du depot pour mettre a jour
            * les objets qui referencent l'objet courant en sens contraire.
            */
            final AbstractSIRSRepository<TronconDigue> tronconDigueRepository = session.getRepositoryForClass(TronconDigue.class);
            final AbstractSIRSRepository<AmenagementHydraulique> ahRepository = Injector.getSession().getRepositoryForClass(AmenagementHydraulique.class);
            final List<String> currentTronconDigueIdsList = new ArrayList<>();
            for(final Element elt : tronconIdsTable.getAllValues()){
                final TronconDigue tronconDigue = (TronconDigue) elt;
                final String tronconId = tronconDigue.getId();
                final String previousAhId = tronconDigue.getAmenagementHydrauliqueId();

                currentTronconDigueIdsList.add(tronconId);
                tronconDigue.setAmenagementHydrauliqueId(element.getId());
                currentTronconDigueList.add(tronconDigue);

                //clean the troncon list of the opposite AHs
                if (previousAhId != null) {
                    AmenagementHydraulique ah = ahRepository.get(previousAhId);
                    ObservableList<String> tronconIds = ah.getTronconIds();
                    tronconIds.removeIf(s -> s.equals(tronconId));
                    ah.setTronconIds(tronconIds);
                    currentOppositeAhList.add(ah);
                }
            }
            tronconDigueRepository.executeBulk(currentTronconDigueList);
            ahRepository.executeBulk(currentOppositeAhList);

            // Manage opposite references for the tronconDigue. Remove the ah
            // reference when it's no more linked with
            List<String> toRemoveIds = element.getTronconIds().stream().filter(id -> !currentTronconDigueIdsList.contains(id)).collect(Collectors.toList());
            List<TronconDigue> toRemoveTroncons = tronconDigueRepository.get(toRemoveIds);
            toRemoveTroncons.stream().forEach(tr -> tr.setAmenagementHydrauliqueId(null));
            tronconDigueRepository.executeBulk(toRemoveTroncons);
            element.setTronconIds(currentTronconDigueIdsList);
        }
        if (prestationIdsTable != null) {
            // Manage opposite references for Prestation...
            final List<String> currentPrestationIdsList = new ArrayList<>();
            for(final Element elt : prestationIdsTable.getAllValues()){
                final Prestation prestation = (Prestation) elt;
                currentPrestationIdsList.add(prestation.getId());
            }
            element.setPrestationIds(currentPrestationIdsList);
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
    }

    private static final class TraitTable extends PojoTable{
        public TraitTable(final ObjectProperty<? extends Element> container) {
            super(TraitAmenagementHydraulique.class, null, container);
            createNewProperty.set(false);
            detaillableProperty.set(false);
            uiAdd.setVisible(false);
        }
    }
}
