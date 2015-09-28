
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.FXFileTextField;
import fr.sirs.util.FXComponentField;
import fr.sirs.util.javafx.FloatSpinnerValueFactory;
import fr.sirs.util.StreamingIterable;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.web.HTMLEditor;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import org.geotoolkit.gui.javafx.util.FXDateField;
import org.geotoolkit.util.collection.CloseableIterator;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXDesordreDependancePane extends AbstractFXElementPane<DesordreDependance> {

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;
    
    @FXML private FXValidityPeriodPane uiValidityPeriod;
    @FXML FXPositionDependancePane uiPosition;

    // Propriétés de DesordreDependance
    @FXML protected TextField ui_lieuDit;
    @FXML protected HTMLEditor ui_commentaire;
    @FXML protected ComboBox ui_dependanceId;
    @FXML protected Button ui_dependanceId_link;
    @FXML protected Tab ui_observations;
    protected final PojoTable observationsTable;
    @FXML protected Tab ui_evenementHydrauliqueIds;
    protected final ListeningPojoTable evenementHydrauliqueIdsTable;

    // Propriétés de AvecGeometrie

    /**
     * Constructor. Initialize part of the UI which will not require update when 
     * element edited change.
     */
    protected FXDesordreDependancePane() {
        SIRS.loadFXML(this, DesordreDependance.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

        /*
         * Disabling rules.
         */
        ui_lieuDit.disableProperty().bind(disableFieldsProperty());
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_dependanceId.disableProperty().bind(disableFieldsProperty());
        ui_dependanceId_link.disableProperty().bind(ui_dependanceId.getSelectionModel().selectedItemProperty().isNull());
        ui_dependanceId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_dependanceId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_dependanceId.getSelectionModel().getSelectedItem()));       
        observationsTable = new PojoTable(ObservationDependance.class, null);
        observationsTable.editableProperty().bind(disableFieldsProperty().not());
        ui_observations.setContent(observationsTable);
        ui_observations.setClosable(false);
        evenementHydrauliqueIdsTable = new ListeningPojoTable(EvenementHydraulique.class, null);
        evenementHydrauliqueIdsTable.editableProperty().bind(disableFieldsProperty().not());
        evenementHydrauliqueIdsTable.createNewProperty().set(false);
        ui_evenementHydrauliqueIds.setContent(evenementHydrauliqueIdsTable);
        ui_evenementHydrauliqueIds.setClosable(false);
        
		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());

        uiPosition.dependanceProperty().bind(elementProperty);
    }
    
    public FXDesordreDependancePane(final DesordreDependance desordreDependance){
        this();
        this.elementProperty().set(desordreDependance);  
    }     

    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends DesordreDependance > observableElement, DesordreDependance oldElement, DesordreDependance newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de DesordreDependance
            ui_lieuDit.textProperty().unbindBidirectional(oldElement.lieuDitProperty());
        // Propriétés de AvecGeometrie
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de DesordreDependance
        // * lieuDit
        ui_lieuDit.textProperty().bindBidirectional(newElement.lieuDitProperty());
        // * commentaire
        ui_commentaire.setHtmlText(newElement.getCommentaire());
        SIRS.initCombo(ui_dependanceId, FXCollections.observableList(
            previewRepository.getByClass(AbstractDependance.class)), 
            newElement.getDependanceId() == null? null : previewRepository.get(newElement.getDependanceId()));
        observationsTable.setParentElement(newElement);
        observationsTable.setTableItems(()-> (ObservableList) newElement.getObservations());
        evenementHydrauliqueIdsTable.setParentElement(null);
        final AbstractSIRSRepository<EvenementHydraulique> evenementHydrauliqueIdsRepo = session.getRepositoryForClass(EvenementHydraulique.class);
        evenementHydrauliqueIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getEvenementHydrauliqueIds(), evenementHydrauliqueIdsRepo));
        // Propriétés de AvecGeometrie
        evenementHydrauliqueIdsTable.setObservableListToListen(newElement.getEvenementHydrauliqueIds());
    }
    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final DesordreDependance element = (DesordreDependance) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());


        Object cbValue;
        cbValue = ui_dependanceId.getValue();
        if (cbValue instanceof Preview) {
            element.setDependanceId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setDependanceId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setDependanceId(null);
        }
        // Manage opposite references for EvenementHydraulique...
        final List<String> currentEvenementHydrauliqueIdsList = new ArrayList<>();
        for(final Element elt : evenementHydrauliqueIdsTable.getAllValues()){
            final EvenementHydraulique evenementHydraulique = (EvenementHydraulique) elt;
            currentEvenementHydrauliqueIdsList.add(evenementHydraulique.getId());
        }
        element.setEvenementHydrauliqueIds(currentEvenementHydrauliqueIdsList);
        
    }
}
