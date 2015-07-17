
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.javafx.FloatSpinnerValueFactory;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXSystemeEndiguementPane extends AbstractFXElementPane<SystemeEndiguement> {

    private final Previews previewRepository;
    private LabelMapper labelMapper;
    
    private final DiguePojoTable table = new DiguePojoTable();

    // Propriétés de SystemeEndiguement
    @FXML HTMLEditor ui_commentaire;
    @FXML TextField ui_libelle;
    @FXML Spinner ui_populationProtegee;
    @FXML TextField ui_classement;
    @FXML Spinner ui_niveauProtection;
    @FXML ComboBox ui_gestionnaireDecretId;
    @FXML Button ui_gestionnaireDecretId_link;
    @FXML ComboBox ui_gestionnaireTechniqueId;
    @FXML Button ui_gestionnaireTechniqueId_link;
    @FXML private TabPane tabs;
    @FXML private VBox centerContent;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXSystemeEndiguementPane() {
        SIRS.loadFXML(this, SystemeEndiguement.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);


		/*
		 * Disabling rules.
		 */
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_populationProtegee.disableProperty().bind(disableFieldsProperty());
        ui_populationProtegee.setEditable(true);
        ui_populationProtegee.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        ui_classement.disableProperty().bind(disableFieldsProperty());
        ui_niveauProtection.disableProperty().bind(disableFieldsProperty());
        ui_niveauProtection.setEditable(true);
        ui_niveauProtection.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_gestionnaireDecretId.disableProperty().bind(disableFieldsProperty());
        ui_gestionnaireDecretId_link.disableProperty().bind(ui_gestionnaireDecretId.getSelectionModel().selectedItemProperty().isNull());
        ui_gestionnaireDecretId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_gestionnaireDecretId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_gestionnaireDecretId.getSelectionModel().getSelectedItem()));       
        ui_gestionnaireTechniqueId.disableProperty().bind(disableFieldsProperty());
        ui_gestionnaireTechniqueId_link.disableProperty().bind(ui_gestionnaireTechniqueId.getSelectionModel().selectedItemProperty().isNull());
        ui_gestionnaireTechniqueId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_gestionnaireTechniqueId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_gestionnaireTechniqueId.getSelectionModel().getSelectedItem()));       
    }
    
    public FXSystemeEndiguementPane(final SystemeEndiguement systemeEndiguement){
        this();
        this.elementProperty().set(systemeEndiguement);
        
        table.editableProperty().bind(disableFieldsProperty().not());
        table.parentElementProperty().bind(elementProperty);
        tabs.getTabs().add(new Tab("Digues", table));
    }     

    /**
     * Initialize fields at element setting.
     */
    private void initFields(ObservableValue<? extends SystemeEndiguement > observableElement, SystemeEndiguement oldElement, SystemeEndiguement newElement) {
        // Unbind fields bound to previous element.
        table.setTableItems(()->null);
        if (oldElement != null) {
        // Propriétés de SystemeEndiguement
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());

            ui_populationProtegee.getValueFactory().valueProperty().unbindBidirectional(oldElement.populationProtegeeProperty());
            ui_classement.textProperty().unbindBidirectional(oldElement.classementProperty());

            ui_niveauProtection.getValueFactory().valueProperty().unbindBidirectional(oldElement.niveauProtectionProperty());
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de SystemeEndiguement
        // * commentaire
        ui_commentaire.setHtmlText(newElement.getCommentaire());
        // * libelle
        ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
        // * populationProtegee
        ui_populationProtegee.getValueFactory().valueProperty().bindBidirectional(newElement.populationProtegeeProperty());
        // * classement
        ui_classement.textProperty().bindBidirectional(newElement.classementProperty());
        // * niveauProtection
        ui_niveauProtection.getValueFactory().valueProperty().bindBidirectional(newElement.niveauProtectionProperty());

        table.setTableItems(()->FXCollections.observableArrayList(
                ((DigueRepository) session.getRepositoryForClass(Digue.class)).getBySystemeEndiguement(newElement)));
            
        SIRS.initCombo(ui_gestionnaireDecretId, FXCollections.observableArrayList(
            previewRepository.getByClass(Organisme.class)), 
            newElement.getGestionnaireDecretId() == null? null : previewRepository.get(newElement.getGestionnaireDecretId()));
        SIRS.initCombo(ui_gestionnaireTechniqueId, FXCollections.observableArrayList(
            previewRepository.getByClass(Organisme.class)), 
            newElement.getGestionnaireTechniqueId() == null? null : previewRepository.get(newElement.getGestionnaireTechniqueId()));
    }
    @Override
    public void preSave() {
        final SystemeEndiguement element = (SystemeEndiguement) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());


        Object cbValue;
        cbValue = ui_gestionnaireDecretId.getValue();
        if (cbValue instanceof Preview) {
            element.setGestionnaireDecretId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setGestionnaireDecretId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setGestionnaireDecretId(null);
        }
        cbValue = ui_gestionnaireTechniqueId.getValue();
        if (cbValue instanceof Preview) {
            element.setGestionnaireTechniqueId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setGestionnaireTechniqueId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setGestionnaireTechniqueId(null);
        }
    }
    
    private class DiguePojoTable extends PojoTable {
    
        public DiguePojoTable() {
            super(Digue.class, "Digues du système d'endiguement");
        }

        @Override
        protected Digue createPojo() {
            Digue createdPojo = (Digue) super.createPojo();
            if (elementProperty.get() != null) {
                ((Digue)createdPojo).setSystemeEndiguementId(elementProperty.get().getId());
            }
            return createdPojo;
        }
    }
}
