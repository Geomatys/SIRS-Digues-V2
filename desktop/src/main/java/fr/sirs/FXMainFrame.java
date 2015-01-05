package fr.sirs;

import fr.sirs.digue.DiguesTab;
import fr.sirs.map.FXMapTab;
import fr.sirs.theme.Theme;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.map.FXMapPane;
import org.geotoolkit.owc.xml.OwcXmlIO;
import fr.sirs.query.FXSearchPane;
import fr.sirs.theme.ui.FXReferencesPane;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.FXPreferenceEditor;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import javax.xml.bind.JAXBException;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.opengis.util.FactoryException;

public class FXMainFrame extends BorderPane {

    public static final Image ICON_ALL  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TABLE,16,FontAwesomeIcons.DEFAULT_COLOR),null);
    private final Session session = Injector.getBean(Session.class);
    
    @FXML private MenuItem uiPref;
    @FXML private MenuButton uiThemes;
    @FXML private MenuButton uiPlugins;
    @FXML private TabPane uiTabs;
    @FXML private MenuItem uiExit;
    @FXML private Button uiMapButton;
    @FXML private Button uiPrintButton;
    @FXML private MenuItem uiDeconnect;
    @FXML private MenuBar uiMenu;

    private FXMapTab mapTab;
    private DiguesTab diguesTab;
    
    private Stage prefEditor;

    public FXMainFrame() {
        SIRS.loadFXML(this);
        
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
            final Menu uiAdmin = new Menu("Administration");
            final MenuItem uiUserAdmin = new MenuItem("Utilisateurs");
            uiUserAdmin.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    openUsersTab();
                }
            });
            final MenuItem uiDocsAdmin = new MenuItem("Validation");
            uiDocsAdmin.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    openDocsTab();
                }
            });
            final MenuItem uiSaveContext = new MenuItem("Sauvegarder le contexte");
            uiSaveContext.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    saveContext();
                }
            });
            final MenuItem uiLoadContext = new MenuItem("Charger le contexte");
            uiLoadContext.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    loadContext();
                }
            });
            uiAdmin.getItems().addAll(uiUserAdmin, uiDocsAdmin, uiSaveContext, uiLoadContext);
            uiMenu.getMenus().add(1, uiAdmin);
        }
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
        uiTabs.getTabs().add(tab);
        final int index = uiTabs.getTabs().indexOf(tab);
        uiTabs.getSelectionModel().clearAndSelect(index);
    }
    
    private MenuItem toMenuItem(final Theme theme){
        
        final List<Theme> subs = theme.getSubThemes();
        final MenuItem item;
        
        if(subs.isEmpty()){
            item = new MenuItem(theme.getName());
            item.setOnAction((ActionEvent event) -> {
                final Tab tab = new FXFreeTab(theme.getName());
                tab.setContent(theme.createPane());
                addTab(tab);
            });
        } else{
            item = new Menu(theme.getName());
            //action avec tous les sous panneaux
            final MenuItem all = new MenuItem("Ouvrir l'ensemble");
            all.setGraphic(new ImageView(ICON_ALL));
            all.setOnAction((ActionEvent event) -> {
                final Tab tab = new FXFreeTab(theme.getName());
                tab.setContent(theme.createPane());
                addTab(tab);
            });
            ((Menu) item).getItems().add(all);
            
            for(final Theme sub : subs){
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

    @FXML
    void openSearchTab(ActionEvent event) {
        final Tab tab = new Tab("Recherche");
        final FXSearchPane pane = new FXSearchPane();
        tab.setContent(pane);
        uiTabs.getTabs().add(tab);
    }
    
    @FXML
    void openPref(ActionEvent event) {
        if (prefEditor == null) {
            prefEditor = new FXPreferenceEditor();            
        }
        prefEditor.show();
    }
    
    @FXML
    void openRefs(ActionEvent event){
        final FXReferencesPane referencesPane = new FXReferencesPane();
        final Tab tab = new FXFreeTab("Références");
        tab.setContent(referencesPane);
        addTab(tab);
    }

    @FXML
    void exit(ActionEvent event) {
        System.exit(0);
    }
    
    @FXML void deconnect(ActionEvent event) throws IOException{
        this.getScene().getWindow().hide();
        session.setUtilisateur(null);
        final Loader loader = new Loader();
        loader.showLoginStage();
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
                    System.out.println(e);
                    Logger.getLogger(FXMainFrame.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        };
        t.start();
    }
    
    private void openUsersTab(){
        final Tab usersTab = new Tab("Utilisateurs");
        
        final PojoTable usersTable = new PojoTable(session.getUtilisateurRepository(), "Table des utilisateurs");
        usersTab.setContent(usersTable);
        addTab(usersTab);
    }
    
    private void saveContext(){
        try {
            OwcXmlIO.write(new FileOutputStream(new File("src/main/resources/saveContext.owc")), getMapTab().getMap().getMapContext());
        } catch (FileNotFoundException | JAXBException | FactoryException ex) {
            Logger.getLogger(FXMapPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void loadContext(){
        try {
            getMapTab().getMap().setMapContext(OwcXmlIO.read(new File("src/main/resources/saveContext.owc")));
        } catch (JAXBException | FactoryException | DataStoreException ex) {
            Logger.getLogger(FXMapPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void openDocsTab(){
        final Tab docsTab = new Tab("Validation");
//        final PojoTable docsTable = new PojoTable(session.getPreviewLabelRepository(), "Table des documents");
        addTab(docsTab);
    }
}
