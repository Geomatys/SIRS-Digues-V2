
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.AbstractTheme.ThemeManager;
import fr.sirs.theme.TronconTheme;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXTronconLitPane extends AbstractFXElementPane<TronconLit> {

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;
    
    @FXML private FXValidityPeriodPane uiValidityPeriod;

    // Propriétés de TronconLit
    @FXML protected ComboBox ui_litId;
    @FXML protected Button ui_litId_link;

    // Propriétés de AvecGeometrie

    // Propriétés de TronconDigue
    @FXML protected TextField ui_libelle;
    @FXML protected HTMLEditor ui_commentaire;
    @FXML protected ComboBox ui_digueId;
    @FXML protected Button ui_digueId_link;
    @FXML protected ComboBox ui_typeRiveId;
    @FXML protected Button ui_typeRiveId_link;
    @FXML protected ComboBox ui_typeTronconId;
    @FXML protected Button ui_typeTronconId_link;
    @FXML protected ComboBox ui_systemeRepDefautId;
    @FXML protected Button ui_systemeRepDefautId_link;
    @FXML protected Tab ui_borneIds;
    protected final ListeningPojoTable borneIdsTable;
    @FXML protected Tab ui_gestions;
    @FXML protected ListView ui_LeftList;
    @FXML protected VBox ui_mainBox;
    @FXML protected BorderPane ui_centerPane;
    
    protected final PojoTable gestionsTable;

    /**
     * Constructor. Initialize part of the UI which will not require update when 
     * element edited change.
     */
    protected FXTronconLitPane() {
        SIRS.loadFXML(this, TronconLit.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

        /*
         * Disabling rules.
         */
        ui_litId.disableProperty().bind(disableFieldsProperty());
        ui_litId_link.disableProperty().bind(ui_litId.getSelectionModel().selectedItemProperty().isNull());
        ui_litId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_litId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_litId.getSelectionModel().getSelectedItem()));       
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_digueId.disableProperty().bind(disableFieldsProperty());
        ui_digueId_link.disableProperty().bind(ui_digueId.getSelectionModel().selectedItemProperty().isNull());
        ui_digueId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_digueId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_digueId.getSelectionModel().getSelectedItem()));       
        ui_typeRiveId.disableProperty().bind(disableFieldsProperty());
        ui_typeRiveId_link.setVisible(false);
        ui_typeTronconId.disableProperty().bind(disableFieldsProperty());
        ui_typeTronconId_link.setVisible(false);
        ui_systemeRepDefautId.disableProperty().bind(disableFieldsProperty());
        ui_systemeRepDefautId_link.disableProperty().bind(ui_systemeRepDefautId.getSelectionModel().selectedItemProperty().isNull());
        ui_systemeRepDefautId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_systemeRepDefautId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_systemeRepDefautId.getSelectionModel().getSelectedItem()));       
        borneIdsTable = new ListeningPojoTable(BorneDigue.class, null);
        borneIdsTable.editableProperty().bind(disableFieldsProperty().not());
        borneIdsTable.createNewProperty().set(false);
        ui_borneIds.setContent(borneIdsTable);
        ui_borneIds.setClosable(false);
        gestionsTable = new PojoTable(GestionTroncon.class, null);
        gestionsTable.editableProperty().bind(disableFieldsProperty().not());
        ui_gestions.setContent(gestionsTable);
        ui_gestions.setClosable(false);
    }
    
    public FXTronconLitPane(final TronconLit tronconLit){
        this();
        this.elementProperty().set(tronconLit);
        borneIdsTable.setObservableListToListen(elementProperty.get().getBorneIds());
    }     

    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends TronconLit > observableElement, TronconLit oldElement, TronconLit newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de TronconLit
        // Propriétés de AvecGeometrie
        // Propriétés de TronconDigue
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de TronconLit
        SIRS.initCombo(ui_litId, FXCollections.observableList(
            previewRepository.getByClass(Lit.class)), 
            newElement.getLitId() == null? null : previewRepository.get(newElement.getLitId()));
        // Propriétés de AvecGeometrie
        // Propriétés de TronconDigue
        // * libelle
        ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
        // * commentaire
        ui_commentaire.setHtmlText(newElement.getCommentaire());
        SIRS.initCombo(ui_digueId, FXCollections.observableList(
            previewRepository.getByClass(Digue.class)), 
            newElement.getDigueId() == null? null : previewRepository.get(newElement.getDigueId()));
        SIRS.initCombo(ui_typeRiveId, FXCollections.observableList(
            previewRepository.getByClass(RefRive.class)), 
            newElement.getTypeRiveId() == null? null : previewRepository.get(newElement.getTypeRiveId()));
        SIRS.initCombo(ui_typeTronconId, FXCollections.observableList(
            previewRepository.getByClass(RefTypeTroncon.class)), 
            newElement.getTypeTronconId() == null? null : previewRepository.get(newElement.getTypeTronconId()));
        SIRS.initCombo(ui_systemeRepDefautId, FXCollections.observableList(
            previewRepository.getByClass(SystemeReperage.class)), 
            newElement.getSystemeRepDefautId() == null? null : previewRepository.get(newElement.getSystemeRepDefautId()));
        borneIdsTable.setParentElement(null);
        final AbstractSIRSRepository<BorneDigue> borneIdsRepo = session.getRepositoryForClass(BorneDigue.class);
        borneIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getBorneIds(), borneIdsRepo));
        gestionsTable.setParentElement(newElement);
        gestionsTable.setTableItems(()-> (ObservableList) newElement.getGestions());
        
        final List<String> items = new ArrayList<>();
        items.add("Description générale");
        items.add("Ouvrages associés");
        items.add("Type d'occupation riveraine");
        items.add("Pente moyenne");
        items.add("Largeur moyenne");
        items.add("Type d'écoulement");
        items.add("Domanialité");
        items.add("Zone d'atterrissement");
        ui_LeftList.setItems(FXCollections.observableList(items));
        ui_LeftList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue.equals("Description générale")) {
                    ui_centerPane.setCenter(ui_mainBox);
                } else if (newValue.equals("Ouvrages associés")) {
                    ThemeManager manager = AbstractTheme.generateThemeManager("Tableau des ouvrages associés", OuvrageAssocieLit.class);
                    final LitThemePojoTable table = new LitThemePojoTable(manager);
                    table.setDeletor(manager.getDeletor());
                    table.setForeignParentId(newElement.getId());
                    ui_centerPane.setCenter(table);
                } else if (newValue.equals("Type d'occupation riveraine")) {
                    ThemeManager manager = AbstractTheme.generateThemeManager("Tableau des occupations riveraines", OccupationRiveraineLit.class);
                    final LitThemePojoTable table = new LitThemePojoTable(manager);
                    table.setDeletor(manager.getDeletor());
                    table.setForeignParentId(newElement.getId());
                    ui_centerPane.setCenter(table);
                } else if (newValue.equals("Pente moyenne")) {
                    ThemeManager manager = AbstractTheme.generateThemeManager("Tableau des pentes", PenteLit.class);
                    final LitThemePojoTable table = new LitThemePojoTable(manager);
                    table.setDeletor(manager.getDeletor());
                    table.setForeignParentId(newElement.getId());
                    ui_centerPane.setCenter(table);
                } else if (newValue.equals("Largeur moyenne")) {
                    ThemeManager manager = AbstractTheme.generateThemeManager("Tableau des largeurs", LargeurLit.class);
                    final LitThemePojoTable table = new LitThemePojoTable(manager);
                    table.setDeletor(manager.getDeletor());
                    table.setForeignParentId(newElement.getId());
                    ui_centerPane.setCenter(table);
                } else if (newValue.equals("Type d'écoulement")) {
                    ThemeManager manager = AbstractTheme.generateThemeManager("Tableau des régimes d'écoulement", RegimeEcoulementLit.class);
                    final LitThemePojoTable table = new LitThemePojoTable(manager);
                    table.setDeletor(manager.getDeletor());
                    table.setForeignParentId(newElement.getId());
                    ui_centerPane.setCenter(table);
                } else if (newValue.equals("Domanialité")) {
                    ThemeManager manager = AbstractTheme.generateThemeManager("Tableau des domanialité", DomanialiteLit.class);
                    final LitThemePojoTable table = new LitThemePojoTable(manager);
                    table.setDeletor(manager.getDeletor());
                    table.setForeignParentId(newElement.getId());
                    ui_centerPane.setCenter(table);
                } else if (newValue.equals("Zone d'atterrissement")) {
                    ThemeManager manager = AbstractTheme.generateThemeManager("Tableau des zones d'atterrissement", ZoneAtterrissementLit.class);
                    final LitThemePojoTable table = new LitThemePojoTable(manager);
                    table.setDeletor(manager.getDeletor());
                    table.setForeignParentId(newElement.getId());
                    ui_centerPane.setCenter(table);
                }
            }
        });
    }
    
    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final TronconLit element = (TronconLit) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());


        Object cbValue;
        cbValue = ui_litId.getValue();
        if (cbValue instanceof Preview) {
            element.setLitId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setLitId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setLitId(null);
        }
        cbValue = ui_digueId.getValue();
        if (cbValue instanceof Preview) {
            element.setDigueId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setDigueId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setDigueId(null);
        }
        cbValue = ui_typeRiveId.getValue();
        if (cbValue instanceof Preview) {
            element.setTypeRiveId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypeRiveId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypeRiveId(null);
        }
        cbValue = ui_typeTronconId.getValue();
        if (cbValue instanceof Preview) {
            element.setTypeTronconId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypeTronconId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypeTronconId(null);
        }
        cbValue = ui_systemeRepDefautId.getValue();
        if (cbValue instanceof Preview) {
            element.setSystemeRepDefautId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setSystemeRepDefautId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setSystemeRepDefautId(null);
        }
        // Manage opposite references for BorneDigue...
        final List<String> currentBorneDigueIdsList = new ArrayList<>();
        for(final Element elt : borneIdsTable.getAllValues()){
            final BorneDigue borneDigue = (BorneDigue) elt;
            currentBorneDigueIdsList.add(borneDigue.getId());
        }
        element.setBorneIds(currentBorneDigueIdsList);
        
    }
    
    protected class LitThemePojoTable<T extends AvecForeignParent> extends ForeignParentPojoTable<T>{

        private final TronconTheme.ThemeManager<T> group;

        public LitThemePojoTable(TronconTheme.ThemeManager<T> group) {
            super(group.getDataClass(), group.getTableTitle());
            foreignParentIdProperty.addListener(this::updateTable);
            this.group = group;
        }

        private void updateTable(ObservableValue<? extends String> observable, String oldValue, String newValue){
            if(newValue==null || group==null) {
                setTableItems(FXCollections::emptyObservableList);
            } else {
                //JavaFX bug : sortable is not possible on filtered list
                // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
                // https://javafx-jira.kenai.com/browse/RT-32091
//                setTableItems(() -> {
//                    final SortedList<T> sortedList = new SortedList<>(group.getExtractor().apply(newValue));
//                    sortedList.comparatorProperty().bind(getUiTable().comparatorProperty());
//                    return sortedList;
//                });
                setTableItems(() -> (ObservableList) group.getExtractor().apply(newValue));
            }
        }
        
        public BooleanProperty getEditableProperty() {
            return editableProperty;
        }
    }
}