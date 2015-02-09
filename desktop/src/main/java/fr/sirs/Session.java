package fr.sirs;

import fr.sirs.core.SirsCore;
import fr.sirs.core.component.DigueRepository;

import java.util.List;

import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sirs.core.component.SessionGen;
import fr.sirs.core.component.PreviewLabelRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.RefConduiteFermee;
import fr.sirs.core.model.RefConvention;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefDevers;
import fr.sirs.core.model.RefDocumentGrandeEchelle;
import fr.sirs.core.model.RefEcoulement;
import fr.sirs.core.model.RefEvenementHydraulique;
import fr.sirs.core.model.RefFoncitonMaitreOeuvre;
import fr.sirs.core.model.RefFonction;
import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
import fr.sirs.core.model.RefImplantation;
import fr.sirs.core.model.RefLargeurFrancBord;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefMoyenManipBatardeaux;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefNatureBatardeaux;
import fr.sirs.core.model.RefOrientationOuvrage;
import fr.sirs.core.model.RefOrientationPhoto;
import fr.sirs.core.model.RefOrientationVent;
import fr.sirs.core.model.RefOrigineProfilLong;
import fr.sirs.core.model.RefOrigineProfilTravers;
import fr.sirs.core.model.RefOuvrageFranchissement;
import fr.sirs.core.model.RefOuvrageHydrauliqueAssocie;
import fr.sirs.core.model.RefOuvrageParticulier;
import fr.sirs.core.model.RefOuvrageTelecomEnergie;
import fr.sirs.core.model.RefOuvrageVoirie;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefPositionProfilLongSurDigue;
import fr.sirs.core.model.RefPrestation;
import fr.sirs.core.model.RefProfilFrancBord;
import fr.sirs.core.model.RefProprietaire;
import fr.sirs.core.model.RefRapportEtude;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.core.model.RefReseauHydroCielOuvert;
import fr.sirs.core.model.RefReseauTelecomEnergie;
import fr.sirs.core.model.RefRevetement;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.RefSeuil;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.RefTypeDocument;
import fr.sirs.core.model.RefTypeGlissiere;
import fr.sirs.core.model.RefTypeProfilTravers;
import fr.sirs.core.model.RefTypeTroncon;
import fr.sirs.core.model.RefUrgence;
import fr.sirs.core.model.RefUsageVoie;
import fr.sirs.core.model.RefUtilisationConduite;
import fr.sirs.core.model.RefVoieDigue;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.theme.Theme;
import fr.sirs.theme.ui.FXTronconThemePane;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.Internal;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.Cache;
import org.apache.sis.util.iso.SimpleInternationalString;

import org.ektorp.CouchDbConnector;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.style.DefaultDescription;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
public class Session extends SessionGen {
    
    public static String FLAG_SIRSLAYER = "SirsLayer";
    
    
    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES REFERENCES
    ////////////////////////////////////////////////////////////////////////////
    private final ReferenceChecker referenceChecker;
    public ReferenceChecker getReferenceChecker(){return referenceChecker;}
    
    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES DROITS
    ////////////////////////////////////////////////////////////////////////////
    private Utilisateur utilisateur = null;
    public Utilisateur getUtilisateur() {return utilisateur;}
    public void setUtilisateur(final Utilisateur utilisateur){
        this.utilisateur = utilisateur;
        if(utilisateur!=null){
            this.role.set(Role.valueOf(utilisateur.getRole()));
            needValidationProperty.set(false);
            geometryEditionProperty.set(false);
            nonGeometryEditionProperty.set(false);
            if(role.get()==Role.ADMIN || role.get()==Role.EXTERNE){
                geometryEditionProperty.set(true);
                nonGeometryEditionProperty.set(true);
                if(role.get()==Role.EXTERNE){
                    needValidationProperty.set(true);
                }
            }
            else if(role.get()==Role.USER){
                geometryEditionProperty.set(false);
                nonGeometryEditionProperty.set(true);
            }
            else if(role.get()==Role.CONSULTANT){
                geometryEditionProperty.set(false);
                nonGeometryEditionProperty.set(false);
            }
        }else{
            this.role.set(null);
            geometryEditionProperty.set(false);
            nonGeometryEditionProperty.set(false);
        }
    }
    
