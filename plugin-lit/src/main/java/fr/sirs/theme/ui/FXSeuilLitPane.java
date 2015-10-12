
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.DesordreLit;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.GestionObjet;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.PlanSeuilLit;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ProprieteObjet;
import fr.sirs.core.model.RefFonction;
import fr.sirs.core.model.RefFonctionSeuilLit;
import fr.sirs.core.model.RefGeometrieCreteSeuilLit;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefPositionAxeSeuilLit;
import fr.sirs.core.model.RefPositionStructureSeuilLit;
import fr.sirs.core.model.RefProfilCoursierSeuilLit;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefTypeInspectionSeuilLit;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.SeuilLit;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.util.StreamingIterable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import org.geotoolkit.util.collection.CloseableIterator;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXSeuilLitPane extends FXSeuilLitPaneStub {

    protected final Previews previewRepository;
    
    // Propriétés de Positionable
    @FXML private FXPositionablePane uiPositionable;
    @FXML private FXValidityPeriodPane uiValidityPeriod;

    protected final PojoTable plansTable;
    protected final ListeningPojoTable voieAccesIdsTable;
    protected final ListeningPojoTable ouvrageFranchissementIdsTable;
    protected final ListeningPojoTable voieDigueIdsTable;
    protected final ListeningPojoTable ouvrageVoirieIdsTable;
    protected final ListeningPojoTable stationPompageIdsTable;
    protected final ListeningPojoTable reseauHydrauliqueFermeIdsTable;
    protected final ListeningPojoTable reseauHydrauliqueCielOuvertIdsTable;
    protected final ListeningPojoTable ouvrageHydrauliqueAssocieIdsTable;
    protected final ListeningPojoTable ouvrageTelecomEnergieIdsTable;
    protected final ListeningPojoTable reseauTelecomEnergieIdsTable;
    protected final ListeningPojoTable ouvrageParticulierIdsTable;
    protected final ListeningPojoTable echelleLimnimetriqueIdsTable;
    protected final ListeningPojoTable desordreIdsTable;
    protected final ListeningPojoTable bergeIdsTable;
    protected final ListeningPojoTable digueIdsTable;

    protected final PojoTable photosTable;

    protected final PojoTable proprietesTable;
    protected final PojoTable gestionsTable;

    private Class bergeClass;

    /**
     * Constructor. Initialize part of the UI which will not require update when 
     * element edited change.
     */
    protected FXSeuilLitPane() {
        SIRS.loadFXML(this, SeuilLit.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        uiPositionable.disableFieldsProperty().bind(disableFieldsProperty());
        uiPositionable.positionableProperty().bind(elementProperty());
        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

        /*
         * Disabling rules.
         */
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_commune.disableProperty().bind(disableFieldsProperty());
        ui_anneeConstruction.disableProperty().bind(disableFieldsProperty());
        ui_anneeConstruction.setEditable(true);
        ui_anneeConstruction.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        ui_dateDerniereInspection.disableProperty().bind(disableFieldsProperty());
        ui_penteRampant.disableProperty().bind(disableFieldsProperty());
        ui_penteRampant.setEditable(true);
        ui_penteRampant.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_longueurTotale.disableProperty().bind(disableFieldsProperty());
        ui_longueurTotale.setEditable(true);
        ui_longueurTotale.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_longueurCoursier.disableProperty().bind(disableFieldsProperty());
        ui_longueurCoursier.setEditable(true);
        ui_longueurCoursier.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_largeurEnCrete.disableProperty().bind(disableFieldsProperty());
        ui_largeurEnCrete.setEditable(true);
        ui_largeurEnCrete.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_hauteurChute.disableProperty().bind(disableFieldsProperty());
        ui_hauteurChute.setEditable(true);
        ui_hauteurChute.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_numCouche.disableProperty().bind(disableFieldsProperty());
        ui_numCouche.setEditable(true);
        ui_numCouche.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        ui_epaisseur.disableProperty().bind(disableFieldsProperty());
        ui_epaisseur.setEditable(true);
        ui_epaisseur.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_passeSportEauVive.disableProperty().bind(disableFieldsProperty());
        ui_passePoisson.disableProperty().bind(disableFieldsProperty());
        ui_surfaceRempantEntretien.disableProperty().bind(disableFieldsProperty());
        ui_surfaceRempantEntretien.setEditable(true);
        ui_surfaceRempantEntretien.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_fonctionSeuilId.disableProperty().bind(disableFieldsProperty());
        ui_fonctionSeuilId_link.setVisible(false);
        ui_typeInspectionId.disableProperty().bind(disableFieldsProperty());
        ui_typeInspectionId_link.setVisible(false);
        ui_materiauPrincipalA.disableProperty().bind(disableFieldsProperty());
        ui_materiauPrincipalA_link.setVisible(false);
        ui_materiauPrincipalB.disableProperty().bind(disableFieldsProperty());
        ui_materiauPrincipalB_link.setVisible(false);
        ui_positionSeuilId.disableProperty().bind(disableFieldsProperty());
        ui_positionSeuilId_link.setVisible(false);
        ui_geometrieCreteId.disableProperty().bind(disableFieldsProperty());
        ui_geometrieCreteId_link.setVisible(false);
        ui_profilCoursierId.disableProperty().bind(disableFieldsProperty());
        ui_profilCoursierId_link.setVisible(false);
        ui_natureId.disableProperty().bind(disableFieldsProperty());
        ui_natureId_link.setVisible(false);
        ui_materiauId.disableProperty().bind(disableFieldsProperty());
        ui_materiauId_link.setVisible(false);
        ui_fonctionId.disableProperty().bind(disableFieldsProperty());
        ui_fonctionId_link.setVisible(false);
        ui_sourceId.disableProperty().bind(disableFieldsProperty());
        ui_sourceId_link.setVisible(false);
        ui_positionStructureId.disableProperty().bind(disableFieldsProperty());
        ui_positionStructureId_link.setVisible(false);
        plansTable = new PojoTable(PlanSeuilLit.class, null);
        plansTable.editableProperty().bind(disableFieldsProperty().not());
        ui_plans.setContent(plansTable);
        ui_plans.setClosable(false);
        voieAccesIdsTable = new ListeningPojoTable(VoieAcces.class, null);
        voieAccesIdsTable.editableProperty().bind(disableFieldsProperty().not());
        voieAccesIdsTable.createNewProperty().set(false);
        ui_voieAccesIds.setContent(voieAccesIdsTable);
        ui_voieAccesIds.setClosable(false);
        ouvrageFranchissementIdsTable = new ListeningPojoTable(OuvrageFranchissement.class, null);
        ouvrageFranchissementIdsTable.editableProperty().bind(disableFieldsProperty().not());
        ouvrageFranchissementIdsTable.createNewProperty().set(false);
        ui_ouvrageFranchissementIds.setContent(ouvrageFranchissementIdsTable);
        ui_ouvrageFranchissementIds.setClosable(false);
        voieDigueIdsTable = new ListeningPojoTable(VoieDigue.class, null);
        voieDigueIdsTable.editableProperty().bind(disableFieldsProperty().not());
        voieDigueIdsTable.createNewProperty().set(false);
        ui_voieDigueIds.setContent(voieDigueIdsTable);
        ui_voieDigueIds.setClosable(false);
        ouvrageVoirieIdsTable = new ListeningPojoTable(OuvrageVoirie.class, null);
        ouvrageVoirieIdsTable.editableProperty().bind(disableFieldsProperty().not());
        ouvrageVoirieIdsTable.createNewProperty().set(false);
        ui_ouvrageVoirieIds.setContent(ouvrageVoirieIdsTable);
        ui_ouvrageVoirieIds.setClosable(false);
        stationPompageIdsTable = new ListeningPojoTable(StationPompage.class, null);
        stationPompageIdsTable.editableProperty().bind(disableFieldsProperty().not());
        stationPompageIdsTable.createNewProperty().set(false);
        ui_stationPompageIds.setContent(stationPompageIdsTable);
        ui_stationPompageIds.setClosable(false);
        reseauHydrauliqueFermeIdsTable = new ListeningPojoTable(ReseauHydrauliqueFerme.class, null);
        reseauHydrauliqueFermeIdsTable.editableProperty().bind(disableFieldsProperty().not());
        reseauHydrauliqueFermeIdsTable.createNewProperty().set(false);
        ui_reseauHydrauliqueFermeIds.setContent(reseauHydrauliqueFermeIdsTable);
        ui_reseauHydrauliqueFermeIds.setClosable(false);
        reseauHydrauliqueCielOuvertIdsTable = new ListeningPojoTable(ReseauHydrauliqueCielOuvert.class, null);
        reseauHydrauliqueCielOuvertIdsTable.editableProperty().bind(disableFieldsProperty().not());
        reseauHydrauliqueCielOuvertIdsTable.createNewProperty().set(false);
        ui_reseauHydrauliqueCielOuvertIds.setContent(reseauHydrauliqueCielOuvertIdsTable);
        ui_reseauHydrauliqueCielOuvertIds.setClosable(false);
        ouvrageHydrauliqueAssocieIdsTable = new ListeningPojoTable(OuvrageHydrauliqueAssocie.class, null);
        ouvrageHydrauliqueAssocieIdsTable.editableProperty().bind(disableFieldsProperty().not());
        ouvrageHydrauliqueAssocieIdsTable.createNewProperty().set(false);
        ui_ouvrageHydrauliqueAssocieIds.setContent(ouvrageHydrauliqueAssocieIdsTable);
        ui_ouvrageHydrauliqueAssocieIds.setClosable(false);
        ouvrageTelecomEnergieIdsTable = new ListeningPojoTable(OuvrageTelecomEnergie.class, null);
        ouvrageTelecomEnergieIdsTable.editableProperty().bind(disableFieldsProperty().not());
        ouvrageTelecomEnergieIdsTable.createNewProperty().set(false);
        ui_ouvrageTelecomEnergieIds.setContent(ouvrageTelecomEnergieIdsTable);
        ui_ouvrageTelecomEnergieIds.setClosable(false);
        reseauTelecomEnergieIdsTable = new ListeningPojoTable(ReseauTelecomEnergie.class, null);
        reseauTelecomEnergieIdsTable.editableProperty().bind(disableFieldsProperty().not());
        reseauTelecomEnergieIdsTable.createNewProperty().set(false);
        ui_reseauTelecomEnergieIds.setContent(reseauTelecomEnergieIdsTable);
        ui_reseauTelecomEnergieIds.setClosable(false);
        ouvrageParticulierIdsTable = new ListeningPojoTable(OuvrageParticulier.class, null);
        ouvrageParticulierIdsTable.editableProperty().bind(disableFieldsProperty().not());
        ouvrageParticulierIdsTable.createNewProperty().set(false);
        ui_ouvrageParticulierIds.setContent(ouvrageParticulierIdsTable);
        ui_ouvrageParticulierIds.setClosable(false);
        echelleLimnimetriqueIdsTable = new ListeningPojoTable(EchelleLimnimetrique.class, null);
        echelleLimnimetriqueIdsTable.editableProperty().bind(disableFieldsProperty().not());
        echelleLimnimetriqueIdsTable.createNewProperty().set(false);
        ui_echelleLimnimetriqueIds.setContent(echelleLimnimetriqueIdsTable);
        ui_echelleLimnimetriqueIds.setClosable(false);
        desordreIdsTable = new ListeningPojoTable(DesordreLit.class, null);
        desordreIdsTable.editableProperty().bind(disableFieldsProperty().not());
        desordreIdsTable.createNewProperty().set(false);
        ui_desordreIds.setContent(desordreIdsTable);
        ui_desordreIds.setClosable(false);
        digueIdsTable = new ListeningPojoTable(Digue.class, null);
        digueIdsTable.editableProperty().bind(disableFieldsProperty().not());
        digueIdsTable.createNewProperty().set(false);
        ui_digueIds.setContent(digueIdsTable);
        ui_digueIds.setClosable(false);
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_linearId.disableProperty().bind(disableFieldsProperty());
        ui_linearId_link.disableProperty().bind(ui_linearId.getSelectionModel().selectedItemProperty().isNull());
        ui_linearId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_linearId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_linearId.getSelectionModel().getSelectedItem()));       
        photosTable = new PojoTable(Photo.class, null);
        photosTable.editableProperty().bind(disableFieldsProperty().not());
        ui_photos.setContent(photosTable);
        ui_photos.setClosable(false);
        proprietesTable = new PojoTable(ProprieteObjet.class, null);
        proprietesTable.editableProperty().bind(disableFieldsProperty().not());
        ui_proprietes.setContent(proprietesTable);
        ui_proprietes.setClosable(false);
        gestionsTable = new PojoTable(GestionObjet.class, null);
        gestionsTable.editableProperty().bind(disableFieldsProperty().not());
        ui_gestions.setContent(gestionsTable);
        ui_gestions.setClosable(false);

        try {
            bergeClass = Class.forName("fr.sirs.core.model.Berge");
        } catch (ClassNotFoundException ex) {
            SIRS.LOGGER.log(Level.WARNING, "Le module berge semble n'être pas chargé.", ex);
            bergeClass = null;
        }

        if(bergeClass!=null){
            bergeIdsTable = new ListeningPojoTable(bergeClass, null);
            bergeIdsTable.editableProperty().bind(disableFieldsProperty().not());
            bergeIdsTable.createNewProperty().set(false);
            ui_bergeIds.setContent(bergeIdsTable);
            ui_bergeIds.setClosable(false);
        }
        else{
            bergeIdsTable = null;
            ui_bergeIds.getTabPane().getTabs().remove(ui_bergeIds);
        }
    }
    
    public FXSeuilLitPane(final SeuilLit seuilLit){
        this();
        this.elementProperty().set(seuilLit);  
    }     

    /**
     * Initialize fields at element setting.
     * @param observableElement
     * @param oldElement
     * @param newElement
     */
    @Override
    protected void initFields(ObservableValue<? extends SeuilLit > observableElement, SeuilLit oldElement, SeuilLit newElement) {   // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de SeuilLit
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
            ui_commune.textProperty().unbindBidirectional(oldElement.communeProperty());
            ui_anneeConstruction.getValueFactory().valueProperty().unbindBidirectional(oldElement.anneeConstructionProperty());
            ui_dateDerniereInspection.valueProperty().unbindBidirectional(oldElement.dateDerniereInspectionProperty());
            ui_penteRampant.getValueFactory().valueProperty().unbindBidirectional(oldElement.penteRampantProperty());
            ui_longueurTotale.getValueFactory().valueProperty().unbindBidirectional(oldElement.longueurTotaleProperty());
            ui_longueurCoursier.getValueFactory().valueProperty().unbindBidirectional(oldElement.longueurCoursierProperty());
            ui_largeurEnCrete.getValueFactory().valueProperty().unbindBidirectional(oldElement.largeurEnCreteProperty());
            ui_hauteurChute.getValueFactory().valueProperty().unbindBidirectional(oldElement.hauteurChuteProperty());
            ui_numCouche.getValueFactory().valueProperty().unbindBidirectional(oldElement.numCoucheProperty());
            ui_epaisseur.getValueFactory().valueProperty().unbindBidirectional(oldElement.epaisseurProperty());
            ui_passeSportEauVive.selectedProperty().unbindBidirectional(oldElement.passeSportEauViveProperty());
            ui_passePoisson.selectedProperty().unbindBidirectional(oldElement.passePoissonProperty());
            ui_surfaceRempantEntretien.getValueFactory().valueProperty().unbindBidirectional(oldElement.surfaceRempantEntretienProperty());
        // Propriétés de AvecGeometrie
        // Propriétés de AotCotAssociable
        // Propriétés de Objet
        // Propriétés de ObjetPhotographiable
        // Propriétés de ObjetLit
        }

        final Session session = Injector.getBean(Session.class);

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de SeuilLit
        // * libelle
        ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
        // * commune
        ui_commune.textProperty().bindBidirectional(newElement.communeProperty());
        // * anneeConstruction
        ui_anneeConstruction.getValueFactory().valueProperty().bindBidirectional(newElement.anneeConstructionProperty());
        // * dateDerniereInspection
        ui_dateDerniereInspection.valueProperty().bindBidirectional(newElement.dateDerniereInspectionProperty());
        // * penteRampant
        ui_penteRampant.getValueFactory().valueProperty().bindBidirectional(newElement.penteRampantProperty());
        // * longueurTotale
        ui_longueurTotale.getValueFactory().valueProperty().bindBidirectional(newElement.longueurTotaleProperty());
        // * longueurCoursier
        ui_longueurCoursier.getValueFactory().valueProperty().bindBidirectional(newElement.longueurCoursierProperty());
        // * largeurEnCrete
        ui_largeurEnCrete.getValueFactory().valueProperty().bindBidirectional(newElement.largeurEnCreteProperty());
        // * hauteurChute
        ui_hauteurChute.getValueFactory().valueProperty().bindBidirectional(newElement.hauteurChuteProperty());
        // * numCouche
        ui_numCouche.getValueFactory().valueProperty().bindBidirectional(newElement.numCoucheProperty());
        // * epaisseur
        ui_epaisseur.getValueFactory().valueProperty().bindBidirectional(newElement.epaisseurProperty());
        // * passeSportEauVive
        ui_passeSportEauVive.selectedProperty().bindBidirectional(newElement.passeSportEauViveProperty());
        // * passePoisson
        ui_passePoisson.selectedProperty().bindBidirectional(newElement.passePoissonProperty());
        // * surfaceRempantEntretien
        ui_surfaceRempantEntretien.getValueFactory().valueProperty().bindBidirectional(newElement.surfaceRempantEntretienProperty());
        SIRS.initCombo(ui_fonctionSeuilId, FXCollections.observableList(
            previewRepository.getByClass(RefFonctionSeuilLit.class)),
            newElement.getFonctionSeuilId() == null? null : previewRepository.get(newElement.getFonctionSeuilId()));
        SIRS.initCombo(ui_typeInspectionId, FXCollections.observableList(
            previewRepository.getByClass(RefTypeInspectionSeuilLit.class)),
            newElement.getTypeInspectionId() == null? null : previewRepository.get(newElement.getTypeInspectionId()));
        SIRS.initCombo(ui_materiauPrincipalA, FXCollections.observableList(
            previewRepository.getByClass(RefMateriau.class)),
            newElement.getMateriauPrincipalA() == null? null : previewRepository.get(newElement.getMateriauPrincipalA()));
        SIRS.initCombo(ui_materiauPrincipalB, FXCollections.observableList(
            previewRepository.getByClass(RefMateriau.class)),
            newElement.getMateriauPrincipalB() == null? null : previewRepository.get(newElement.getMateriauPrincipalB()));
        SIRS.initCombo(ui_positionSeuilId, FXCollections.observableList(
            previewRepository.getByClass(RefPositionAxeSeuilLit.class)),
            newElement.getPositionSeuilId() == null? null : previewRepository.get(newElement.getPositionSeuilId()));
        SIRS.initCombo(ui_geometrieCreteId, FXCollections.observableList(
            previewRepository.getByClass(RefGeometrieCreteSeuilLit.class)),
            newElement.getGeometrieCreteId() == null? null : previewRepository.get(newElement.getGeometrieCreteId()));
        SIRS.initCombo(ui_profilCoursierId, FXCollections.observableList(
            previewRepository.getByClass(RefProfilCoursierSeuilLit.class)),
            newElement.getProfilCoursierId() == null? null : previewRepository.get(newElement.getProfilCoursierId()));
        SIRS.initCombo(ui_natureId, FXCollections.observableList(
            previewRepository.getByClass(RefNature.class)),
            newElement.getNatureId() == null? null : previewRepository.get(newElement.getNatureId()));
        SIRS.initCombo(ui_materiauId, FXCollections.observableList(
            previewRepository.getByClass(RefMateriau.class)),
            newElement.getMateriauId() == null? null : previewRepository.get(newElement.getMateriauId()));
        SIRS.initCombo(ui_fonctionId, FXCollections.observableList(
            previewRepository.getByClass(RefFonction.class)),
            newElement.getFonctionId() == null? null : previewRepository.get(newElement.getFonctionId()));
        SIRS.initCombo(ui_sourceId, FXCollections.observableList(
            previewRepository.getByClass(RefSource.class)),
            newElement.getSourceId() == null? null : previewRepository.get(newElement.getSourceId()));
        SIRS.initCombo(ui_positionStructureId, FXCollections.observableList(
            previewRepository.getByClass(RefPositionStructureSeuilLit.class)),
            newElement.getPositionStructureId() == null? null : previewRepository.get(newElement.getPositionStructureId()));
        plansTable.setParentElement(newElement);
        plansTable.setTableItems(()-> (ObservableList) newElement.getPlans());
        voieAccesIdsTable.setParentElement(null);
        final AbstractSIRSRepository<VoieAcces> voieAccesIdsRepo = session.getRepositoryForClass(VoieAcces.class);
        voieAccesIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getVoieAccesIds(), voieAccesIdsRepo));
        ouvrageFranchissementIdsTable.setParentElement(null);
        final AbstractSIRSRepository<OuvrageFranchissement> ouvrageFranchissementIdsRepo = session.getRepositoryForClass(OuvrageFranchissement.class);
        ouvrageFranchissementIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOuvrageFranchissementIds(), ouvrageFranchissementIdsRepo));
        voieDigueIdsTable.setParentElement(null);
        final AbstractSIRSRepository<VoieDigue> voieDigueIdsRepo = session.getRepositoryForClass(VoieDigue.class);
        voieDigueIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getVoieDigueIds(), voieDigueIdsRepo));
        ouvrageVoirieIdsTable.setParentElement(null);
        final AbstractSIRSRepository<OuvrageVoirie> ouvrageVoirieIdsRepo = session.getRepositoryForClass(OuvrageVoirie.class);
        ouvrageVoirieIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOuvrageVoirieIds(), ouvrageVoirieIdsRepo));
        stationPompageIdsTable.setParentElement(null);
        final AbstractSIRSRepository<StationPompage> stationPompageIdsRepo = session.getRepositoryForClass(StationPompage.class);
        stationPompageIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getStationPompageIds(), stationPompageIdsRepo));
        reseauHydrauliqueFermeIdsTable.setParentElement(null);
        final AbstractSIRSRepository<ReseauHydrauliqueFerme> reseauHydrauliqueFermeIdsRepo = session.getRepositoryForClass(ReseauHydrauliqueFerme.class);
        reseauHydrauliqueFermeIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getReseauHydrauliqueFermeIds(), reseauHydrauliqueFermeIdsRepo));
        reseauHydrauliqueCielOuvertIdsTable.setParentElement(null);
        final AbstractSIRSRepository<ReseauHydrauliqueCielOuvert> reseauHydrauliqueCielOuvertIdsRepo = session.getRepositoryForClass(ReseauHydrauliqueCielOuvert.class);
        reseauHydrauliqueCielOuvertIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getReseauHydrauliqueCielOuvertIds(), reseauHydrauliqueCielOuvertIdsRepo));
        ouvrageHydrauliqueAssocieIdsTable.setParentElement(null);
        final AbstractSIRSRepository<OuvrageHydrauliqueAssocie> ouvrageHydrauliqueAssocieIdsRepo = session.getRepositoryForClass(OuvrageHydrauliqueAssocie.class);
        ouvrageHydrauliqueAssocieIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOuvrageHydrauliqueAssocieIds(), ouvrageHydrauliqueAssocieIdsRepo));
        ouvrageTelecomEnergieIdsTable.setParentElement(null);
        final AbstractSIRSRepository<OuvrageTelecomEnergie> ouvrageTelecomEnergieIdsRepo = session.getRepositoryForClass(OuvrageTelecomEnergie.class);
        ouvrageTelecomEnergieIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOuvrageTelecomEnergieIds(), ouvrageTelecomEnergieIdsRepo));
        reseauTelecomEnergieIdsTable.setParentElement(null);
        final AbstractSIRSRepository<ReseauTelecomEnergie> reseauTelecomEnergieIdsRepo = session.getRepositoryForClass(ReseauTelecomEnergie.class);
        reseauTelecomEnergieIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getReseauTelecomEnergieIds(), reseauTelecomEnergieIdsRepo));
        ouvrageParticulierIdsTable.setParentElement(null);
        final AbstractSIRSRepository<OuvrageParticulier> ouvrageParticulierIdsRepo = session.getRepositoryForClass(OuvrageParticulier.class);
        ouvrageParticulierIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOuvrageParticulierIds(), ouvrageParticulierIdsRepo));
        echelleLimnimetriqueIdsTable.setParentElement(null);
        final AbstractSIRSRepository<EchelleLimnimetrique> echelleLimnimetriqueIdsRepo = session.getRepositoryForClass(EchelleLimnimetrique.class);
        echelleLimnimetriqueIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getEchelleLimnimetriqueIds(), echelleLimnimetriqueIdsRepo));
        desordreIdsTable.setParentElement(null);
        final AbstractSIRSRepository<DesordreLit> desordreIdsRepo = session.getRepositoryForClass(DesordreLit.class);
        desordreIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getDesordreIds(), desordreIdsRepo));
        if(bergeClass!=null){
            bergeIdsTable.setParentElement(null);
            final AbstractSIRSRepository<TronconDigue> bergeIdsRepo = Injector.getSession().getRepositoryForClass(bergeClass);
            bergeIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getBergeIds(), bergeIdsRepo));
        }
        digueIdsTable.setParentElement(null);
        final AbstractSIRSRepository<Digue> digueIdsRepo = session.getRepositoryForClass(Digue.class);
        digueIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getDigueIds(), digueIdsRepo));
        // Propriétés de AvecGeometrie
        // Propriétés de AotCotAssociable
        // Propriétés de Objet
        // * commentaire
        ui_commentaire.setHtmlText(newElement.getCommentaire());
        SIRS.initCombo(ui_linearId, FXCollections.observableList(
            previewRepository.getByClass(TronconDigue.class)),
            newElement.getLinearId() == null? null : previewRepository.get(newElement.getLinearId()));
        // Propriétés de ObjetPhotographiable
        photosTable.setParentElement(newElement);
        photosTable.setTableItems(()-> (ObservableList) newElement.getPhotos());
        // Propriétés de ObjetLit
        proprietesTable.setParentElement(newElement);
        proprietesTable.setTableItems(()-> (ObservableList) newElement.getProprietes());
        gestionsTable.setParentElement(newElement);
        gestionsTable.setTableItems(()-> (ObservableList) newElement.getGestions());
        voieAccesIdsTable.setObservableListToListen(newElement.getVoieAccesIds());
        ouvrageFranchissementIdsTable.setObservableListToListen(newElement.getOuvrageFranchissementIds());
        voieDigueIdsTable.setObservableListToListen(newElement.getVoieDigueIds());
        ouvrageVoirieIdsTable.setObservableListToListen(newElement.getOuvrageVoirieIds());
        stationPompageIdsTable.setObservableListToListen(newElement.getStationPompageIds());
        reseauHydrauliqueFermeIdsTable.setObservableListToListen(newElement.getReseauHydrauliqueFermeIds());
        reseauHydrauliqueCielOuvertIdsTable.setObservableListToListen(newElement.getReseauHydrauliqueCielOuvertIds());
        ouvrageHydrauliqueAssocieIdsTable.setObservableListToListen(newElement.getOuvrageHydrauliqueAssocieIds());
        ouvrageTelecomEnergieIdsTable.setObservableListToListen(newElement.getOuvrageTelecomEnergieIds());
        reseauTelecomEnergieIdsTable.setObservableListToListen(newElement.getReseauTelecomEnergieIds());
        ouvrageParticulierIdsTable.setObservableListToListen(newElement.getOuvrageParticulierIds());
        echelleLimnimetriqueIdsTable.setObservableListToListen(newElement.getEchelleLimnimetriqueIds());
        desordreIdsTable.setObservableListToListen(newElement.getDesordreIds());
        if(bergeClass!=null){
            bergeIdsTable.setObservableListToListen(newElement.getBergeIds());
        }
        digueIdsTable.setObservableListToListen(newElement.getDigueIds());
        
    }

    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final SeuilLit element = (SeuilLit) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());

        uiPositionable.preSave();

        Object cbValue;
        cbValue = ui_fonctionSeuilId.getValue();
        if (cbValue instanceof Preview) {
            element.setFonctionSeuilId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setFonctionSeuilId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setFonctionSeuilId(null);
        }
        cbValue = ui_typeInspectionId.getValue();
        if (cbValue instanceof Preview) {
            element.setTypeInspectionId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypeInspectionId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypeInspectionId(null);
        }
        cbValue = ui_materiauPrincipalA.getValue();
        if (cbValue instanceof Preview) {
            element.setMateriauPrincipalA(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setMateriauPrincipalA(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setMateriauPrincipalA(null);
        }
        cbValue = ui_materiauPrincipalB.getValue();
        if (cbValue instanceof Preview) {
            element.setMateriauPrincipalB(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setMateriauPrincipalB(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setMateriauPrincipalB(null);
        }
        cbValue = ui_positionSeuilId.getValue();
        if (cbValue instanceof Preview) {
            element.setPositionSeuilId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setPositionSeuilId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setPositionSeuilId(null);
        }
        cbValue = ui_geometrieCreteId.getValue();
        if (cbValue instanceof Preview) {
            element.setGeometrieCreteId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setGeometrieCreteId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setGeometrieCreteId(null);
        }
        cbValue = ui_profilCoursierId.getValue();
        if (cbValue instanceof Preview) {
            element.setProfilCoursierId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setProfilCoursierId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setProfilCoursierId(null);
        }
        cbValue = ui_natureId.getValue();
        if (cbValue instanceof Preview) {
            element.setNatureId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setNatureId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setNatureId(null);
        }
        cbValue = ui_materiauId.getValue();
        if (cbValue instanceof Preview) {
            element.setMateriauId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setMateriauId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setMateriauId(null);
        }
        cbValue = ui_fonctionId.getValue();
        if (cbValue instanceof Preview) {
            element.setFonctionId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setFonctionId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setFonctionId(null);
        }
        cbValue = ui_sourceId.getValue();
        if (cbValue instanceof Preview) {
            element.setSourceId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setSourceId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setSourceId(null);
        }
        cbValue = ui_positionStructureId.getValue();
        if (cbValue instanceof Preview) {
            element.setPositionStructureId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setPositionStructureId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setPositionStructureId(null);
        }
        // Manage opposite references for VoieAcces...
        final List<String> currentVoieAccesIdsList = new ArrayList<>();
        for(final Element elt : voieAccesIdsTable.getAllValues()){
            final VoieAcces voieAcces = (VoieAcces) elt;
            currentVoieAccesIdsList.add(voieAcces.getId());
        }
        element.setVoieAccesIds(currentVoieAccesIdsList);

        // Manage opposite references for OuvrageFranchissement...
        final List<String> currentOuvrageFranchissementIdsList = new ArrayList<>();
        for(final Element elt : ouvrageFranchissementIdsTable.getAllValues()){
            final OuvrageFranchissement ouvrageFranchissement = (OuvrageFranchissement) elt;
            currentOuvrageFranchissementIdsList.add(ouvrageFranchissement.getId());
        }
        element.setOuvrageFranchissementIds(currentOuvrageFranchissementIdsList);

        // Manage opposite references for VoieDigue...
        final List<String> currentVoieDigueIdsList = new ArrayList<>();
        for(final Element elt : voieDigueIdsTable.getAllValues()){
            final VoieDigue voieDigue = (VoieDigue) elt;
            currentVoieDigueIdsList.add(voieDigue.getId());
        }
        element.setVoieDigueIds(currentVoieDigueIdsList);

        // Manage opposite references for OuvrageVoirie...
        final List<String> currentOuvrageVoirieIdsList = new ArrayList<>();
        for(final Element elt : ouvrageVoirieIdsTable.getAllValues()){
            final OuvrageVoirie ouvrageVoirie = (OuvrageVoirie) elt;
            currentOuvrageVoirieIdsList.add(ouvrageVoirie.getId());
        }
        element.setOuvrageVoirieIds(currentOuvrageVoirieIdsList);

        // Manage opposite references for StationPompage...
        final List<String> currentStationPompageIdsList = new ArrayList<>();
        for(final Element elt : stationPompageIdsTable.getAllValues()){
            final StationPompage stationPompage = (StationPompage) elt;
            currentStationPompageIdsList.add(stationPompage.getId());
        }
        element.setStationPompageIds(currentStationPompageIdsList);

        // Manage opposite references for ReseauHydrauliqueFerme...
        final List<String> currentReseauHydrauliqueFermeIdsList = new ArrayList<>();
        for(final Element elt : reseauHydrauliqueFermeIdsTable.getAllValues()){
            final ReseauHydrauliqueFerme reseauHydrauliqueFerme = (ReseauHydrauliqueFerme) elt;
            currentReseauHydrauliqueFermeIdsList.add(reseauHydrauliqueFerme.getId());
        }
        element.setReseauHydrauliqueFermeIds(currentReseauHydrauliqueFermeIdsList);

        // Manage opposite references for ReseauHydrauliqueCielOuvert...
        final List<String> currentReseauHydrauliqueCielOuvertIdsList = new ArrayList<>();
        for(final Element elt : reseauHydrauliqueCielOuvertIdsTable.getAllValues()){
            final ReseauHydrauliqueCielOuvert reseauHydrauliqueCielOuvert = (ReseauHydrauliqueCielOuvert) elt;
            currentReseauHydrauliqueCielOuvertIdsList.add(reseauHydrauliqueCielOuvert.getId());
        }
        element.setReseauHydrauliqueCielOuvertIds(currentReseauHydrauliqueCielOuvertIdsList);

        // Manage opposite references for OuvrageHydrauliqueAssocie...
        final List<String> currentOuvrageHydrauliqueAssocieIdsList = new ArrayList<>();
        for(final Element elt : ouvrageHydrauliqueAssocieIdsTable.getAllValues()){
            final OuvrageHydrauliqueAssocie ouvrageHydrauliqueAssocie = (OuvrageHydrauliqueAssocie) elt;
            currentOuvrageHydrauliqueAssocieIdsList.add(ouvrageHydrauliqueAssocie.getId());
        }
        element.setOuvrageHydrauliqueAssocieIds(currentOuvrageHydrauliqueAssocieIdsList);

        // Manage opposite references for OuvrageTelecomEnergie...
        final List<String> currentOuvrageTelecomEnergieIdsList = new ArrayList<>();
        for(final Element elt : ouvrageTelecomEnergieIdsTable.getAllValues()){
            final OuvrageTelecomEnergie ouvrageTelecomEnergie = (OuvrageTelecomEnergie) elt;
            currentOuvrageTelecomEnergieIdsList.add(ouvrageTelecomEnergie.getId());
        }
        element.setOuvrageTelecomEnergieIds(currentOuvrageTelecomEnergieIdsList);

        // Manage opposite references for ReseauTelecomEnergie...
        final List<String> currentReseauTelecomEnergieIdsList = new ArrayList<>();
        for(final Element elt : reseauTelecomEnergieIdsTable.getAllValues()){
            final ReseauTelecomEnergie reseauTelecomEnergie = (ReseauTelecomEnergie) elt;
            currentReseauTelecomEnergieIdsList.add(reseauTelecomEnergie.getId());
        }
        element.setReseauTelecomEnergieIds(currentReseauTelecomEnergieIdsList);

        // Manage opposite references for OuvrageParticulier...
        final List<String> currentOuvrageParticulierIdsList = new ArrayList<>();
        for(final Element elt : ouvrageParticulierIdsTable.getAllValues()){
            final OuvrageParticulier ouvrageParticulier = (OuvrageParticulier) elt;
            currentOuvrageParticulierIdsList.add(ouvrageParticulier.getId());
        }
        element.setOuvrageParticulierIds(currentOuvrageParticulierIdsList);

        // Manage opposite references for EchelleLimnimetrique...
        final List<String> currentEchelleLimnimetriqueIdsList = new ArrayList<>();
        for(final Element elt : echelleLimnimetriqueIdsTable.getAllValues()){
            final EchelleLimnimetrique echelleLimnimetrique = (EchelleLimnimetrique) elt;
            currentEchelleLimnimetriqueIdsList.add(echelleLimnimetrique.getId());
        }
        element.setEchelleLimnimetriqueIds(currentEchelleLimnimetriqueIdsList);

        /*
        * En cas de reference opposee on se prepare a stocker les objets
        * "opposes" pour les mettre � jour.
        */
        final List<DesordreLit> currentDesordreLitList = new ArrayList<>();
        // Manage opposite references for DesordreLit...
        /*
        * Si on est sur une reference principale, on a besoin du depot pour
        * supprimer reellement les elements que l'on va retirer du tableau.
        * Si on a une reference opposee, on a besoin du depot pour mettre a jour
        * les objets qui referencent l'objet courant en sens contraire.
        */
        final AbstractSIRSRepository<DesordreLit> desordreLitRepository = session.getRepositoryForClass(DesordreLit.class);
        final List<String> currentDesordreLitIdsList = new ArrayList<>();
        for(final Element elt : desordreIdsTable.getAllValues()){
            final DesordreLit desordreLit = (DesordreLit) elt;
            currentDesordreLitIdsList.add(desordreLit.getId());
            currentDesordreLitList.add(desordreLit);

            // Addition
            if(!desordreLit.getSeuilIds().contains(element.getId())){
                desordreLit.getSeuilIds().add(element.getId());
            }
        }
        desordreLitRepository.executeBulk(currentDesordreLitList);
        element.setDesordreIds(currentDesordreLitIdsList);

        // Deletion
        final StreamingIterable<DesordreLit> listDesordreLit = desordreLitRepository.getAllStreaming();
        try (final CloseableIterator<DesordreLit> it = listDesordreLit.iterator()) {
            while (it.hasNext()) {
                final DesordreLit i = it.next();
                if(i.getSeuilIds().contains(element.getId())
                    || element.getDesordreIds().contains(i.getId())){
                    if(!desordreIdsTable.getAllValues().contains(i)){
                        element.getDesordreIds().remove(i.getId()); //Normalement inutile du fait du  clear avant les op�rations d'ajout
                        i.getSeuilIds().remove(element.getId());
                        desordreLitRepository.update(i);
                    }
                }
            }
        }
        // Manage opposite references for TronconDigue...
        final List<String> currentTronconDigueIdsList = new ArrayList<>();
        for(final Element elt : bergeIdsTable.getAllValues()){
            final TronconDigue tronconDigue = (TronconDigue) elt;
            currentTronconDigueIdsList.add(tronconDigue.getId());
        }
        element.setBergeIds(currentTronconDigueIdsList);

        // Manage opposite references for Digue...
        final List<String> currentDigueIdsList = new ArrayList<>();
        for(final Element elt : digueIdsTable.getAllValues()){
            final Digue digue = (Digue) elt;
            currentDigueIdsList.add(digue.getId());
        }
        element.setDigueIds(currentDigueIdsList);

        cbValue = ui_linearId.getValue();
        if (cbValue instanceof Preview) {
            element.setLinearId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setLinearId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setLinearId(null);
        }
    }
}
