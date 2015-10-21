package fr.sirs;

import fr.sirs.core.ModuleDescription;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.SirsDBInfoRepository;
import fr.sirs.core.component.UtilisateurRepository;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.other.FXDesignationPane;
import fr.sirs.other.FXReferencePane;
import fr.sirs.other.FXValidationPane;
import fr.sirs.theme.Theme;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.ui.TemplateGeneratorPane;
import fr.sirs.ui.TemplatesTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.SirsPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import org.apache.sis.util.collection.Cache;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.display2d.ext.DefaultBackgroundTemplate;
import org.geotoolkit.display2d.ext.legend.DefaultLegendTemplate;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.storage.coverage.CoverageReference;
import org.geotoolkit.storage.coverage.CoverageStore;
import org.geotoolkit.style.DefaultDescription;
import org.opengis.util.GenericName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * La session contient toutes les données chargées dans l'instance courante de
 * l'application.
 *
 * Notamment, elle doit réferencer l'ensemble des thèmes ouvert, ainsi que les
 * onglets associés. De même pour les {@link Element}s et leurs éditeurs.
 *
 * La session fournit également un point d'accès centralisé à tous les documents
 * de la base CouchDB.
 *
 * @author Johann Sorel
 */
@Component
public class Session extends SessionCore {

    public static String FLAG_SIRSLAYER = "SirsLayer";

    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES REFERENCES
    ////////////////////////////////////////////////////////////////////////////
    private final ReferenceChecker referenceChecker;
    public ReferenceChecker getReferenceChecker(){return referenceChecker;}

    ////////////////////////////////////////////////////////////////////////////
    private MapContext mapContext;
    private final MapItem backgroundGroup = MapBuilder.createItem();

    private FXMainFrame frame = null;

    private final Cache<Element, FXFreeTab> openEditors = new Cache<>(12, 0, false);
    private final Cache<Theme, FXFreeTab> openThemes = new Cache<>(12, 0, false);
    private final Cache<Class<? extends ReferenceType>, FXFreeTab> openReferencePanes = new Cache<>(12, 0, false);
    private final Cache<Class<? extends Element>, FXFreeTab> openDesignationPanes = new Cache<>(12, 0, false);
    public enum AdminTab{VALIDATION, USERS}
    private final Cache<AdminTab, FXFreeTab> openAdminTabs = new Cache<>(2, 0, false);
    public enum PrintTab{DESORDRE, RESEAU_FERME, TEMPLATE}
    private final Cache<PrintTab, FXFreeTab> openPrintTabs = new Cache<>(2, 0, false);
    private static FXFreeTab userGuideTab = null;

    //generate a template for the legend
    final DefaultLegendTemplate legendTemplate = new DefaultLegendTemplate(
            new DefaultBackgroundTemplate( //legend background
                    new BasicStroke(2), //stroke
                    Color.BLUE, //stroke paint
                    Color.WHITE, // fill paint
                    new Insets(10, 10, 10, 10), //border margins
                    8 //round border
            ),
            2, //gap between legend elements
            null, //glyph size, we can let it to null for the legend to use the best size
            new Font("Serial", Font.PLAIN, 11), //Font used for style rules
            true, // show layer names
            new Font("Serial", Font.BOLD, 12), //Font used for layer names
            true // display only visible layers
    );

    /**
     * Clear session cache.
     */
    public void clearCache(){
        openEditors.clear();
        openThemes.clear();
        openReferencePanes.clear();
        openDesignationPanes.clear();
        openAdminTabs.clear();
    }

