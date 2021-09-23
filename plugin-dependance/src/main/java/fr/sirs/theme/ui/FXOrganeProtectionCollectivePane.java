
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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class FXOrganeProtectionCollectivePane extends AbstractFXElementPane<OrganeProtectionCollective> {

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;


    // Propriétés de OrganeProtectionCollective
    @FXML protected Spinner ui_cote;
    @FXML protected ComboBox ui_type;
    @FXML protected Button ui_type_link;
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
    protected FXOrganeProtectionCollectivePane() {
        SIRS.loadFXML(this, OrganeProtectionCollective.class);
        final Session session = Injector.getBean(Session.class);
        previewRepository = session.getPreviews();
        elementProperty().addListener(this::initFields);


        /*
         * Disabling rules.
         */
        ui_cote.disableProperty().bind(disableFieldsProperty());
        ui_cote.setEditable(true);
        ui_cote.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_type.disableProperty().bind(disableFieldsProperty());
        ui_type_link.setVisible(false);
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

    public FXOrganeProtectionCollectivePane(final OrganeProtectionCollective organeProtectionCollective){
        this();
        this.elementProperty().set(organeProtectionCollective);
    }

    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends OrganeProtectionCollective > observableElement, OrganeProtectionCollective oldElement, OrganeProtectionCollective newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de OrganeProtectionCollective
            ui_cote.getValueFactory().valueProperty().unbindBidirectional(oldElement.coteProperty());
            ui_cote.getValueFactory().setValue(0);
        // Propriétés de AvecGeometrie
        // Propriétés de AvecSettableGeometrie
        // Propriétés de DescriptionAmenagementHydraulique
        }

        final Session session = Injector.getBean(Session.class);

        if (newElement == null) {

                ui_type.setItems(null);
                ui_amenagementHydrauliqueId.setItems(null);
        } else {


        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de OrganeProtectionCollective
        // * cote
        ui_cote.getValueFactory().valueProperty().bindBidirectional(newElement.coteProperty());
            final AbstractSIRSRepository<RefTypeOrganeProtectionCollective> typeRepo = session.getRepositoryForClass(RefTypeOrganeProtectionCollective.class);
            SIRS.initCombo(ui_type, SIRS.observableList(typeRepo.getAll()), newElement.getType() == null? null : typeRepo.get(newElement.getType()));
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


    protected void updateObservationsTable(final Session session, final OrganeProtectionCollective newElement) {
            if (observationsTable == null)
                return;

            if (newElement == null) {
                observationsTable.setTableItems(null);
            } else {
        observationsTable.setParentElement(newElement);
        observationsTable.setTableItems(()-> (ObservableList) newElement.getObservations());
        }
    }


    protected void updatePhotosTable(final Session session, final OrganeProtectionCollective newElement) {
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
        final OrganeProtectionCollective element = (OrganeProtectionCollective) elementProperty().get();




        Object cbValue;
        cbValue = ui_type.getValue();
        if (cbValue instanceof Preview) {
            element.setType(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setType(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setType(null);
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
