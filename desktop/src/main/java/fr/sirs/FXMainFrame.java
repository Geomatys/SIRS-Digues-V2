package fr.sirs;

import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.ui.AlertItem;
import fr.sirs.ui.AlertManager;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.Role;
import fr.sirs.digue.DiguesTab;
import fr.sirs.map.FXMapTab;
import fr.sirs.theme.Theme;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.query.FXSearchPane;
import fr.sirs.util.FXPreferenceEditor;
import org.geotoolkit.gui.javafx.util.ProgressMonitor;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
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
    public static final Image ICON_ALERT  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_BELL,16,FontAwesomeIcons.DEFAULT_COLOR),null);
    private final Session session = Injector.getBean(Session.class);
    private final ResourceBundle bundle = ResourceBundle.getBundle(FXMainFrame.class.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
    
    @FXML private MenuButton uiThemesLocalized;
    @FXML private MenuButton uiThemesUnlocalized;
    @FXML private MenuButton uiPlugins;
    @FXML private Button uiAlertsBtn;
    @FXML private ImageView uiPluginsImg;
    @FXML private ToolBar uiToolBarPlugins;
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

    private static final String CSS_POPUP_ALERTS = "popup-alerts";
    private static final String CSS_POPUP_RAPPEL_TITLE = "popup-alerts-rappel-title";
    
    /** 
     * A cache to get back tab as long as their displayed in application, 
     * allowing fast search on Theme keys.
     */
    
    private Stage prefEditor;

    public FXMainFrame() {
        SIRS.loadFXML(this, FXMainFrame.class);
        
        final ToolBar pm = new ToolBar(new ProgressMonitor(TaskManager.INSTANCE));
        pm.prefHeightProperty().bind(uiMenu.heightProperty());
        ((HBox) uiMenu.getParent()).getChildren().add(pm);
        
        // Load themes
        final Theme[] themes = Plugins.getThemes();
        for(final Theme theme : themes){
            if (Theme.Type.LOCALIZED.equals(theme.getType())) {
                uiThemesLocalized.getItems().add(toMenuItem(theme));
            } else if (Theme.Type.UNLOCALIZED.equals(theme.getType())) {
                uiThemesUnlocalized.getItems().add(toMenuItem(theme));
            }
        }

        // Load plugins
        for (Plugin plugin : Plugins.getPlugins()) {
            if (plugin.name.equals(CorePlugin.NAME)) {
                continue;
            }
            uiPlugins.getItems().add(toMenuItem(plugin));
        }
        uiPlugins.visibleProperty().bind(Bindings.isNotEmpty(uiPlugins.getItems()));
        uiPlugins.managedProperty().bind(uiPlugins.visibleProperty());

        final Menu uiAdmin = new Menu(bundle.getString(BUNDLE_KEY_ADMINISTATION));
        uiMenu.getMenus().add(1, uiAdmin);

        final MenuItem uiUserAdmin = new MenuItem(bundle.getString(BUNDLE_KEY_USERS));
        uiUserAdmin.setOnAction((ActionEvent event) -> {
            addTab(Injector.getSession().getOrCreateAdminTab(Session.AdminTab.USERS, bundle.getString(BUNDLE_KEY_USERS)));
        });

        final MenuItem uiValidation = new MenuItem(bundle.getString(BUNDLE_KEY_VALIDATION));
        uiValidation.setOnAction((ActionEvent event) -> {
            addTab(Injector.getSession().getOrCreateAdminTab(Session.AdminTab.VALIDATION, bundle.getString(BUNDLE_KEY_VALIDATION)));
        });

        final Menu uiReference = new Menu(bundle.getString(BUNDLE_KEY_REFERENCES));
        for (final Class reference : Session.getReferences()) {
            uiReference.getItems().add(toMenuItem(reference, Choice.REFERENCE));
        }

        final Menu uiDesignation = new Menu(bundle.getString(BUNDLE_KEY_DESIGNATIONS));
        for (final Class elementClass : Session.getElements()) {
            if (!Session.getReferences().contains(elementClass)) {
                uiDesignation.getItems().add(toMenuItem(elementClass, Choice.MODEL));
            }
        }
        
        uiAdmin.getItems().addAll(uiUserAdmin, uiValidation, uiReference, uiDesignation);
        uiAdmin.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            Utilisateur user = session.utilisateurProperty().get();
            if (user != null && Role.ADMIN.equals(user.getRole())) {
                return true;
            }
            return false;
        }, session.utilisateurProperty()));

        final ObservableList<AlertItem> alerts = AlertManager.getInstance().getAlerts();
        uiAlertsBtn.setOnMouseClicked(event -> showAlertsPopup());
        uiAlertsBtn.setGraphic(new ImageView(ICON_ALERT));
        uiAlertsBtn.setText(alerts.size() +" alerte(s)");

        SIRS.LOGGER.log(Level.FINE, org.apache.sis.setup.About.configuration().toString());
    }

    public void showAlertsPopup() {
        final ObservableList<AlertItem> alerts = AlertManager.getInstance().getAlerts();
        final Popup popup = new Popup();
        final VBox vbox = new VBox();
        vbox.getStylesheets().add(SIRS.CSS_PATH);
        vbox.getStyleClass().add(CSS_POPUP_ALERTS);
        for (final AlertItem alert : alerts) {
            final Label label = new Label(alert.getTitle());
            label.getStyleClass().add(CSS_POPUP_RAPPEL_TITLE);
            vbox.getChildren().add(label);
            vbox.getChildren().add(new Label("Echéance "+ alert.getDate().toString()));
        }

        popup.getContent().add(vbox);
        popup.setAutoHide(true);

        final Bounds bounds = uiAlertsBtn.getBoundsInLocal();
        popup.show(uiAlertsBtn, bounds.getMinX(), bounds.getMinY() - popup.getHeight() - 30);
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
        
    public final synchronized void addTab(Tab tab){
        if (!uiTabs.equals(tab.getTabPane())) {
            uiTabs.getTabs().add(tab);
        }
        uiTabs.getSelectionModel().select(tab);
    }

    private enum Choice{REFERENCE, MODEL};
    private MenuItem toMenuItem(final Class clazz, final Choice typeOfSummary){
        final ResourceBundle bdl = ResourceBundle.getBundle(clazz.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
        final MenuItem item = new MenuItem(bdl.getString(BUNDLE_KEY_CLASS));
        final EventHandler<ActionEvent> handler;
        
        if(typeOfSummary==Choice.REFERENCE){
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

    /**
     * Génère le menu déroulant pour le plugin donné.
     *
     * @param plugin Plugin à montrer.
     * @return
     */
    private MenuItem toMenuItem(final Plugin plugin) {
        final MenuItem item = new MenuItem(plugin.getTitle().toString());
        if (plugin.getImage() != null) {
            // Une image a été fournie pour le plugin, elle remplacera l'icône générale des modules.
            uiPluginsImg.setImage(plugin.getImage());
        } else {
            // chargement de l'image par défaut
            uiPluginsImg.setImage(new Image(this.getClass().getResourceAsStream("images/menu-modules.png")));
        }
        item.setOnAction(event -> {
            uiPlugins.setText(plugin.getTitle().toString());
            uiToolBarPlugins.getItems().clear();
            for (Theme theme : plugin.getThemes()) {
                uiToolBarPlugins.getItems().add(toButton((AbstractPluginsButtonTheme) theme));
            }
        });
        return item;
    }

    /**
     * Créé un bouton pour représenter le sous menu d'un plugin.
     *
     * @param theme
     * @return
     */
    private ButtonBase toButton(final AbstractPluginsButtonTheme theme) {
        final ButtonBase button;
        if (theme.getSubThemes() != null && !theme.getSubThemes().isEmpty()) {
            button = new MenuButton(theme.getName());
            for (final Theme t : theme.getSubThemes()) {
                ((MenuButton) button).getItems().add(toMenuItem(t));
            }
        } else {
            button = new Button(theme.getName());
            button.setOnAction(new DisplayTheme(theme));
        }

        if (theme.getDescription() != null && !theme.getDescription().isEmpty()) {
            button.setTooltip(new Tooltip(theme.getDescription()));
        }
        if (theme.getImg() != null) {
            final ImageView imageView = new ImageView();
            imageView.setFitHeight(40);
            imageView.setFitWidth(40);
            imageView.setImage(theme.getImg());
            button.setGraphic(imageView);
        }
        // Button style
        button.setTextOverrun(OverrunStyle.CLIP);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setWrapText(true);
        button.setMinWidth(USE_PREF_SIZE);
        button.setMinHeight(USE_COMPUTED_SIZE);
        button.setPrefWidth(115);
        button.setPrefHeight(USE_COMPUTED_SIZE);
        button.setMaxWidth(USE_PREF_SIZE);
        button.setMaxHeight(USE_COMPUTED_SIZE);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setAlignment(Pos.CENTER);
        button.getStyleClass().add("buttonbar-button");

        return button;
    }

    /**
     * Créé un item de menu et son arborescence pour le thème choisi.
     *
     * @param theme
     * @return
     */
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
        if (SIRS.getLauncher() != null) {
            SIRS.getLauncher().restart();
        } else {
            // Ne devrait pas arriver car on a toujours une instance de Stage à la création.
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
                if(session.getPrintManager().getElementsToPrint()!=null){
                    session.getPrintManager().printElements();
                } else if(session.getPrintManager().getFeaturesToPrint()!=null){
                    session.getPrintManager().printFeatures();
                }
            }
        };
        t.start();
    }
    
    @FXML private void disorderPrint(){
        System.out.println("IMPRESSION DES FICHES DE DÉSORDRES !");
        final Stage disorderPrintStage = new Stage();
        disorderPrintStage.setScene(new Scene(new FXDisorderPrintFrame()));
        disorderPrintStage.show();
    }
    
    @FXML
    public void openAppInfo() {
        final Stage infoStage = new Stage();
        infoStage.getIcons().add(SirsCore.ICON);
        infoStage.setTitle("À propos");
        infoStage.initStyle(StageStyle.UTILITY);
        infoStage.setScene(new Scene(new FXAboutPane()));
        infoStage.setResizable(false);
        infoStage.show();
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
