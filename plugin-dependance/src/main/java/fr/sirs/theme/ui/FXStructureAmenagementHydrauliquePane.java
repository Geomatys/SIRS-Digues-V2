
package fr.sirs.theme.ui;

import fr.sirs.theme.ui.*;
import fr.sirs.theme.ui.pojotable.PojoTableExternalAddable;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.FXFileTextField;
import fr.sirs.util.FXComponentField;
import fr.sirs.util.javafx.FloatSpinnerValueFactory;
import fr.sirs.util.StreamingIterable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.DatePickerConverter;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
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
import javafx.beans.value.ChangeListener;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class FXStructureAmenagementHydrauliquePane extends AbstractFXElementPane<StructureAmenagementHydraulique> {
    
    protected final Previews previewRepository;
    protected LabelMapper labelMapper;

    @FXML private FXValidityPeriodPane uiValidityPeriod;

    // Propriétés de StructureAmenagementHydraulique
    @FXML protected Spinner ui_numCouche;
    @FXML protected ComboBox ui_materiauId;
    @FXML protected Button ui_materiauId_link;
    @FXML protected ComboBox ui_sourceId;
    @FXML protected Button ui_sourceId_link;
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
    protected FXStructureAmenagementHydrauliquePane() {
        SIRS.loadFXML(this, StructureAmenagementHydraulique.class);
        final Session session = Injector.getBean(Session.class);
        previewRepository = session.getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

        /*
        * Disabling rules.
        */
        ui_numCouche.disableProperty().bind(disableFieldsProperty());
        ui_numCouche.setEditable(true);
        ui_numCouche.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        ui_materiauId.disableProperty().bind(disableFieldsProperty());
        ui_materiauId_link.setVisible(false);
        ui_sourceId.disableProperty().bind(disableFieldsProperty());
        ui_sourceId_link.setVisible(false);
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());
        
        uiPosition.dependanceProperty().bind(elementProperty);
        
        ui_observations.setContent(() -> {
            observationsTable = new PojoTableExternalAddable(Observation.class, elementProperty());
            observationsTable.editableProperty().bind(disableFieldsProperty().not());
            updateObservationsTable(session, elementProperty.get());
            return observationsTable;
        });
        ui_observations.setClosable(false);
        
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
    
    public FXStructureAmenagementHydrauliquePane(final StructureAmenagementHydraulique structureAmenagementHydraulique){
        this();
        this.elementProperty().set(structureAmenagementHydraulique);
    }
    
    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends StructureAmenagementHydraulique > observableElement, StructureAmenagementHydraulique oldElement, StructureAmenagementHydraulique newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
            // Propriétés de StructureAmenagementHydraulique
            ui_numCouche.getValueFactory().valueProperty().unbindBidirectional(oldElement.numCoucheProperty());
            ui_numCouche.getValueFactory().setValue(0);
            // Propriétés de AvecGeometrie
            // Propriétés de AvecSettableGeometrie
            // Propriétés de DescriptionAmenagementHydraulique
        }
        
        final Session session = Injector.getBean(Session.class);
        
        if (newElement == null) {
            
            ui_materiauId.setItems(null);
            ui_sourceId.setItems(null);
            ui_amenagementHydrauliqueId.setItems(null);
        } else {
            
            
            /*
            * Bind control properties to Element ones.
            */
            // Propriétés de StructureAmenagementHydraulique
            // * numCouche
            ui_numCouche.getValueFactory().valueProperty().bindBidirectional(newElement.numCoucheProperty());
            final AbstractSIRSRepository<RefMateriau> materiauIdRepo = session.getRepositoryForClass(RefMateriau.class);
            SIRS.initCombo(ui_materiauId, SIRS.observableList(materiauIdRepo.getAll()), newElement.getMateriauId() == null? null : materiauIdRepo.get(newElement.getMateriauId()));
            final AbstractSIRSRepository<RefSource> sourceIdRepo = session.getRepositoryForClass(RefSource.class);
            SIRS.initCombo(ui_sourceId, SIRS.observableList(sourceIdRepo.getAll()), newElement.getSourceId() == null? null : sourceIdRepo.get(newElement.getSourceId()));
            // Propriétés de AvecGeometrie
            // Propriétés de AvecSettableGeometrie
            // Propriétés de DescriptionAmenagementHydraulique
            {
            final Preview linearPreview = newElement.getAmenagementHydrauliqueId() == null ? null : previewRepository.get(newElement.getAmenagementHydrauliqueId());
            SIRS.initCombo(ui_amenagementHydrauliqueId, SIRS.observableList(
                    previewRepository.getByClass(linearPreview == null ? AmenagementHydraulique.class : linearPreview.getJavaClassOr(AmenagementHydraulique.class))).sorted(), linearPreview);
        }
        }
        
        updateObservationsTable(session, newElement);
        updatePhotosTable(session, newElement);
    }
    
    
    protected void updateObservationsTable(final Session session, final StructureAmenagementHydraulique newElement) {
        if (observationsTable == null)
            return;
        
        if (newElement == null) {
            observationsTable.setTableItems(null);
        } else {
            observationsTable.setParentElement(newElement);
            observationsTable.setTableItems(()-> (ObservableList) newElement.getObservations());
        }
    }
    
    
    protected void updatePhotosTable(final Session session, final StructureAmenagementHydraulique newElement) {
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
        final StructureAmenagementHydraulique element = (StructureAmenagementHydraulique) elementProperty().get();
        
        
        
        
        Object cbValue;
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
