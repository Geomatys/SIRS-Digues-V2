
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.core.model.GestionObjet;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.MateriauDependance;
import fr.sirs.core.model.PhotoDependance;
import fr.sirs.core.model.ProprieteObjet;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXAireStockageDependancePane extends AbstractFXElementPane<AireStockageDependance> {

    private final Previews previewRepository;
    private LabelMapper labelMapper;

    @FXML FXPositionDependancePane uiPosition;
    @FXML FXValidityPeriodPane uiValidityPeriod;

    // Propriétés de AireStockageDependance
    @FXML Tab ui_materiaux;
    final PojoTable materiauxTable;

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
    private FXAireStockageDependancePane() {
        SIRS.loadFXML(this, AireStockageDependance.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());
        materiauxTable = new PojoTable(MateriauDependance.class, null);
        materiauxTable.editableProperty().bind(disableFieldsProperty().not());
        ui_materiaux.setContent(materiauxTable);
        ui_materiaux.setClosable(false);
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
    
    public FXAireStockageDependancePane(final AireStockageDependance aireStockageDependance){
        this();
        this.elementProperty().set(aireStockageDependance);
        
    }     

    /**
     * Initialize fields at element setting.
     */
    private void initFields(ObservableValue<? extends AireStockageDependance > observableElement, AireStockageDependance oldElement, AireStockageDependance newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de AireStockageDependance
        // Propriétés de AotCotAssociable
        // Propriétés de AbstractDependance
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de AireStockageDependance

        materiauxTable.setParentElement(newElement);
        materiauxTable.setTableItems(()-> (ObservableList) newElement.getMateriaux());
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
        final AireStockageDependance element = (AireStockageDependance) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());


        Object cbValue;
    }
}
