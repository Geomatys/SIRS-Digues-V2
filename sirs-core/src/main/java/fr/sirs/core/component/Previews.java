package fr.sirs.core.component;

import fr.sirs.core.CacheRules;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsCoreRuntimeException;
import static fr.sirs.core.component.Previews.BY_CLASS;
import static fr.sirs.core.component.Previews.BY_ID;
import static fr.sirs.core.component.Previews.VALIDATION;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Organisme;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.Preview;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import javafx.collections.ObservableList;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.Cache;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.Options;
import org.ektorp.ViewQuery;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A read-only repository to get previews of current elements in database.
 * There's three types of map :
 * - By Id : Default view, sort element summaries by id.
 * - By class : A view which render previews according to the pointed element class.
 * - By validation : sort previews by validation state of target element.
 *
 * @author Alexis Manin (Geomatys)
 */
@Views({
    @View(name = BY_ID, map="classpath:Preview-by-id-map.js"),
    @View(name = BY_CLASS, map="classpath:Preview-by-class-map.js"),
    @View(name = VALIDATION, map="classpath:Preview-by-validation-map.js")})
@Component
public class Previews extends CouchDbRepositorySupport<Preview> implements DocumentListener {

    /**
     * Keeps previews got by class in memory. It allows multiple improvements :
     * - Keep unique instances of previews, limiting memory overload.
     * - Reduce IO by preventing dquerying database each time previews are queried.
     *
     * Notes :
     *  - Cached previews are kept up to date, thanks to CouchDB changes stream and javafx observable list capabilities.
     *  - Cache keys are queried class names, values are associated preview list.
     */
    private final Cache<String, ObservableList<Preview>> byClassCache = new Cache<>(20, 0, CacheRules.cacheElementsOfType(Preview.class));

    public static final String BY_ID = "previews";
    public static final String BY_CLASS = "designation";
    public static final String VALIDATION = "validation";

    @Autowired
    private Previews(CouchDbConnector couchDbConnector) {
        super(Preview.class, couchDbConnector);
        initStandardDesignDocument();
    }

    @Autowired(required=false)
    private void initListener(final DocumentChangeEmiter docChangeEmiter) {
        if (docChangeEmiter != null) {
            docChangeEmiter.addListener(this);
        }
    }

    @Override
    public Preview get(String id) {
        ArgumentChecks.ensureNonNull("Element ID", id);
        final ViewQuery viewQuery = createQuery(BY_ID).includeDocs(false).key(id);
        final List<Preview> usages = db.queryView(viewQuery, Preview.class);
        if (usages.size() > 0) {
            return usages.get(0);
        } else {
            throw new DocumentNotFoundException("No document found for "+id);
        }
    }

    public List<Preview> get(String... ids) {
        ArgumentChecks.ensureNonNull("Element ids", ids);
        ArgumentChecks.ensureNonNull("Class", ids);
        final ViewQuery viewQuery = createQuery(BY_ID).includeDocs(false).keys(Arrays.asList(ids));
        return db.queryView(viewQuery, Preview.class);
    }

    @Override
    public List<Preview> getAll() {
        final ViewQuery viewQuery = createQuery(BY_ID).includeDocs(false);
        return db.queryView(viewQuery, Preview.class);
    }


    /**
     * Retrieve the previews for objects of given canonicalClassNames (only for
     * concrete classes).
     *
     * @param canonicalClassNames
     * @return
     */
    public List<Preview> getByClass(final String... canonicalClassNames) {
        return Previews.this.getByClass(Arrays.asList(canonicalClassNames));
    }

    /**
     * Retrieve the previews for objects of given canonicalClassNames (only for
     * concrete classes).
     *
     * @param canonicalClassNames
     * @return
     */
    public List<Preview> getByClass(final Collection<String> canonicalClassNames) {
        ArgumentChecks.ensureNonNull("Class", canonicalClassNames);
        if (canonicalClassNames.size() < 1) {
            return Collections.EMPTY_LIST;
        } else if (canonicalClassNames.size() == 1) {
            return getOrCreateByClass(canonicalClassNames.iterator().next());

        } else {
            // No cache for now. When a "concatenated observable list" implementation will be available, we'll be able to use it.
            return db.queryView(createQuery(BY_CLASS).includeDocs(false).keys(canonicalClassNames), Preview.class);
        }
    }

    /**
     * Search in cache for previews of elements of the specified class. If we cannot
     * find them in it, we query them from database.
     * @param className
     * @return Previews matching given class.
     */
    private ObservableList<Preview> getOrCreateByClass(final String className) {
        try {
            return byClassCache.getOrCreate(className, () -> {
                return SirsCore.observableList(
                        db.queryView(
                                createQuery(BY_CLASS).includeDocs(false).key(className),
                                Preview.class
                        )
                );
            });
        } catch (Exception ex) {
            throw new SirsCoreRuntimeException("Cannot get previews for class "+className, ex);
        }
    }

    /**
     * Retrieve the previews for objects of given classes.
     *
     * For a concrete class,retrieve the previews of the objects of this class.
     *
     * For an abstract class or an interface, retrive the previews of the
     * objects of all classes extending the abstract class or implementing the
     * interface.
     *
     * @param classes
     * @return
     */
    public List<Preview> getByClass(final Class... classes) {
        final HashSet<String> classNames = new HashSet<>();
        for (final Class c : classes) {
            // To retrieve all previews for an abstract class, we must find all its implementations first.
            if(Modifier.isAbstract(c.getModifiers()) || c.isInterface()) {
                final List<Class> subTypes = SessionCore.getConcreteSubTypes(c);
                for (final Class clazz : subTypes) {
                    classNames.add(clazz.getCanonicalName());
                }
            } else {
                classNames.add(c.getCanonicalName());
            }
        }
        return Previews.this.getByClass(classNames);
    }

