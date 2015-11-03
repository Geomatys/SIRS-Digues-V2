package fr.sirs.core.component;

import fr.sirs.core.SessionCore;
import static fr.sirs.core.component.Previews.BY_CLASS;
import static fr.sirs.core.component.Previews.BY_ID;
import static fr.sirs.core.component.Previews.VALIDATION;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.Preview;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.Options;
import org.ektorp.ViewQuery;
import org.ektorp.support.Views;

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
public class Previews extends CouchDbRepositorySupport<Preview> {

    public static final String BY_ID = "previews";
    public static final String BY_CLASS = "designation";
    public static final String VALIDATION = "validation";

    public Previews(CouchDbConnector couchDbConnector) {
        super(Preview.class, couchDbConnector);
        initStandardDesignDocument();
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

        ViewQuery viewQuery = createQuery(BY_CLASS).includeDocs(false);
        if (canonicalClassNames.size() == 1) {
            viewQuery = viewQuery.key(canonicalClassNames.iterator().next());
        } else if (canonicalClassNames.size() > 1) {
            viewQuery = viewQuery.keys(canonicalClassNames);
        }
        return db.queryView(viewQuery, Preview.class);
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
                classNames.addAll(SessionCore.getConcreteSubTypes(c));
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

    @Override
    public Preview get(String id, Options options) {
        throw new UnsupportedOperationException("We only work on views here, result cannot be parameterized.");
    }
}