    private final BooleanProperty geometryEditionProperty = new SimpleBooleanProperty(false);
    public BooleanProperty geometryEditionProperty() {return geometryEditionProperty;}
    private final BooleanProperty nonGeometryEditionProperty = new SimpleBooleanProperty(false);
    public BooleanProperty nonGeometryEditionProperty() {return nonGeometryEditionProperty;}
    private final BooleanProperty needValidationProperty = new SimpleBooleanProperty(true);
    public BooleanProperty needValidationProperty() {return needValidationProperty;}
    
    
    private final ObjectProperty<Role> role = new SimpleObjectProperty();
    public Role getRole(){return role.get();}
    ////////////////////////////////////////////////////////////////////////////
    
    private ClassPathXmlApplicationContext applicationContext;
    public ClassPathXmlApplicationContext getApplicationContext(){return applicationContext;}
    public void setApplicationContext(final ClassPathXmlApplicationContext applicationContext){
        this.applicationContext=applicationContext;
    }

    private static final Class[] SUPPORTED_TYPES = new Class[]{
        Boolean.class,
        String.class,
        Integer.class,
        Float.class,
        Double.class,
        boolean.class,
        int.class,
        float.class,
        double.class,
        LocalDateTime.class
    };
        
    private static final Map<String,Map<String,Integer>> SORTED_FIELDS = new HashMap<>();
    static {
        final Properties SORTED_PROPERTIES = new Properties();
        final Properties SORTED_OVERRIDES = new Properties();
//        SORTED_PROPERTIES.load(Session.class.getResourceAsStream("/fr/sirs/model/fields.properties"));
//        SORTED_OVERRIDES.load(Session.class.getResourceAsStream("/fr/sirs/model/fields.properties"));
        
        for(Entry entry : SORTED_PROPERTIES.entrySet()){
            final String name = (String) entry.getKey();
            final String fields = (String) entry.getValue();
            final String[] split = fields.split(",");
            final Map<String,Integer> s = new HashMap<>();
            for(int i=0;i<split.length;i++){
                s.put(split[i].trim().toLowerCase(), i);
            }
            SORTED_FIELDS.put(name.toLowerCase(), s);
            
        }
        for(Entry entry : SORTED_OVERRIDES.entrySet()){
            final String name = (String) entry.getKey();
            final String fields = (String) entry.getValue();
            final String[] split = fields.split(",");
            final Map<String,Integer> s = new HashMap<>();
            for(int i=0;i<split.length;i++){
                s.put(split[i].trim().toLowerCase(), i);
            }
            SORTED_FIELDS.put(name.toLowerCase(), s);
        }
    }
    
    private Object objectToPrint = null;
    
    private MapContext mapContext;
    private final MapItem sirsGroup = MapBuilder.createItem();
    private final MapItem backgroundGroup = MapBuilder.createItem();

    private final CouchDbConnector connector;
    
    private final PreviewLabelRepository previewLabelRepository;

    private FXMainFrame frame = null;
    
    private final Cache<Theme, FXFreeTab> openThemes = new Cache<>(12, 0, false);
    private final Cache<Element, FXFreeTab> openEditors = new Cache<>(12, 0, false);
    
    /**
     * Clear session cache.
     */
    public void clearCache(){
        openEditors.clear();
        openThemes.clear();
    }
    
    @Autowired
    public Session(CouchDbConnector couchDbConnector) {
        super(couchDbConnector);
        this.connector = couchDbConnector;
        
        previewLabelRepository = new PreviewLabelRepository(connector);
        
        sirsGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
        referenceChecker = new ReferenceChecker();
    }

    public CouchDbConnector getConnector() {
        return connector;
    }
    
    public PreviewLabelRepository getPreviewLabelRepository() {
        return previewLabelRepository;
    }    
    
    void setFrame(FXMainFrame frame) {
        this.frame = frame;
    }

    public FXMainFrame getFrame() {
        return frame;
    }
        
