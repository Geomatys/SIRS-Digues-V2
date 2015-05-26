package fr.sirs.core;

import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.DatabaseRegistry;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.component.DigueRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.component.SessionGen;
import fr.sirs.core.component.ReferenceUsageRepository;
import fr.sirs.core.component.SQLQueryRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.core.model.Identifiable;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.Preview;
import fr.sirs.index.ElementHit;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.ektorp.CouchDbConnector;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.springframework.context.ConfigurableApplicationContext;

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
public class SessionCore extends SessionGen {
    
    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES DROITS
    ////////////////////////////////////////////////////////////////////////////
    private final ObjectProperty<Utilisateur> utilisateurProperty = new SimpleObjectProperty<>(null);
    public ObjectProperty<Utilisateur> utilisateurProperty() {return utilisateurProperty;}
    
    public Utilisateur getUtilisateur() {return utilisateurProperty.get();}
    public void setUtilisateur(final Utilisateur utilisateur){
        utilisateurProperty.set(utilisateur);
        if(utilisateur!=null){
            role.set(utilisateur.getRole());
            needValidationProperty.set(false);
            geometryEditionProperty.set(false);
            nonGeometryEditionProperty.set(false);
            if(role.get()==Role.ADMIN || role.get()==Role.EXTERN){
                geometryEditionProperty.set(true);
                nonGeometryEditionProperty.set(true);
                if(role.get()==Role.EXTERN){
                    needValidationProperty.set(true);
                }
            }
            else if(role.get()==Role.USER){
                geometryEditionProperty.set(false);
                nonGeometryEditionProperty.set(true);
            }
            else if(role.get()==Role.GUEST){
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
    
    private final ElementCreator elementCreator;
    
    public ElementCreator getElementCreator(){return elementCreator;}
    ////////////////////////////////////////////////////////////////////////////
    
    private ConfigurableApplicationContext applicationContext;
    public ConfigurableApplicationContext getApplicationContext(){return applicationContext;}
    public void setApplicationContext(final ConfigurableApplicationContext applicationContext) {
        this.applicationContext=applicationContext;
    }

    private final CouchDbConnector connector;
    
    ////////////////////////////////////////////////////////////////////////////
    // NON-GENERATED REPOSITORIES
    ////////////////////////////////////////////////////////////////////////////
    private final ReferenceUsageRepository referenceUsageRepository;
    private final Previews previews;
    private final SQLQueryRepository sqlQueryRepository;
    
    private CoordinateReferenceSystem projection;
    private int srid;
    
    @Autowired
    public SessionCore(CouchDbConnector couchDbConnector) {
        super(couchDbConnector);
        this.connector = couchDbConnector;
        
        referenceUsageRepository = new ReferenceUsageRepository(connector);
        previews = new Previews(connector);
        sqlQueryRepository = new SQLQueryRepository(connector);
        elementCreator = new ElementCreator(this);
    }

    public CouchDbConnector getConnector() {
        return connector;
    }
    
    public int getSrid() {
        if (projection == null) {
            getProjection();
        }
        return srid;
    }
    
    public CoordinateReferenceSystem getProjection() {
        if (projection == null) {
            try {
                Optional<SirsDBInfo> info = DatabaseRegistry.getInfo(connector);
                if (info.isPresent()) {
                    projection = CRS.decode(info.get().getEpsgCode());
                    srid = IdentifiedObjects.lookupEpsgCode(projection, true);
                }
            } catch (FactoryException e) {
                throw new SirsCoreRuntimeExecption(e);
            }
        }
        return projection;
    }
           
    /**
     * 
     * @return the application task manager, designed to start users tasks in a 
     * separate thread pool.
     */
    public TaskManager getTaskManager() {
        return SirsCore.getTaskManager();
    }  
    
    public ReferenceUsageRepository getReferenceUsageRepository(){
        return referenceUsageRepository;
    }
    
    public Previews getPreviews() {
        return previews;
    }

    public SQLQueryRepository getSqlQueryRepository() {
        return sqlQueryRepository;
    }
    
    public Collection<AbstractSIRSRepository> getModelRepositories(){
        return repositories.values();
    }
    
    public List<Objet> getObjetsByTronconId(final String tronconId){
        final List<Objet> objets = new ArrayList<>();
        for(final Element element : ServiceLoader.load(Element.class)){
            if(element instanceof Objet){
                final AbstractPositionableRepository repo = (AbstractPositionableRepository) getRepositoryForClass(element.getClass());
                final List elementList = repo.getByLinearId(tronconId);
                for(final Object objet : elementList){
                    objets.add((Objet) objet);
                }
            }
        }
        return objets;
    }
    
    public List<AbstractPositionDocument> getPositionDocumentsByTronconId(final String tronconId){
        final List<AbstractPositionDocument> positions = new ArrayList<>();
        for(final Element element : ServiceLoader.load(Element.class)){
            if(element instanceof AbstractPositionDocument){
                final AbstractPositionableRepository repo = (AbstractPositionableRepository) getRepositoryForClass(element.getClass());
                final List elementList = repo.getByLinearId(tronconId);
                for(final Object position : elementList){
                    positions.add((AbstractPositionDocument) position);
                }
            }
        }
        return positions;
    }
        
    // REFERENCES
    private static final List<Class<? extends ReferenceType>> REFERENCES = new ArrayList<>();
    private static final List<Class<? extends Element>> ELEMENTS = new ArrayList<>();
    private static void initReferences(){
        ServiceLoader.load(ReferenceType.class).forEach((ReferenceType t) -> REFERENCES.add(t.getClass()));
    }
    private static void initElements(){
        ServiceLoader.load(Element.class).forEach((Element t) -> ELEMENTS.add(t.getClass()));
    }
    
    static{
        initReferences();
        initElements();
    }
    
    public static List<Class<? extends ReferenceType>> getReferences(){return REFERENCES;}
    public static List<Class<? extends Element>> getElements(){return ELEMENTS;}
    
    public List<Digue> getDigues() {
        return getDigueRepository().getAll();
    }
    
    /**
     * Take an element in input, and return the same, but with its {@link Element#parentProperty() }
     * and {@link Element#getCouchDBDocument() } set.
     * 
     * @param e The element we want to get parent for.
     * @return The same element, completed with its parent, Or a null value if we 
     * cannot get full version of the element.
     */
    public Optional<? extends Element> getCompleteElement(Element e) {
        if (e != null) {
            if (e.getCouchDBDocument() != null) {
                // For objects like {@link tronconDigue}, we force reload, because 
                //they're root objects. It means checking their document do not 
                //ensure they're complete. 
                if (e.getCouchDBDocument() == e) {
                    return Optional.of((Element)getRepositoryForClass(e.getClass()).get(e.getId()));
                } else {
                    return Optional.of(e);
                }
            } else {
                String documentId = e.getDocumentId();
                if (documentId != null && !documentId.isEmpty()) {
                    Preview label = previews.get(documentId);
                    AbstractSIRSRepository targetRepo = getRepositoryForType(label.getDocClass());
                    if (targetRepo != null) {
                        Identifiable parent = targetRepo.get(documentId);
                        if (parent instanceof Element) {
                            return Optional.ofNullable(((Element) parent).getChildById(e.getId()));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Analyse input object to find a matching {@link Element} registered in database.
     * @param toGetElementFor The object which represents the Element to retrieve.
     * Can be a {@link Preview}, {@link Preview}, or {@link  ElementHit}
     * @return An optional which contains the found value, if any.
     */
    public Optional<? extends Element> getElement(final Object toGetElementFor) {
        if (toGetElementFor instanceof Element) {
            return getCompleteElement((Element)toGetElementFor);
            
        } else if (toGetElementFor instanceof Preview) {
            Preview summary = (Preview) toGetElementFor;
            AbstractSIRSRepository repository = getRepositoryForType(summary.getDocClass());
            Identifiable tmp = repository.get(summary.getDocId());
            if (tmp instanceof Element) {
                if (summary.getElementId() != null) {
                    return Optional.of(((Element)tmp).getChildById(summary.getElementId()));
                } else {
                    return Optional.of((Element)tmp);
                }
            }
            
        } else if (toGetElementFor instanceof ElementHit) {
            ElementHit hit = (ElementHit) toGetElementFor;
            AbstractSIRSRepository repository = getRepositoryForType(hit.geteElementClassName());
            Identifiable tmp = repository.get(hit.getDocumentId());
            if (tmp instanceof Element) {
                return Optional.of((Element)tmp);
            }
        }
        
        return Optional.empty();
    }
    
    public String getElementType(final Object o) {
        if (o instanceof Element) {
            return o.getClass().getCanonicalName();
        } else if (o instanceof Preview) {
            return ((Preview)o).getElementClass();
        } else if (o instanceof ElementHit) {
            return ((ElementHit)o).geteElementClassName();
        } else {
            return null;
        }
    }
}
