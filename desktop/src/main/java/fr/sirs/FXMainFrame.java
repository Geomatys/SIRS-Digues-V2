package fr.sirs;

import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import fr.sirs.core.component.AbstractSIRSRepository;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Role;
import fr.sirs.digue.DiguesTab;
import fr.sirs.map.FXMapTab;
import fr.sirs.theme.Theme;
import fr.sirs.util.PrinterUtilitiesElement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.other.FXDoubleDesignationPane;
import fr.sirs.query.FXSearchPane;
import fr.sirs.other.FXReferencePane;
import fr.sirs.other.FXValidationPane;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.FXPreferenceEditor;
import fr.sirs.util.SirsStringConverter;
import org.geotoolkit.gui.javafx.util.ProgressMonitor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            uiUserAdmin.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    openUsersTab();
                }
            });
            
            final MenuItem uiDocsAdmin = new MenuItem(bundle.getString(BUNDLE_KEY_VALIDATION));
            uiDocsAdmin.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    openDocsTab();
                }
            });
             
            final Menu uiRefs = new Menu(bundle.getString(BUNDLE_KEY_REFERENCES));
        
            // Load references
            for(final Class reference : Session.getReferences()){
                uiRefs.getItems().add(toMenuItem(reference, SummaryTab.REFERENCE));
            }
            
            final Menu uiPseudoId = new Menu(bundle.getString(BUNDLE_KEY_DESIGNATIONS));
            for(final Class elementClass : Session.getElements()){
                if(!Session.getReferences().contains(elementClass)){
                    uiPseudoId.getItems().add(toMenuItem(elementClass, SummaryTab.MODEL));
                }
            }
            
            uiAdmin.getItems().addAll(uiUserAdmin, uiDocsAdmin, uiRefs, uiPseudoId);
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
        final EventHandler handler;
        
        if(typeOfSummary==SummaryTab.REFERENCE){
            handler = new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    final Tab tab = new FXFreeTab(bdl.getString(BUNDLE_KEY_CLASS));
                    tab.setContent(new FXReferencePane(clazz));
                    addTab(tab);
                }
            };
        }
        else{
            handler = new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    final Tab tab = new FXFreeTab(bdl.getString(BUNDLE_KEY_CLASS));
                    tab.setContent(new FXDoubleDesignationPane(clazz));
                    addTab(tab);
                }
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
    public void print() throws Exception {

        final Thread t = new Thread() {
            @Override
            public void run() {

                for(final Element obj : session.getObjectToPrint()){
                    
                    final File fileToPrint;
                    final List avoidFields = new ArrayList<>();
                    avoidFields.add("geometry");
                    avoidFields.add("documentId");
                    avoidFields.add("id");

                    if(obj instanceof TronconDigue){
                        avoidFields.add("stuctures");
                        avoidFields.add("borneIds");
                    }

                    if(obj instanceof Element){
                        avoidFields.add("couchDBDocument");
                    }

                    try {
                        fileToPrint = PrinterUtilitiesElement.print(obj, avoidFields, session.getPreviewLabelRepository(), new SirsStringConverter());
                        fileToPrint.deleteOnExit();

                        final Desktop desktop = Desktop.getDesktop();
                        desktop.open(fileToPrint);
                    } catch (Exception e) {
                        Logger.getLogger(FXMainFrame.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        };
        t.start();
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
                        else if ("".equals(utilisateur.getLogin())){
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

    private void openDocsTab(){
        final Tab docsTab = new Tab(bundle.getString(BUNDLE_KEY_VALIDATION));
        docsTab.setContent(new FXValidationPane());
//        final PojoTable docsTable = new PojoTable(session.getPreviewLabelRepository(), "Table des documents");
        addTab(docsTab);
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