    // REFERENCES
    private static final List<Class> REFERENCES = new ArrayList<>();
    private static void initReferences(){
        REFERENCES.add(RefConduiteFermee.class);
        REFERENCES.add(RefConvention.class);
        REFERENCES.add(RefCote.class);
        REFERENCES.add(RefDevers.class);
        REFERENCES.add(RefDocumentGrandeEchelle.class);
        REFERENCES.add(RefEcoulement.class);
        REFERENCES.add(RefEvenementHydraulique.class);
        REFERENCES.add(RefFoncitonMaitreOeuvre.class);
        REFERENCES.add(RefFonction.class);
        REFERENCES.add(RefFrequenceEvenementHydraulique.class);
        REFERENCES.add(RefImplantation.class);
        REFERENCES.add(RefLargeurFrancBord.class);
        REFERENCES.add(RefMateriau.class);
        REFERENCES.add(RefMoyenManipBatardeaux.class);
        REFERENCES.add(RefNatureBatardeaux.class);
        REFERENCES.add(RefNature.class);
        REFERENCES.add(RefOrientationOuvrage.class);
        REFERENCES.add(RefOrientationPhoto.class);
        REFERENCES.add(RefOrientationVent.class);
        REFERENCES.add(RefOrigineProfilLong.class);
        REFERENCES.add(RefOrigineProfilTravers.class);
        REFERENCES.add(RefOuvrageFranchissement.class);
        REFERENCES.add(RefOuvrageHydrauliqueAssocie.class);
        REFERENCES.add(RefOuvrageParticulier.class);
        REFERENCES.add(RefOuvrageTelecomEnergie.class);
        REFERENCES.add(RefOuvrageVoirie.class);
        REFERENCES.add(RefPosition.class);
        REFERENCES.add(RefPositionProfilLongSurDigue.class);
        REFERENCES.add(RefPrestation.class);
        REFERENCES.add(RefProfilFrancBord.class);
        REFERENCES.add(RefProprietaire.class);
        REFERENCES.add(RefRapportEtude.class);
        REFERENCES.add(RefReferenceHauteur.class);
        REFERENCES.add(RefReseauHydroCielOuvert.class);
        REFERENCES.add(RefReseauTelecomEnergie.class);
        REFERENCES.add(RefRevetement.class);
        REFERENCES.add(RefRive.class);
        REFERENCES.add(RefSeuil.class);
        REFERENCES.add(RefSource.class);
        REFERENCES.add(RefSystemeReleveProfil.class);
        REFERENCES.add(RefTypeDesordre.class);
        REFERENCES.add(RefTypeDocument.class);
        REFERENCES.add(RefTypeGlissiere.class);
        REFERENCES.add(RefTypeProfilTravers.class);
        REFERENCES.add(RefTypeTroncon.class);
        REFERENCES.add(RefUrgence.class);
        REFERENCES.add(RefUsageVoie.class);
        REFERENCES.add(RefUtilisationConduite.class);
        REFERENCES.add(RefVoieDigue.class);
    }
    
    static{
        initReferences();
    }
    
    public static List<Class> getReferences(){return REFERENCES;}
    
    public static void addReference(final Class reference){REFERENCES.add(reference);}

