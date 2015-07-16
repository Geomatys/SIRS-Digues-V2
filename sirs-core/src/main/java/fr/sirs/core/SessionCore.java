package fr.sirs.core;

import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.DatabaseRegistry;
import org.geotoolkit.gui.javafx.util.TaskManager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.component.ReferenceUsageRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.core.model.Identifiable;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.index.ElementHit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.apache.sis.util.collection.Cache;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentOperationResult;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
public class SessionCore implements ApplicationContextAware {

    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES DROITS
    ////////////////////////////////////////////////////////////////////////////
    private final ObjectProperty<Utilisateur> utilisateurProperty = new SimpleObjectProperty<>(null);
    public ObjectProperty<Utilisateur> utilisateurProperty() {return utilisateurProperty;}

    public Utilisateur getUtilisateur() {return utilisateurProperty.get();}
    public void setUtilisateur(final Utilisateur utilisateur) {
        utilisateurProperty.set(utilisateur);
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
    // GESTION DU CONTEXTE SPRING
    ////////////////////////////////////////////////////////////////////////////
    private ApplicationContext applicationContext;
    public ApplicationContext getApplicationContext(){return applicationContext;}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
    }


    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES REPOSITORIES COUCHDB
    ////////////////////////////////////////////////////////////////////////////
    /**
     * A map of all available repositories in current context. Value is a
     * specific repository, and key is the canonical name of the model class on
     * which the repository works.
     */
    private final HashMap<String, AbstractSIRSRepository> repositories = new HashMap<>();

    private final Cache<Class<? extends Element>, Collection<AbstractSIRSRepository>> matchingRepositoriesCache = new Cache<>(12, 12, false);

    /**
     * Retrieve all registries initialized by spring, then add them to current session.
     *
     * @param registered
     */
    @Autowired
    public void initRepositories(List<AbstractSIRSRepository> registered) {
        for (final AbstractSIRSRepository repo : registered) {
            repositories.put(repo.getModelClass().getCanonicalName(), repo);
        }
    }

    /**
     * Find a repository for update operations on {@link Element} of given type.
     * @param <T>
     * @param elementType The class of the type we want a {@link AbstractSIRSRepository} for. (Ex : RefMateriau.class, TronconDigue.class, etc.)
     * @return A valid repository for input type, or null if we cannot find any repository for given type.
     */
    public <T extends Element> AbstractSIRSRepository<T> getRepositoryForClass(Class<T> elementType) {
        if (elementType == null) return null;
        return getRepositoryForType(elementType.getCanonicalName());
    }

    public Collection<AbstractSIRSRepository> getModelRepositories(){
        return repositories.values();
    }

