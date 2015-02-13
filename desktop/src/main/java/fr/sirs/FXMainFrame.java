package fr.sirs;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.Role;
import fr.sirs.digue.DiguesTab;
import fr.sirs.map.FXMapTab;
import fr.sirs.theme.Theme;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.query.FXSearchPane;
import fr.sirs.other.FXReferencePane;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.FXPreferenceEditor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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
//    @FXML private Menu uiRefs;
//    @FXML private Menu uiRefsList;

    private FXMapTab mapTab;
    private DiguesTab diguesTab;
    private Tab searchTab;
    
    /** A cache to get back tab as long as their displayed in application, 
     * allowing fast search on Theme keys.
     */ 
    //private final Cache<Theme, Tab> themeTabs = new Cache<>(12, 1, false);
    
    private Stage prefEditor;

    public FXMainFrame() {
        SIRS.loadFXML(this, FXMainFrame.class);
        
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
            final Menu uiAdmin = new Menu(bundle.getString("administration"));
            final MenuItem uiUserAdmin = new MenuItem(bundle.getString("utilisateurs"));
            uiUserAdmin.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    openUsersTab();
                }
            });
            final MenuItem uiDocsAdmin = new MenuItem(bundle.getString("validation"));
            uiDocsAdmin.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    openDocsTab();
                }
            });
            
            uiAdmin.getItems().addAll(uiUserAdmin, uiDocsAdmin);
            uiMenu.getMenus().add(1, uiAdmin);
            
            
            final Menu uiRefs = new Menu(bundle.getString("references"));
            final Menu uiRefsList = new Menu(bundle.getString("listeReferences"));
            uiRefs.getItems().add(uiRefsList);
        
            // Load references
            for(final Class reference : Session.getReferences()){
                uiRefsList.getItems().add(toMenuItem(reference));
            }
            
            uiAdmin.getItems().add(uiRefs);
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
    
    private MenuItem toMenuItem(final Class reference){
        final ResourceBundle bundle = ResourceBundle.getBundle(reference.getName());
        final MenuItem item = new MenuItem(bundle.getString("class"));
        item.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                final FXReferencePane referencesPane = new FXReferencePane(reference);
                final Tab tab = new FXFreeTab(bundle.getString("class"));
                tab.setContent(referencesPane);
                addTab(tab);
            }
        });
        return item;
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
            searchTab = new Tab(bundle.getString("search"));
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
        SIRS.LOADER.showSplashStage();
    }
    
    @FXML
    public void print() throws Exception {

        final Thread t = new Thread() {
            @Override
            public void run() {

                final Object obj = session.getObjectToPrint();
                final File fileToPrint;
                final List avoidFields = new ArrayList<>();
                avoidFields.add("geometry");
                avoidFields.add("documentId");
                
                if(obj instanceof TronconDigue){
                    avoidFields.add("stuctures");
                    avoidFields.add("borneIds");
                }
                
                try {
                    fileToPrint = PrinterUtilities.print(obj, avoidFields);
                    fileToPrint.deleteOnExit();

                    final Desktop desktop = Desktop.getDesktop();
                    desktop.open(fileToPrint);
                } catch (Exception e) {
                    Logger.getLogger(FXMainFrame.class.getName()).log(Level.SEVERE, null, e);
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
        final Tab usersTab = new Tab(bundle.getString("utilisateurs"));
        
        final PojoTable usersTable = new PojoTable(session.getUtilisateurRepository(), "Table des utilisateurs"){
            @Override
            protected void deletePojos(final Element... pojos) {
                final List<Element> pojoList = new ArrayList<>();
                for (final Element pojo : pojos) {
                    if(pojo.equals(session.getUtilisateur())){
                        new Alert(Alert.AlertType.ERROR, "Vous ne pouvez pas supprimer votre propre compte.", ButtonType.CLOSE).showAndWait();                       
                    }
                    else{
                        pojoList.add(pojo);
                    }
                }
                super.deletePojos(pojoList.toArray(new Element[0]));
            }
        };
        usersTab.setContent(usersTable);
        addTab(usersTab);
    }

    private void openDocsTab(){
        final Tab docsTab = new Tab("Validation");
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
