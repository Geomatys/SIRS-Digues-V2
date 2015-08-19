
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.AutreDependance;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.GestionObjet;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.PhotoDependance;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ProprieteObjet;
import fr.sirs.core.model.RefDestinationDependance;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXAutreDependancePane extends AbstractFXElementPane<AutreDependance> {

    private final Previews previewRepository;
    private LabelMapper labelMapper;

    @FXML FXPositionDependancePane uiPosition;
    @FXML FXValidityPeriodPane uiValidityPeriod;

    // Propriétés de AutreDependance
    @FXML ComboBox ui_destinationId;
    @FXML Button ui_destinationId_link;

    // Propriétés de AotCotAssociable

    // Propriétés de AbstractDependance
    @FXML HTMLEditor ui_commentaire;
    @FXML TextField ui_libelle;
    @FXML Tab ui_proprietes;
    final PojoTable proprietesTable;
    @FXML Tab ui_gestions;
    final PojoTable gestionsTable;
    @FXML Tab ui_photos;
    final PojoTable photosTable;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXAutreDependancePane() {
        SIRS.loadFXML(this, AutreDependance.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());
        ui_destinationId.disableProperty().bind(disableFieldsProperty());
        ui_destinationId_link.setVisible(false);
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        proprietesTable = new PojoTable(ProprieteObjet.class, null);
        proprietesTable.editableProperty().bind(disableFieldsProperty().not());
        ui_proprietes.setContent(proprietesTable);
        ui_proprietes.setClosable(false);
        gestionsTable = new PojoTable(GestionObjet.class, null);
        gestionsTable.editableProperty().bind(disableFieldsProperty().not());
        ui_gestions.setContent(gestionsTable);
        ui_gestions.setClosable(false);
        photosTable = new PojoTable(PhotoDependance.class, null);
        photosTable.editableProperty().bind(disableFieldsProperty().not());
        ui_photos.setContent(photosTable);
        ui_photos.setClosable(false);
    }
    
    public FXAutreDependancePane(final AutreDependance autreDependance){
        this();
        this.elementProperty().set(autreDependance);
        
    }     

    /**
     * Initialize fields at element setting.
     */
    private void initFields(ObservableValue<? extends AutreDependance > observableElement, AutreDependance oldElement, AutreDependance newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de AutreDependance
        // Propriétés de AotCotAssociable
        // Propriétés de AbstractDependance
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de AutreDependance

        SIRS.initCombo(ui_destinationId, FXCollections.observableArrayList(
            previewRepository.getByClass(RefDestinationDependance.class)), 
            newElement.getDestinationId() == null? null : previewRepository.get(newElement.getDestinationId()));
        // Propriétés de AotCotAssociable

        // Propriétés de AbstractDependance
        // * commentaire
        ui_commentaire.setHtmlText(newElement.getCommentaire());
        // * libelle
        ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());

        proprietesTable.setParentElement(newElement);
        proprietesTable.setTableItems(()-> (ObservableList) newElement.getProprietes());
        gestionsTable.setParentElement(newElement);
        gestionsTable.setTableItems(()-> (ObservableList) newElement.getGestions());
        photosTable.setParentElement(newElement);
        photosTable.setTableItems(()-> (ObservableList) newElement.getPhotos());
    }
    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final AutreDependance element = (AutreDependance) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());


        Object cbValue;
        cbValue = ui_destinationId.getValue();
        if (cbValue instanceof Preview) {
            element.setDestinationId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setDestinationId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setDestinationId(null);
        }
    }
}