    /**
     * Return a collection of candidate repositories for an abstract class or an interface.
     * @param elementType
     * @return All repositories which work on given element types or its sub-classes. Can be empty, but never null.
     */
    public Collection<AbstractSIRSRepository> getRepositoriesForClass(Class<? extends Element> elementType) {
        if (elementType == null)
            return Collections.EMPTY_SET;
        try {
            return matchingRepositoriesCache.getOrCreate(elementType, () -> {
                final HashSet<AbstractSIRSRepository> result = new HashSet<>();
                for (final AbstractSIRSRepository repo : repositories.values()) {
                    if (elementType.isAssignableFrom(repo.getModelClass())) {
                        result.add(repo);
                    }
                }
                return result;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a repository for update operations on {@link Element} of given type.
     * @param type The name of the type we want a {@link AbstractSIRSRepository} for. (Ex : RefMateriau, TronconDigue, etc.)
     * @return A valid repository for input type. Never null..
     */
    public AbstractSIRSRepository getRepositoryForType(String type) {
        return repositories.get(type);
    }


    ////////////////////////////////////////////////////////////////////////////
    // SPECIFIC REPOSITORIES
    ////////////////////////////////////////////////////////////////////////////
    private final ReferenceUsageRepository referenceUsageRepository;
    private final Previews previews;

    private final CouchDbConnector connector;
    private CoordinateReferenceSystem projection;
    private int srid;

    @Autowired
    public SessionCore(CouchDbConnector couchDbConnector) {
        this.connector = couchDbConnector;

        referenceUsageRepository = new ReferenceUsageRepository(connector);
        previews = new Previews(connector);
        elementCreator = new ElementCreator(this);

        // Listen on user change
        utilisateurProperty.addListener(
                (ObservableValue<? extends Utilisateur> observable, Utilisateur oldValue, Utilisateur newValue) -> {
                    if (newValue == null || newValue.getRole() == null) {
                        role.set(Role.GUEST);
                    } else {
                        role.set(newValue.getRole());
                    }

                    // reset rights to most restricted, then unlock authorization regarding user role.
                    needValidationProperty.set(true);
                    geometryEditionProperty.set(false);
                    nonGeometryEditionProperty.set(false);
                    switch (role.get()) {
                        case USER:
                            nonGeometryEditionProperty.set(true);
                            needValidationProperty.set(false);
                            break;
                        case ADMIN:
                            nonGeometryEditionProperty.set(true);
                            geometryEditionProperty.set(true);
                            needValidationProperty.set(false);
                            break;
                        case EXTERN:
                            nonGeometryEditionProperty.set(true);
                            geometryEditionProperty.set(true);
                    }
                });
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

    public List<ProprieteTroncon> getProprietesByTronconId(final String tronconId){
        final AbstractPositionableRepository<ProprieteTroncon> repo = (AbstractPositionableRepository<ProprieteTroncon>) getRepositoryForClass(ProprieteTroncon.class);
        return repo.getByLinearId(tronconId);
    }

    public List<GardeTroncon> getGardesByTronconId(final String tronconId){
        final AbstractPositionableRepository<GardeTroncon> repo = (AbstractPositionableRepository<GardeTroncon>) getRepositoryForClass(GardeTroncon.class);
        return repo.getByLinearId(tronconId);
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
     * Can be a {@link Preview}, {@link  ElementHit}, or a {@link String} (in which
     * case it must represent a valid element ID).
     * @return An optional which contains the found value, if any.
     */
    public Optional<? extends Element> getElement(Object toGetElementFor) {
        if (toGetElementFor instanceof Element) {
            return getCompleteElement((Element)toGetElementFor);
        }

        if (toGetElementFor instanceof String) {
            toGetElementFor = previews.get((String)toGetElementFor);
        }

        if (toGetElementFor instanceof Preview) {
            final Preview summary = (Preview) toGetElementFor;
            final AbstractSIRSRepository repository = getRepositoryForType(summary.getDocClass());
            final Identifiable tmp = repository.get(summary.getDocId());
            if (tmp instanceof Element) {
                if (summary.getElementId() != null) {
                    return Optional.of(((Element)tmp).getChildById(summary.getElementId()));
                } else {
                    return Optional.of((Element)tmp);
                }
            }
        } else if (toGetElementFor instanceof ElementHit) {
            final ElementHit hit = (ElementHit) toGetElementFor;
            final AbstractSIRSRepository repository = getRepositoryForType(hit.getElementClassName());
            final Identifiable tmp = repository.get(hit.getDocumentId());
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
            return ((ElementHit)o).getElementClassName();
        } else {
            return null;
        }
    }

        /**
     * Update or add given elements to database. If one of the given elements is
     * a fragment, we'll try to get back complete element before updating.
     * @param target Collection of objects to update.
     * @return A list of failed operations. Can be empty, but never null.
     */
    public List<DocumentOperationResult> executeBulk(Collection<Element> target) {
        if (target == null || target.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        // Sort input documents by type to delegate bulk to repositories (to avoid cache problems)
        final HashMap<Class, HashSet<Element>> toUpdate = new HashMap<>();
        final ArrayList<DocumentOperationResult> result = new ArrayList<>();
        Iterator<Element> iterator = target.iterator();
        while (iterator.hasNext()) {
            Element e = iterator.next();
            if (e.getDocumentId() != null && !e.getDocumentId().equals(e.getId())) {
                // input object was only part of a couchdb document, so we try to retrieve complete element.
                Optional<? extends Element> completeElement = getCompleteElement(e);
                if (completeElement.isPresent()) {
                    e = completeElement.get();
                } else {
                    result.add(DocumentOperationResult.newInstance(e.getDocumentId(), "Cannot update document.", "Impossible to retrieve complete document to update from fragment."));
                }
            }

            HashSet<Element> tmpList = toUpdate.get(e.getClass());
            if (tmpList == null) {
                tmpList = new HashSet<>();
                toUpdate.put(e.getClass(), tmpList);
            }
            tmpList.add(e);
        }

        // Finally, we call bulk executor of each concerned repository.
        for (final Map.Entry<Class, HashSet<Element>> entry : toUpdate.entrySet()) {
            result.addAll(getRepositoryForClass(entry.getKey()).executeBulk(entry.getValue()));
        }

        return result;
    }
}
