package fr.sirs.core;

import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.PositionProfilTraversRepository;
import org.geotoolkit.gui.javafx.util.TaskManager;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.component.ReferenceUsageRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.AvecPhotos;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.core.model.Identifiable;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetPhotographiable;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.index.ElementHit;
import fr.sirs.util.ClosingDaemon;
import fr.sirs.util.StreamingIterable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.sis.util.collection.Cache;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentOperationResult;
import org.ektorp.http.StdHttpClient;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.util.collection.CloseableIterator;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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

    public static final String BUNDLE_KEY_CLASS = "class";

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
     * @return the list of all classes pointed by repositories.
     */
    public Collection<Class> getAvailableModels() {
        final Set<Class> clazz = new HashSet<>();
        for (final AbstractSIRSRepository repo : repositories.values()) {
            clazz.add(repo.getModelClass());
        }
        return clazz;
    }

    /**
     * Return a collection of candidate repositories for an abstract class or an interface.
     * @param elementType
     * @return All repositories which work on given element types or its sub-classes. Can be empty, but never null.
     */
    public Collection<AbstractSIRSRepository> getRepositoriesForClass(Class elementType) {
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
     * @return A valid repository for input type. Can be null if input canonical class name is unknown.
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

        PoolingClientConnectionManager connectionPool = null;
        StdHttpClient connection = (StdHttpClient) connector.getConnection();
        if (connection.getBackend() instanceof DefaultHttpClient) {
            final DefaultHttpClient defaultConnection = (DefaultHttpClient)connection.getBackend();
            ClientConnectionManager connectionManager = defaultConnection.getConnectionManager();
            if (connectionManager instanceof PoolingClientConnectionManager) {
                connectionPool = (PoolingClientConnectionManager)connectionManager;
            }
        }

        if (connectionPool == null) {
            SirsCore.LOGGER.warning("Cannot get connection pool stats");
        } else {
            final PoolingClientConnectionManager pool = connectionPool;
            final Thread statDaemon = new Thread(() -> {
                final Thread t = Thread.currentThread();
                while (!t.isInterrupted()) {
                    SirsCore.LOGGER.fine(pool.getTotalStats().toString());
                    SirsCore.LOGGER.fine("Watched resources : "+ClosingDaemon.referenceCache.size());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        SirsCore.LOGGER.log(Level.WARNING, "Stat thread interrupted !", ex);
                    }
                }
            });
            statDaemon.setDaemon(true);
            statDaemon.start();
        }

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
                throw new SirsCoreRuntimeException(e);
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

    public List<Objet> getObjetsByTronconId(final String tronconId) {
        final List<Objet> objets = new ArrayList<>();
        for(final AbstractSIRSRepository<Objet> repo : getRepositoriesForClass(Objet.class)) {
            if(repo instanceof AbstractPositionableRepository) {
                objets.addAll(((AbstractPositionableRepository<Objet>)repo).getByLinearId(tronconId));
            }
        }
        return objets;
    }

    public List<AbstractPositionDocument> getPositionDocumentsByTronconId(final String tronconId) {
        final List<AbstractPositionDocument> positions = new ArrayList<>();
                for(final AbstractSIRSRepository<AbstractPositionDocument> repo : getRepositoriesForClass(AbstractPositionDocument.class)) {
            if(repo instanceof AbstractPositionableRepository) {
                positions.addAll(((AbstractPositionableRepository<AbstractPositionDocument>)repo).getByLinearId(tronconId));
            }
        }

        return positions;
    }

    public List<Photo> getPhotoList(final String linearId) {
        final List<Photo> photos = new ArrayList<>();
        final StreamingIterable<PositionProfilTravers> positions = InjectorCore.getBean(PositionProfilTraversRepository.class).getByLinearIdStreaming(linearId);
        try (CloseableIterator<PositionProfilTravers> iterator = positions.iterator()) {
            while (iterator.hasNext()) {
                final List<Photo> p = iterator.next().getPhotos();
                if (p != null && !p.isEmpty())
                    photos.addAll(p);
            }
        }

        final List<Objet> objets = getObjetsByTronconId(linearId);
        for(final Objet objet : objets){
            if (objet instanceof ObjetPhotographiable){
                final List<Photo> p = ((ObjetPhotographiable) objet).getPhotos();
                if(p!=null && !p.isEmpty()) photos.addAll(p);
            } else if (objet instanceof Desordre){
                for (final Observation observation : ((Desordre) objet).getObservations()){
                    final List<Photo> p = observation.getPhotos();
                    if(p!=null && !p.isEmpty()) photos.addAll(p);
                }
            }
        }

        final Collection<AbstractSIRSRepository> repos = getRepositoriesForClass(AvecPhotos.class);
        for (final AbstractSIRSRepository repo : repos) {
            if (repo instanceof AbstractPositionableRepository) {
                StreamingIterable byLinearId = ((AbstractPositionableRepository)repo).getByLinearIdStreaming(linearId);
                try (final CloseableIterator iterator = byLinearId.iterator()) {
                    while (iterator.hasNext()) {
                        photos.addAll(((AvecPhotos)iterator.next()).getPhotos());
                    }
                }
            } else {
                for (final Object photoContainer : repo.getAllStreaming()) {
                    for (final Photo photo : ((AvecPhotos<Photo>)photoContainer).getPhotos()) {
                        Element parent = photo.getParent();
                        while (parent != null) {
                            if (parent instanceof AvecForeignParent && linearId.equalsIgnoreCase(((AvecForeignParent)parent).getForeignParentId())) {
                                photos.add(photo);
                                break;
                            }
                            parent = parent.getParent();
                        }
                    }
                }
            }
        }

        // Special case :
        StreamingIterable<Desordre> desordres = ((AbstractPositionableRepository<Desordre>) getRepositoryForClass(Desordre.class)).getByLinearIdStreaming(linearId);
        try (final CloseableIterator<Desordre> iterator = desordres.iterator()) {
            while (iterator.hasNext()) {
                final Desordre d = iterator.next();
                for (final Observation o : d.observations) {
                    photos.addAll(o.photos);
                }
            }
        }

        return photos;
    }

    public List<Positionable> getPositionableByLinearId(final String linearId) {
        final ArrayList<Positionable> positionables = new ArrayList<>();
        for (final AbstractPositionableRepository repo : applicationContext.getBeansOfType(AbstractPositionableRepository.class).values()) {
            positionables.addAll(repo.getByLinearId(linearId));
        }
        positionables.addAll(getPhotoList(linearId));

        return positionables;
    }

    // REFERENCES
    private static final List<Class<? extends Element>> ELEMENTS;
    private static final List<Class<? extends ReferenceType>> REFERENCES;
    private static final Comparator<Class<? extends Element>> BUNDLE_CLASS_NAME_COMPARATOR = new Comparator<Class<? extends Element>>() {
            @Override
            public int compare(Class<? extends Element> o1, Class<? extends Element> o2) {
                final ResourceBundle bdl1 = ResourceBundle.getBundle(o1.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
                final ResourceBundle bdl2 = ResourceBundle.getBundle(o2.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
                return bdl1.getString(BUNDLE_KEY_CLASS).compareTo(bdl2.getString(BUNDLE_KEY_CLASS));
            }
        };
    public Comparator<Class<? extends Element>> getBundleClassNameComparator(){return BUNDLE_CLASS_NAME_COMPARATOR;}

    static{
        final Iterator<Element> it = ServiceLoader.load(Element.class).iterator();

        final List<Class<? extends Element>> elements = new ArrayList<>();
        final List<Class<? extends ReferenceType>> references = new ArrayList<>();

        while(it.hasNext()){
            final Class c = it.next().getClass();
            elements.add(c);
            if(ReferenceType.class.isAssignableFrom(c)) references.add(c);
        }
        Collections.sort(elements, BUNDLE_CLASS_NAME_COMPARATOR);
        Collections.sort(references, BUNDLE_CLASS_NAME_COMPARATOR);
        ELEMENTS = Collections.unmodifiableList(elements);
        REFERENCES = Collections.unmodifiableList(references);
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
            final Identifiable tmp = repository.get(summary.getDocId() == null? summary.getElementId() : summary.getDocId());
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
                    e = completeElement.get().getCouchDBDocument();
                } else {
                    result.add(DocumentOperationResult.newInstance(e.getDocumentId(), "Cannot update document (client side error).", "Impossible to retrieve complete document to update from fragment."));
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
            final HashSet<Element> docs = entry.getValue();
            final Class docClass = entry.getKey();
            if (!docs.isEmpty()) {
                try {
                    final AbstractSIRSRepository repo = getRepositoryForClass(docClass);
                    if (repo == null) {
                        SirsCore.LOGGER.log(Level.WARNING, "No repository found for " + docClass);
                        for (final Element inError : docs) {
                            result.add(DocumentOperationResult.newInstance(inError.getDocumentId(), "Cannot update document (client side error).", "Cannot find any repository for class " + docClass.getCanonicalName()));
                        }
                    } else {
                        result.addAll(repo.executeBulk(docs));
                    }
                } catch (NoSuchBeanDefinitionException e) {
                    SirsCore.LOGGER.log(Level.WARNING, "No repository found for " + docClass, e);
                    for (final Element inError : docs) {
                        result.add(DocumentOperationResult.newInstance(inError.getDocumentId(), "Cannot update document (client side error).", "Cannot find any repository for class " + docClass.getCanonicalName()));
                    }
                } catch (Exception e) {
                    SirsCore.LOGGER.log(Level.WARNING, "Unexpected error on bulk execution.", e);
                    for (final Element inError : docs) {
                        result.add(DocumentOperationResult.newInstance(inError.getDocumentId(), "Unexpected error on position update", e.getMessage()));
                    }
                }
            }
        }

        return result;
    }
}