    @Autowired
    public Session(CouchDbConnector couchDbConnector) {
        super(couchDbConnector);
        final String referenceUrl;
        if(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.REFERENCE_URL)!=null){
            referenceUrl = SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.REFERENCE_URL);
        }
        else {
            referenceUrl = SirsPreferences.PROPERTIES.REFERENCE_URL.getDefaultValue();
        }
        referenceChecker = new ReferenceChecker(referenceUrl);
        printManager = new PrintManager();
    }

    void setFrame(FXMainFrame frame) {
        this.frame = frame;
    }

    public FXMainFrame getFrame() {
        return frame;
    }

    /**
     * MapContext affiché pour toute l'application.
     *
     * @return MapContext
     */
    public synchronized MapContext getMapContext() {
        if(mapContext==null){
            mapContext = MapBuilder.createContext(getProjection());
            mapContext.setName("Carte");

            try {
                //modules layers
                final Plugin[] plugins = Plugins.getPlugins();
                final HashMap<String, ModuleDescription> moduleDescriptions = new HashMap<>(plugins.length);
                for(Plugin plugin : plugins){
                    final ModuleDescription d = new ModuleDescription();
                    d.setName(plugin.name);
                    d.setTitle(plugin.getTitle().toString());
                    d.setVersion(plugin.getConfiguration().getVersionMajor()+"."+plugin.getConfiguration().getVersionMinor());

                    List<MapItem> mapItems = plugin.getMapItems();
                    for (final MapItem item : mapItems) {
                        setPluginProvider(item, plugin);
                        ModuleDescription.getLayerDescription(item).ifPresent(desc -> d.layers.add(desc));
                    }
                    mapContext.items().addAll(mapItems);
                    moduleDescriptions.put(d.getName(), d);
                }
                mapContext.setAreaOfInterest(mapContext.getBounds(true));
                SirsDBInfoRepository infoRepo = getApplicationContext().getBean(SirsDBInfoRepository.class);
                infoRepo.updateModuleDescriptions(moduleDescriptions);

            } catch (Exception ex) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot retrieve sirs layers.", ex);
                final Runnable r = () -> GeotkFX.newExceptionDialog("Impossible de construire la liste des couches cartographiques", ex).show();
                if (Platform.isFxApplicationThread()) {
                    r.run();
                } else {
                    Platform.runLater(r);
                }
            }

            try{
                //Fond de plan
                backgroundGroup.setName("Fond de plan");
                mapContext.items().add(0,backgroundGroup);
                final CoverageStore store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);

                for (GenericName n : store.getNames()) {
                    final CoverageReference cr = store.getCoverageReference(n);
                    final CoverageMapLayer cml = MapBuilder.createCoverageLayer(cr);
                    cml.setName("Open Street Map");
                    cml.setDescription(new DefaultDescription(
                            new SimpleInternationalString("Open Street Map"),
                            new SimpleInternationalString("Open Street Map")));
                    cml.setVisible(false);
                    backgroundGroup.items().add(cml);
                    break;
                }
            } catch(Exception ex){
                SirsCore.LOGGER.log(Level.WARNING, "Cannot retrieve background layers.", ex);
                final Runnable r = () -> GeotkFX.newExceptionDialog("Impossible de construire le fond de plan OpenStreetMap", ex).show();
                if (Platform.isFxApplicationThread()) {
                    r.run();
                } else {
                    Platform.runLater(r);
                }
            }

        }
        return mapContext;
    }

    /**
     * Mark the given map item and all of its layers as provided by input plugin.
     * @param mapItem The map item to mark. Cannot be null
     * @param provider The plugin which provided the item. Cannot be null.
     */
    private static void setPluginProvider(final MapItem mapItem, final Plugin provider) {
        mapItem.getUserProperties().put(Plugin.PLUGIN_FLAG, provider.name);
        for (final MapItem child : mapItem.items()) {
            setPluginProvider(child, provider);
        }
    }

    public synchronized MapItem getBackgroundLayerGroup() {
        getMapContext();
        return backgroundGroup;
    }

    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES IMPRESSIONS PDF
    ////////////////////////////////////////////////////////////////////////////
    private final PrintManager printManager;
    public final PrintManager getPrintManager(){return printManager;}

    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES PANNEAUX
    ////////////////////////////////////////////////////////////////////////////

    public void showEditionTab(final Object object) {
        final Optional<? extends Element> element = getElement(object);
        if (element.isPresent()){
            if(element.get() instanceof ReferenceType) {
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Les références ne sont pas éditables.", ButtonType.CLOSE);
                alert.setResizable(true);
                alert.showAndWait();
            } else {
                getFrame().addTab(getOrCreateElementTab(element.get()));
            }
        }
    }

    public FXFreeTab getOrCreatePrintTab(final PrintTab printTab, final String title){

        try {
            if (PrintTab.DESORDRE.equals(printTab)) {
                return openPrintTabs.getOrCreate(PrintTab.DESORDRE, () -> {
                    final FXFreeTab tab = new FXFreeTab(title);
                    tab.setContent(new FXDisorderPrintPane());
                    return tab;
                });
            } else if(PrintTab.RESEAU_FERME.equals(printTab)) {
                return openPrintTabs.getOrCreate(PrintTab.RESEAU_FERME, () -> {
                    final FXFreeTab tab = new FXFreeTab(title);
                    tab.setContent(new FXReseauFermePrintPane());
                    return tab;
                });
            } else {
                return openPrintTabs.getOrCreate(PrintTab.TEMPLATE, () -> {
                    final FXFreeTab tab = new FXFreeTab(title);
                    final HBox hbox = new HBox();
                    hbox.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
                    final TemplatesTable table = new TemplatesTable();
                    HBox.setHgrow(table, Priority.SOMETIMES);
                    hbox.getChildren().add(table);
                    final TemplateGeneratorPane genPane = new TemplateGeneratorPane();
                    HBox.setHgrow(genPane, Priority.SOMETIMES);
                    hbox.getChildren().add(genPane);
                    hbox.setMinWidth(400);
                    hbox.setMaxWidth(Double.MAX_VALUE);
                    hbox.setFillHeight(true);
                    tab.setContent(hbox);
                    return tab;
                });
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public FXFreeTab getOrCreateAdminTab(final AdminTab adminTab, final String title){

        try {
            switch(adminTab){
                case USERS:
                    return openAdminTabs.getOrCreate(AdminTab.USERS, () -> {
                        final FXFreeTab tab = new FXFreeTab(title);
                        final PojoTable usersTable = new PojoTable(getRepositoryForClass(Utilisateur.class), "Table des utilisateurs"){
                            @Override
                            protected void deletePojos(final Element... pojos) {
                                final List<Element> pojoList = new ArrayList<>();
                                for (final Element pojo : pojos) {
                                    if(pojo instanceof Utilisateur){
                                        final Utilisateur utilisateur = (Utilisateur) pojo;
                                        // On interdit la suppression de l'utilisateur courant !
                                        if(utilisateur.equals(session.getUtilisateur())){
                                            final Alert alert = new Alert(Alert.AlertType.ERROR, "Vous ne pouvez pas supprimer votre propre compte.", ButtonType.CLOSE);
                                            alert.setResizable(true);
                                            alert.showAndWait();
                                        }
                                        // On interdit également la suppression de l'invité par défaut !
                                        else if (UtilisateurRepository.GUEST_USER.equals(utilisateur)){
                                            final Alert alert = new Alert(Alert.AlertType.ERROR, "Vous ne pouvez pas supprimer le compte de l'invité par défaut.", ButtonType.CLOSE);
                                            alert.setResizable(true);
                                            alert.showAndWait();
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
                        tab.setContent(usersTable);
                        return tab;
                    });
                case VALIDATION:
                    return openAdminTabs.getOrCreate(AdminTab.VALIDATION, () -> {
                        final FXFreeTab tab = new FXFreeTab(title);
                        tab.setContent(new FXValidationPane());
                        return tab;
                    });
                default:
                    throw new UnsupportedOperationException("Unsupported administration pane.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FXFreeTab getOrCreateThemeTab(final Theme theme) {
        if(theme.isCached()){
            try {
                return openThemes.getOrCreate(theme, new Callable<FXFreeTab>() {
                    @Override
                    public FXFreeTab call() throws Exception {
                        final Parent parent = theme.createPane();
                        if (parent == null) {
                            return null;
                        } else {
                            final FXFreeTab tab = new FXFreeTab(theme.getName());
                            tab.setContent(parent);
                            tab.setOnClosed(event -> openThemes.remove(theme));
                            tab.selectedProperty().addListener(theme.getSelectedPropertyListener());
                            return tab;
                        }
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        /*
        Certains thèmes doivent pouvoir être ouverts depuis plusieus origines et
        ne doivent donc pas être mis en cache.

        Par exemple, pour le plugin AOT/COT, le thème de consultation d'une
        convention pour un élément dépend de l'élément courant. Si on est sur un
        élément "A" lié à une convention "1", le panneau de thème liste la
        convention "1". Mais si on ne ferme pas le panneau de thème et qu'on
        souhaite ensuite consulter les conventions d'un élément "B" lié à une
        autre convention "2", le fait de refaire appel au thème, s'il est mis en
        cache, ne fait que donner le focus au panneau précédant listant la
        convention "1". En supprimant le cache, on ouvre une nouvelle fenêtre
        avec la convention "2".
         */
        else {
            final Parent parent = theme.createPane();
            if(parent==null)
                return null;
            else {
                final FXFreeTab tab = new FXFreeTab(theme.getName());
                tab.setContent(parent);
                tab.selectedProperty().addListener(theme.getSelectedPropertyListener());
                return tab;
            }
        }
    }

    public FXFreeTab getOrCreateDesignationTab(final Class<? extends Element> clazz){
        try {
            return openDesignationPanes.getOrCreate(clazz, new Callable<FXFreeTab>() {
                final ResourceBundle bdl = ResourceBundle.getBundle(clazz.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
                @Override
                public FXFreeTab call() throws Exception {
                    final FXFreeTab tab = new FXFreeTab("Désignations du type " + bdl.getString(BUNDLE_KEY_CLASS));
                    tab.setContent(new FXDesignationPane(clazz));
                    return tab;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FXFreeTab getOrCreateReferenceTypeTab(final Class<? extends ReferenceType> clazz){
        try {
            return openReferencePanes.getOrCreate(clazz, new Callable<FXFreeTab>() {
                final ResourceBundle bdl = ResourceBundle.getBundle(clazz.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
                @Override
                public FXFreeTab call() throws Exception {
                    final FXFreeTab tab = new FXFreeTab(bdl.getString(BUNDLE_KEY_CLASS));
                    tab.setContent(new FXReferencePane(clazz));
                    return tab;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param element
     * @return
     */
    public FXFreeTab generateTabForPane(final Element element){
        final FXFreeTab tab = new FXFreeTab();

        final ProgressIndicator wait = new ProgressIndicator();
        wait.setMaxSize(200, 200);
        wait.setProgress(-1);
        final BorderPane content = new BorderPane(wait);
        tab.setContent(content);

        Injector.getSession().getTaskManager().submit(() -> {
            Node edit = (Node) SIRS.generateEditionPane(element);
            if (edit == null) {
                edit = new BorderPane(new Label("Pas d'éditeur pour le type : " + element.getClass().getSimpleName()));
            }
            final Node n = edit;
            FadeTransition ft = new FadeTransition(Duration.millis(1000), n);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            Platform.runLater(()->{content.setCenter(n);n.requestFocus();ft.play();});
        });

        if(element instanceof PositionDocument){
            final PositionDocument positionDocument = (PositionDocument) element;
            positionDocument.sirsdocumentProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    tab.setTextAbrege(generateElementTitle(element));
                }
            });
        }
        element.designationProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                tab.setTextAbrege(generateElementTitle(element));
            }
        });
        tab.setTextAbrege(generateElementTitle(element));

        // Remove from cache when tab is closed.
        tab.setOnClosed(event -> openEditors.remove(element));
        return tab;
    }

    public FXFreeTab getOrCreateElementTab(final Element element) {

        // On commence par regarder si un plugin spécifie une ouverture particulière.
        for(final Plugin plugin : Plugins.getPlugins()){
            if(plugin.handleTronconType(element.getClass())){
                return plugin.openTronconPane(element);
            }
        }

        // Si on a affaire à un élément qui n'est pas un tronçon, ou bien d'un type de tronçon qu'aucun plugin n'ouvre de manière particulière, on ouvre l'élément de manière standard.
        try {
            return openEditors.getOrCreate(element, () -> generateTabForPane(element));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateElementTitle(final Element element) {
        String title="";

        final String libelle = new SirsStringConverter().toString(element);
        if (libelle != null && !libelle.isEmpty()) {
            title += libelle;
        }

        final Element parent = element.getParent();
        if (parent instanceof AvecLibelle) {
            final String parentLibelle = ((AvecLibelle)parent).getLibelle();
            if(parentLibelle!=null){
                title+=" ("+parentLibelle+")";
            }
        }
        return title;
    }

    public void focusOnMap(Element target) {
        if (target == null || frame == null || frame.getMapTab() == null || frame.getMapTab().getMap() == null) {
            return;
        }
        frame.getMapTab().getMap().focusOnElement(target);
    }

    public DefaultLegendTemplate getLegendTemplate() {
        return legendTemplate;
    }
}
