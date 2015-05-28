package fr.sirs;

import static fr.sirs.SIRS.BORNE_IDS_REFERENCE;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import static fr.sirs.SIRS.COUCH_DB_DOCUMENT_FIELD;
import static fr.sirs.SIRS.DOCUMENT_ID_FIELD;
import static fr.sirs.SIRS.GEOMETRY_FIELD;
import static fr.sirs.SIRS.ID_FIELD;
import fr.sirs.core.component.AbstractSIRSRepository;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.Role;
import fr.sirs.digue.DiguesTab;
import fr.sirs.map.FXMapTab;
import fr.sirs.theme.Theme;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.other.FXDesignationPane;
import fr.sirs.query.FXSearchPane;
import fr.sirs.other.FXReferencePane;
import fr.sirs.other.FXValidationPane;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.FXPreferenceEditor;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.SirsStringConverter;
import org.geotoolkit.gui.javafx.util.ProgressMonitor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

public class FXMainFrame extends BorderPane {

    public static final Image ICON_ALL  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TABLE,16,FontAwesomeIcons.DEFAULT_COLOR),null);
    private final Session session = Injector.getBean(Session.class);
    private final ResourceBundle bundle = ResourceBundle.getBundle(FXMainFrame.class.getName());
    
    @FXML private MenuButton uiThemes;
    @FXML private MenuButton uiPlugins;
    @FXML private TabPane uiTabs;
    @FXML private MenuBar uiMenu;

    private FXMapTab mapTab;
    private DiguesTab diguesTab;
    private Tab searchTab;
    
    private static final String BUNDLE_KEY_ADMINISTATION = "administration";
    private static final String BUNDLE_KEY_USERS = "users";
    private static final String BUNDLE_KEY_VALIDATION = "validation";
    private static final String BUNDLE_KEY_REFERENCES = "references";
    private static final String BUNDLE_KEY_DESIGNATIONS = "designations";
    private static final String BUNDLE_KEY_SEARCH = "search";
    
    /** A cache to get back tab as long as their displayed in application, 
     * allowing fast search on Theme keys.
     */ 
    //private final Cache<Theme, Tab> themeTabs = new Cache<>(12, 1, false);
    
    private Stage prefEditor;

    public FXMainFrame() {
        SIRS.loadFXML(this, FXMainFrame.class);
        
        final ToolBar pm = new ToolBar(new ProgressMonitor(TaskManager.INSTANCE));
        pm.prefHeightProperty().bind(uiMenu.heightProperty());
        ((HBox) uiMenu.getParent()).getChildren().add(pm);
        
        // Load themes
        final Theme[] themes = Plugins.getThemes();
        for(final Theme theme : themes){
            if(theme.getType().equals(Theme.Type.STANDARD)){
                uiThemes.getItems().add(toMenuItem(theme));
            }
            else{
                uiPlugins.getItems().add(toMenuItem(theme));
            }
        }
        
        if(session.getRole()==Role.ADMIN){
            final Menu uiAdmin = new Menu(bundle.getString(BUNDLE_KEY_ADMINISTATION));
            uiMenu.getMenus().add(1, uiAdmin);  
            
            final MenuItem uiUserAdmin = new MenuItem(bundle.getString(BUNDLE_KEY_USERS));
            uiUserAdmin.setOnAction((ActionEvent event) -> {openUsersTab();});
            
            final MenuItem uiValidation = new MenuItem(bundle.getString(BUNDLE_KEY_VALIDATION));
            uiValidation.setOnAction((ActionEvent event) -> {openValidationTab();});
             
            final Menu uiReference = new Menu(bundle.getString(BUNDLE_KEY_REFERENCES));
            for(final Class reference : Session.getReferences()){
                uiReference.getItems().add(toMenuItem(reference, SummaryTab.REFERENCE));
            }
            
            final Menu uiDesignation = new Menu(bundle.getString(BUNDLE_KEY_DESIGNATIONS));
            for(final Class elementClass : Session.getElements()){
                if(!Session.getReferences().contains(elementClass)){
                    uiDesignation.getItems().add(toMenuItem(elementClass, SummaryTab.MODEL));
                }
            }
            
            uiAdmin.getItems().addAll(uiUserAdmin, uiValidation, uiReference, uiDesignation);
        }
        
        SIRS.LOGGER.log(Level.FINE, org.apache.sis.setup.About.configuration().toString());     
    }
    
    public TabPane getUiTabs() {
        return uiTabs;
    }

    public synchronized FXMapTab getMapTab() {
        if(mapTab==null){
            mapTab = new FXMapTab(uiTabs);
        }
        return mapTab;
    }
    
    public synchronized DiguesTab getDiguesTab() {
        if(diguesTab==null){
            diguesTab = new DiguesTab(uiTabs);
        }
        return diguesTab;
    }

    private Tab validationTab;
    private void openValidationTab(){
        if(validationTab==null){
            validationTab = new Tab(bundle.getString(BUNDLE_KEY_VALIDATION));
            validationTab.setContent(new FXValidationPane());
        }
        if(!uiTabs.getTabs().contains(validationTab)){
            uiTabs.getTabs().add(validationTab);
        }
        uiTabs.getSelectionModel().select(validationTab);
    }
        
    public synchronized void addTab(Tab tab){
        if (!uiTabs.equals(tab.getTabPane())) {
            uiTabs.getTabs().add(tab);
        }
        uiTabs.getSelectionModel().select(tab);
    }
    
    private enum SummaryTab{REFERENCE, MODEL};
    private MenuItem toMenuItem(final Class clazz, final SummaryTab typeOfSummary){
        final ResourceBundle bdl = ResourceBundle.getBundle(clazz.getName());
        final MenuItem item = new MenuItem(bdl.getString(BUNDLE_KEY_CLASS));
        final EventHandler<ActionEvent> handler;
        
        if(typeOfSummary==SummaryTab.REFERENCE){
            handler = (ActionEvent event) -> {
                    addTab(Injector.getSession().getOrCreateReferenceTypeTab(clazz));
            };
        }
        else{
            handler = (ActionEvent event) -> {
                    addTab(Injector.getSession().getOrCreateDesignationTab(clazz));
            };
        }
        
        item.setOnAction(handler);
        return item;
    }
    
    @FXML 
    private void clearCache(){
        session.clearCache();
        final Collection<AbstractSIRSRepository> repos = session.getModelRepositories();
        for(final AbstractSIRSRepository repo : repos){
            repo.clearCache();
        }
    }
    
    private MenuItem toMenuItem(final Theme theme) {
        final List<Theme> subs = theme.getSubThemes();
        final MenuItem item;
        // Atomic case
        if (subs.isEmpty()) {
            item = new MenuItem(theme.getName());
            item.setOnAction(new DisplayTheme(theme));
        // container case
        } else {
            item = new Menu(theme.getName());
            //action avec tous les sous-panneaux
            final MenuItem all = new MenuItem("Ouvrir l'ensemble");
            all.setGraphic(new ImageView(ICON_ALL));
            all.setOnAction(new DisplayTheme(theme));
            ((Menu) item).getItems().add(all);

            for (final Theme sub : subs) {
                ((Menu) item).getItems().add(toMenuItem(sub));
            }
        }

        return item;
    }
  
    @FXML
    void openMap(ActionEvent event) {
        getMapTab().show();
    }

    @FXML
    void openDigueTab(ActionEvent event) {
        getDiguesTab().show();
    }

    /**
     * Get or create search tab. If it has been previously closed, we reset it, 
     * so user won't be bothered with an old request.
     * @param event 
     */
    @FXML
    private void openSearchTab(ActionEvent event) {
        if (searchTab == null || !uiTabs.equals(searchTab.getTabPane())) {
            searchTab = new Tab(bundle.getString(BUNDLE_KEY_SEARCH));
            final FXSearchPane searchPane = new FXSearchPane();
            searchTab.setContent(searchPane);
            uiTabs.getTabs().add(searchTab);
        }
        uiTabs.getSelectionModel().select(searchTab);
    }
    
    @FXML
    void openCompte(ActionEvent event){
        if (session.getUtilisateur()!=null) {
            addTab(session.getOrCreateElementTab(session.getUtilisateur()));
        }
    }
    
    @FXML
    void openPref(ActionEvent event) {
        if (prefEditor == null) {
            prefEditor = new FXPreferenceEditor();            
        }
        prefEditor.show();
    }

    @FXML
    void exit(ActionEvent event) {
        System.exit(0);
    }
    
    @FXML 
    void deconnect(ActionEvent event) throws IOException{
        this.getScene().getWindow().hide();
        session.setUtilisateur(null);
        session.clearCache();
        session.getTaskManager().reset();
        if(SIRS.getLauncher()!=null){
            session.getApplicationContext().close();
            SIRS.getLauncher().show();
        } else {
            SIRS.LOADER.showSplashStage();
        }
    }
    
    @FXML 
    void changeUser(ActionEvent event) throws IOException{
        this.getScene().getWindow().hide();
        session.setUtilisateur(null);
        session.clearCache();
        session.getTaskManager().reset();
        SIRS.LOADER.showSplashStage();
    }
    
    @FXML
    private void print() throws Exception {

        final Thread t = new Thread() {
            @Override
            public void run() {
                if(session.getElementsToPrint()!=null){
                    printElements();
                } else if(session.getFeaturesToPrint()!=null){
                    printFeatures();
                }
            }
        };
        t.start();
    }
    
    private void printFeatures(){
        try {
            final File fileToPrint = PrinterUtilities.print(session.getFeaturesToPrint(), null);
            fileToPrint.deleteOnExit();
            Desktop.getDesktop().open(fileToPrint);
        } catch (Exception ex) {
            Logger.getLogger(FXMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void printElements(){
        for(final Element element : session.getElementsToPrint()){
                    
            final File fileToPrint;
            final List avoidFields = new ArrayList<>();
            avoidFields.add(GEOMETRY_FIELD);
            avoidFields.add(DOCUMENT_ID_FIELD);
            avoidFields.add(ID_FIELD);

            if(element instanceof TronconDigue){
                avoidFields.add(BORNE_IDS_REFERENCE);
            }

            if(element instanceof Element){
                avoidFields.add(COUCH_DB_DOCUMENT_FIELD);
            }

            try {
                fileToPrint = PrinterUtilities.print(element, avoidFields, session.getPreviews(), new SirsStringConverter());
                fileToPrint.deleteOnExit();
                Desktop.getDesktop().open(fileToPrint);
            } catch (Exception e) {
                Logger.getLogger(FXMainFrame.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    @FXML
    public void openAppInfo() {
        final Stage infoStage = new Stage();
        infoStage.setTitle("À propos");
        infoStage.initStyle(StageStyle.UTILITY);
        infoStage.setScene(new Scene(new FXAboutPane()));
        infoStage.setResizable(false);
        infoStage.show();
    }
    
    private void openUsersTab(){
        final Tab usersTab = new Tab(bundle.getString(BUNDLE_KEY_USERS));
        
        final PojoTable usersTable = new PojoTable(session.getUtilisateurRepository(), "Table des utilisateurs"){
            @Override
            protected void deletePojos(final Element... pojos) {
                final List<Element> pojoList = new ArrayList<>();
                for (final Element pojo : pojos) {
                    if(pojo instanceof Utilisateur){
                        final Utilisateur utilisateur = (Utilisateur) pojo;
                        // On interdit la suppression de l'utilisateur courant !
                        if(utilisateur.equals(session.getUtilisateur())){
                            new Alert(Alert.AlertType.ERROR, "Vous ne pouvez pas supprimer votre propre compte.", ButtonType.CLOSE).showAndWait();                       
                        } 
                        // On interdit également la suppression de l'invité par défaut !
                        else if (SIRS.LOGIN_DEFAULT_GUEST.equals(utilisateur.getLogin())){
                            new Alert(Alert.AlertType.ERROR, "Vous ne pouvez pas supprimer le compte de l'invité par défaut.", ButtonType.CLOSE).showAndWait();
                        }
                        else{
                            pojoList.add(pojo);
                        }
                    }
                }
                super.deletePojos(pojoList.toArray(new Element[0]));
            }
        };
        usersTable.cellEditableProperty().unbind();
        usersTable.cellEditableProperty().set(false);
        usersTab.setContent(usersTable);
        addTab(usersTab);
    }
    
    private class DisplayTheme implements EventHandler<ActionEvent> {

        private final Theme theme;

        public DisplayTheme(Theme theme) {
            this.theme = theme;
        }

        @Override
        public void handle(ActionEvent event) {
            final Tab result = Injector.getSession().getOrCreateThemeTab(theme);
            addTab(result);
        }
    }
}