    /**
     * MapContext affiché pour toute l'application.
     *
     * @return MapContext
     */
    public synchronized MapContext getMapContext() {
        if(mapContext==null){
            mapContext = MapBuilder.createContext(SirsCore.getEpsgCode());
            mapContext.setName("Carte");

            try {
                //sirs layers
                sirsGroup.setName("Système de digue");
                mapContext.items().add(0,sirsGroup);

                for(Plugin plugin : Plugins.getPlugins()){
                    sirsGroup.items().addAll(plugin.getMapItems());
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
            }catch(Exception ex){
                ex.printStackTrace();
            }
            
        }
        return mapContext;
    }

    public synchronized MapItem getSirsLayerGroup() {
        getMapContext();
        return sirsGroup;
    }

    public synchronized MapItem getBackgroundLayerGroup() {
        getMapContext();
        return backgroundGroup;
    }
    
    public List<Digue> getDigues() {
        return ((DigueRepository) repositories.get("DigueRepository")).getAll();
    }
    
    public Digue getDigueById(final String digueId){
        return ((DigueRepository) repositories.get("DigueRepository")).get(digueId);
    }
    
    public SystemeEndiguement getSystemeEndiguementById(final String systemeEndiguementId){
        return ((SystemeEndiguementRepository) repositories.get("SystemeEndiguementRepository")).get(systemeEndiguementId);
    }

    public List<TronconDigue> getTroncons() {
        return ((TronconDigueRepository) repositories.get("TronconDigueRepository")).getAll();
    }

    public List<TronconDigue> getTronconDigueByDigue(final Digue digue) {
        return ((TronconDigueRepository) repositories.get("TronconDigueRepository")).getByDigue(digue);
    }
    
    public void update(final Digue digue){
        digue.setDateMaj(LocalDateTime.now());
        this.repositories.get("DigueRepository").update(digue);
    }
    
    /**
     * Update a section of the database.
     * @param tronconDigue 
     */
    public void update(final TronconDigue tronconDigue){
        tronconDigue.setDateMaj(LocalDateTime.now());
        SIRS.LOGGER.log(Level.FINE, "enregistrement de "+tronconDigue+" : : "+tronconDigue.getDigueId());
        repositories.get("TronconDigueRepository").update(tronconDigue);
    }
    
    /**
     * Update a list of sections of the database.
     * @param troncons 
     */
    public void update(final List<TronconDigue> troncons){
        troncons.stream().forEach((troncon) -> {
            this.update(troncon);
        });
    }
    
    /**
     * Add a troncon to the database.
     * @param tronconDigue 
     */
    public void add(final TronconDigue tronconDigue){
        tronconDigue.setDateMaj(LocalDateTime.now());
        repositories.get("TronconDigueRepository").add(tronconDigue);
    }
    
    /**
     * Remove a section from the database.
     * @param tronconDigue 
     */
    public void delete(final TronconDigue tronconDigue){
        repositories.get("TronconDigueRepository").remove(tronconDigue);
    }

    public void prepareToPrint(final Object object){
        objectToPrint=object;
        // TODO : make in asynchronous task ?
        if (object instanceof Element) {
            focusOnMap((Element) object);
        }
    }
    
    public Object getObjectToPrint(){return objectToPrint;}
    
    /**
     * Récupération des attributes simple pour affichage dans les tables.
     * 
     * @param clazz
     * @return liste des propriétés simples
     */
    public static List<PropertyDescriptor> listSimpleProperties(Class clazz) {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        try {
            for (java.beans.PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                final Method m = pd.getReadMethod();
                if(m==null || m.getAnnotation(Internal.class)!=null) continue;
                final Class propClass = m.getReturnType();
                for(Class c : SUPPORTED_TYPES){
                    if(c.isAssignableFrom(propClass)){
                        properties.add(pd);
                        break;
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        final Map<String,Integer> fields = SORTED_FIELDS.get(clazz.getSimpleName().toLowerCase());
        if(fields!=null){
            Collections.sort(properties, new Comparator<PropertyDescriptor>() {
                @Override
                public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
                    Integer idx1 = fields.get(o1.getName().toLowerCase());
                    if(idx1==null) idx1= Integer.MAX_VALUE;
                    Integer idx2 = fields.get(o2.getName().toLowerCase());
                    if(idx2==null) idx2= Integer.MAX_VALUE;
                    return idx1.compareTo(idx2);
                }
            });
        }
        
        return properties;
    }
    
    public FXFreeTab getOrCreateThemeTab(final Theme theme) {
        try {
            return openThemes.getOrCreate(theme, new Callable<FXFreeTab>() {
                @Override
                public FXFreeTab call() throws Exception {
                    final FXFreeTab tab = new FXFreeTab(theme.getName());
                    Parent parent = theme.createPane();
                    tab.setContent(parent);
                    if (parent instanceof FXTronconThemePane) {
                        ((FXTronconThemePane) parent).currentTronconProperty().addListener(new ChangeListener<TronconDigue>() {
                            @Override
                            public void changed(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
                                tab.setTextAbrege(theme.getName() + " (" + getPreviewLabelRepository().getPreview(newValue.getId()) + ")");
                            }
                        });
                    }
                    return tab;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public FXFreeTab getOrCreateElementTab(final Element element) {
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
                    tab.setTextAbrege(generateElementTitle(element));
                    tab.setOnSelectionChanged((Event event) -> {
                        if (tab.isSelected()) {
                            prepareToPrint(element);
                        }
                    });
                    return tab;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String generateElementTitle(final Element element) {
        String title;
        final ResourceBundle bundle = ResourceBundle.getBundle(element.getClass().getName());
        if (bundle != null) {
            title = bundle.getString("class");
        } else {
            title = "";
        }
        
        final String libelle = new SirsStringConverter().toString(element);
        if (libelle != null && !libelle.isEmpty()) {
            title += " : "+libelle;
        }
        
        final Element parent = element.getParent();
        if (parent instanceof AvecLibelle) {
            title+=" ("+((AvecLibelle)parent).getLibelle()+")";
        }
        return title;
    }
    
    // TODO : Implement
    public Element getCompleteElement(Element e) {
        if (e.getCouchDBDocument() != null) {
            return e;
        } else {
            String documentId = e.getDocumentId();
        }
        throw new IllegalArgumentException("Complete element cannot be rebuilt for : "+e);
    }
    
    public void focusOnMap(Element target) {
        if (target == null || frame == null || frame.getMapTab() == null || frame.getMapTab().getMap() == null) {
            return;
        }
        frame.getMapTab().getMap().focusOnElement(target);
    }
}
