
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.CheminAccesDependance;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.GestionObjet;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.PhotoDependance;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ProprieteObjet;
import fr.sirs.core.model.RefRevetement;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXCheminAccesDependancePane extends AbstractFXElementPane<CheminAccesDependance> {

    private final Previews previewRepository;
    private LabelMapper labelMapper;

    @FXML FXPositionDependancePane uiPosition;
    @FXML FXValidityPeriodPane uiValidityPeriod;

    // Propriétés de CheminAccesDependance
    @FXML Spinner ui_largeur;
    @FXML CheckBox ui_statut;
    @FXML ComboBox ui_revetementId;
    @FXML Button ui_revetementId_link;

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
    private FXCheminAccesDependancePane() {
        SIRS.loadFXML(this, CheminAccesDependance.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());
        ui_largeur.disableProperty().bind(disableFieldsProperty());
        ui_largeur.setEditable(true);
        ui_largeur.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE));
        ui_statut.disableProperty().bind(disableFieldsProperty());
        ui_revetementId.disableProperty().bind(disableFieldsProperty());
        ui_revetementId_link.setVisible(false);
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
    
    public FXCheminAccesDependancePane(final CheminAccesDependance cheminAccesDependance){
        this();
        this.elementProperty().set(cheminAccesDependance);
        
    }     

    /**
     * Initialize fields at element setting.
     */
    private void initFields(ObservableValue<? extends CheminAccesDependance > observableElement, CheminAccesDependance oldElement, CheminAccesDependance newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de CheminAccesDependance

            ui_largeur.getValueFactory().valueProperty().unbindBidirectional(oldElement.largeurProperty());
            ui_statut.selectedProperty().unbindBidirectional(oldElement.statutProperty());
        // Propriétés de AotCotAssociable
        // Propriétés de AbstractDependance
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de CheminAccesDependance
        // * largeur
        ui_largeur.getValueFactory().valueProperty().bindBidirectional(newElement.largeurProperty());
        // * statut
        ui_statut.selectedProperty().bindBidirectional(newElement.statutProperty());

        SIRS.initCombo(ui_revetementId, FXCollections.observableArrayList(
            previewRepository.getByClass(RefRevetement.class)), 
            newElement.getRevetementId() == null? null : previewRepository.get(newElement.getRevetementId()));
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
        final CheminAccesDependance element = (CheminAccesDependance) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());


        Object cbValue;
        cbValue = ui_revetementId.getValue();
        if (cbValue instanceof Preview) {
            element.setRevetementId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setRevetementId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setRevetementId(null);
        }
    }
}