    public List<Preview> getAllByClass() {
        final ViewQuery viewQuery = createQuery(BY_CLASS).includeDocs(false);
        return db.queryView(viewQuery, Preview.class);
    }

    public List<Preview> getValidation() {
        final ViewQuery viewQuery = createQuery(VALIDATION).includeDocs(false);
        final List<Preview> previews = db.queryView(viewQuery, Preview.class);
        filterExistClass(previews);
        return previews;
    }

    public List<Preview> getAllByValidationState(final boolean valid) {
        final ViewQuery viewQuery = createQuery(VALIDATION).includeDocs(false).key(valid);
        final List<Preview> previews = db.queryView(viewQuery, Preview.class);
        filterExistClass(previews);
        return previews;
    }

    private void filterExistClass(final List<Preview> previews) {
        for (int length=previews.size()-1, i=length; i>=0; i--) {
            final Preview preview = previews.get(i);
            try {
                Class.forName(preview.getElementClass(), true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException ex) {
                // La classe de cet objet n'existe pas dans le classpath, c'est donc un objet que l'on
                // a créé via un plugin, puis le plugin a été désinstallé mais l'objet est resté dans la
                // base. On le retire donc de la liste des objets à valider.
                previews.remove(preview);
            }
        }
    }

    @Override
    public Preview get(String id, Options options) {
        throw new UnsupportedOperationException("We only work on views here, result cannot be parameterized.");
    }

    @Override
    public void documentCreated(Map<Class, List<Element>> added) {
        for (final Map.Entry<Class, List<Element>> entry : added.entrySet()) {
            final ObservableList<Preview> previews = byClassCache.get(entry.getKey().getCanonicalName());
            if (previews == null) {
                continue;
            }

            for (final Element e : entry.getValue()) {
                previews.add(createPreview(e));
            }
        }
    }

    @Override
    public void documentChanged(Map<Class, List<Element>> changed) {
        /* If an element has changed, we have to find all previews reflecting
         * changed elements or one of their children to replace them.
         */
        for (final Map.Entry<Class, List<Element>> entry : changed.entrySet()) {
            final ObservableList<Preview> previews = byClassCache.get(entry.getKey().getCanonicalName());
            if (previews == null) {
                continue;
            }

            // Index modified documents to find previews to modify faster.
            final HashMap<String, Element> documents = new HashMap<>(entry.getValue().size());
            for (final Element e : entry.getValue()) {
                if (e.getCouchDBDocument() != null) {
                    documents.putIfAbsent(e.getDocumentId(), e.getCouchDBDocument());
                }
            }

            ListIterator<Preview> it = previews.listIterator();
            Preview p;
            Element doc, child;
            while (it.hasNext()) {
                p = it.next();
                // Do not remove preview if no document matches it. it's the job of #documentDeleted.
                if (p.getDocId() != null) {
                    doc = documents.get(p.getDocId());
                    if (doc != null) {
                        child = doc.getChildById(p.getElementId());
                        if (child == null) { // Child should have been deleted from document. Preview is no longer needed.
                            it.remove();
                        } else {
                            it.set(createPreview(child)); // Update preview.
                        }
                    }
                }
            }
        }
    }

    @Override
    public void documentDeleted(Map<Class, List<Element>> deletedObject) {
        for (final Map.Entry<Class, List<Element>> entry : deletedObject.entrySet()) {
            final ObservableList<Preview> previews = byClassCache.get(entry.getKey().getCanonicalName());
            if (previews == null) {
                continue;
            }

            final HashSet<String> docIds = new HashSet<>(entry.getValue().size());
            for (final Element e : entry.getValue()) {
                docIds.add(e.getDocumentId());
            }
            previews.removeIf(p -> docIds.contains(p.getDocId()));
        }
    }

    /**
     * Create a new preview in memory, reflecting given element.
     * @param e Element to put in a preview.
     * @return Created preview.
     */
    private static Preview createPreview(Element e) {
        final Preview p = new Preview();
        p.setAuthor(e.getAuthor());
        p.setDesignation(e.getDesignation());
        p.setDocClass(e.getCouchDBDocument() != null? e.getCouchDBDocument().getClass().getCanonicalName() : null);
        p.setDocId(e.getDocumentId());
        p.setElementClass(e.getClass().getCanonicalName());
        p.setElementId(e.getId());
        if (e instanceof AvecLibelle) {
            p.setLibelle(((AvecLibelle)e).getLibelle());
        } else if (e instanceof Organisme) {
            p.setLibelle(((Organisme)e).getNom());
        } else if (e instanceof Contact) {
            p.setLibelle(((Contact)e).getNom());
        }
        p.setValid(e.getValid());

        return p;
    };

    /*
     * UNSUPPORTED OPERATIONS
     */
    @Override
    public void update(Preview entity) {
        throw new UnsupportedOperationException("Read-only repository. We only work on views here.");
    }

    @Override
    public void remove(Preview entity) {
        throw new UnsupportedOperationException("Read-only repository. We only work on views here.");
    }

    @Override
    public void add(Preview entity) {
        throw new UnsupportedOperationException("Read-only repository. We only work on views here.");
    }
}
