package fr.sirs;

import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.UtilisateurRepository;

import java.util.List;

import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.digue.DiguesTab;
import fr.sirs.other.FXDesignationPane;
import fr.sirs.other.FXReferencePane;
import fr.sirs.other.FXValidationPane;
import fr.sirs.theme.Theme;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.SirsPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.apache.sis.util.collection.Cache;
import org.apache.sis.util.iso.SimpleInternationalString;

import org.ektorp.CouchDbConnector;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.display2d.ext.DefaultBackgroundTemplate;
import org.geotoolkit.display2d.ext.legend.DefaultLegendTemplate;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.style.DefaultDescription;

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
    private final MapItem sirsGroup = MapBuilder.createItem();
    private final MapItem backgroundGroup = MapBuilder.createItem();

    private FXMainFrame frame = null;
    
    private final Cache<Element, FXFreeTab> openEditors = new Cache<>(12, 0, false);
    private final Cache<Theme, FXFreeTab> openThemes = new Cache<>(12, 0, false);
    private final Cache<Class<? extends ReferenceType>, FXFreeTab> openReferencePanes = new Cache<>(12, 0, false);
    private final Cache<Class<? extends Element>, FXFreeTab> openDesignationPanes = new Cache<>(12, 0, false);
    public enum AdminTab{VALIDATION, USERS}
    private final Cache<AdminTab, FXFreeTab> openAdminTabs = new Cache<>(2, 0, false);
    
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
        
        sirsGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
        final String referenceUrl;
        if(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.REFERENCE_URL)!=null){
            referenceUrl = SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.REFERENCE_URL);
        }
        else {
            referenceUrl = SirsPreferences.PROPERTIES.REFERENCE_URL.getDefaultValue();
        }
        referenceChecker = new ReferenceChecker(referenceUrl);
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
                //sirs layers
                sirsGroup.setName("Description des ouvrages");
                mapContext.items().add(0,sirsGroup);

                for(Plugin plugin : Plugins.getPlugins()){
                    List<MapItem> mapItems = plugin.getMapItems();
                    for (final MapItem item : mapItems) {
                        setPluginProvider(item, plugin);
                    }
                    sirsGroup.items().addAll(mapItems);
                }
                mapContext.setAreaOfInterest(mapContext.getBounds(true));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            try{
                //Fond de plan
                backgroundGroup.setName("Fond de plan");
                mapContext.items().add(0,backgroundGroup);
                final CoverageStore store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);

                for (Name n : store.getNames()) {
                    final CoverageReference cr = store.getCoverageReference(n);
                    final CoverageMapLayer cml = MapBuilder.createCoverageLayer(cr);
                    cml.setName("Open Street Map");
                    cml.setDescription(new DefaultDescription(
                            new SimpleInternationalString("Open Street Map"),
                            new SimpleInternationalString("Open Street Map")));
                    cml.setVisible(false);
                    backgroundGroup.items().add(cml);
                }
            } catch(Exception ex){
                ex.printStackTrace();
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
    
    public synchronized MapItem getSirsLayerGroup() {
        getMapContext();
        return sirsGroup;
    }

    public synchronized MapItem getBackgroundLayerGroup() {
        getMapContext();
        return backgroundGroup;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES IMPRESSIONS PDF
    ////////////////////////////////////////////////////////////////////////////
    private List<Element> elementsToPrint = null;
    private FeatureCollection featuresToPrint = null;
    
    public List<? extends Element> getElementsToPrint(){return elementsToPrint;}
    public FeatureCollection getFeaturesToPrint(){return featuresToPrint;}

    public void prepareToPrint(final Element object){
        featuresToPrint = null;
        elementsToPrint = new ArrayList<>();
        elementsToPrint.add(object);
    }

    public void prepareToPrint(final List<Element> objects){
        featuresToPrint = null;
        elementsToPrint = objects;
    }
    
    public void prepareToPrint(final Feature feature){
        elementsToPrint = null;
        featuresToPrint = FeatureStoreUtilities.collection(feature);
    }
    
    public void prepareToPrint(final FeatureCollection featureCollection){
        elementsToPrint = null;
        featuresToPrint = featureCollection;
    }
    
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
    
    public FXFreeTab getOrCreateAdminTab(final AdminTab adminTab, final String title){
        
        try {
            switch(adminTab){
                case USERS:
                    return openAdminTabs.getOrCreate(AdminTab.USERS, () -> {
                        final FXFreeTab tab = new FXFreeTab(title);
                        final PojoTable usersTable = new PojoTable(getUtilisateurRepository(), "Table des utilisateurs"){
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
        try {
            return openThemes.getOrCreate(theme, new Callable<FXFreeTab>() {
                @Override
                public FXFreeTab call() throws Exception {
                    final FXFreeTab tab = new FXFreeTab(theme.getName());
                    Parent parent = theme.createPane();
                    tab.setContent(parent);
                    tab.setOnClosed(event -> openThemes.remove(theme));
                    return tab;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    
    public FXFreeTab getOrCreateElementTab(final Element element) {
        if (element instanceof TronconDigue) {
            final DiguesTab diguesTab = Injector.getSession().getFrame().getDiguesTab();
            diguesTab.getDiguesController().displayElement(element);
            diguesTab.setOnSelectionChanged((Event event) -> {
                if (diguesTab.isSelected()) {
                    prepareToPrint(element);
                }
            });
            return diguesTab;
        } else {
            try {
                return openEditors.getOrCreate(element, new Callable<FXFreeTab>() {
                    @Override
                    public FXFreeTab call() throws Exception {
                        final FXFreeTab tab = new FXFreeTab();
                        Node content = (Node) SIRS.generateEditionPane(element);
                        if (content == null) {
                            content = new BorderPane(new Label("Pas d'éditeur pour le type : " + element.getClass().getSimpleName()));
                        }

                        tab.setContent(content);
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
                        tab.setOnSelectionChanged((Event event) -> {
                            if (tab.isSelected()) {
                                prepareToPrint(element);
                            }
                        });
                        
                        // Remove from cache when tab is closed.
                        tab.setOnClosed(event -> openEditors.remove(element));
                        return tab;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public String generateElementTitle(final Element element) {
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
