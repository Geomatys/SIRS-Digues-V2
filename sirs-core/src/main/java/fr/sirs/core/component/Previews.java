package fr.sirs.core.component;

import static fr.sirs.core.component.Previews.BY_CLASS;
import static fr.sirs.core.component.Previews.BY_ID;
import static fr.sirs.core.component.Previews.VALIDATION;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.Preview;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    
    public List<Preview> getByClass(final String... canonicalClassNames) {
        return Previews.this.getByClass(Arrays.asList(canonicalClassNames));
    }
    
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
    
    public List<Preview> getByClass(final Class... classes) {
        final ArrayList<String> classNames = new ArrayList();
        for (final Class c : classes) {
            classNames.add(c.getCanonicalName());
        }
        return Previews.this.getByClass(classNames);
    }
    
    public List<Preview> getAllByClass() {
        final ViewQuery viewQuery = createQuery(BY_CLASS).includeDocs(false);
        return db.queryView(viewQuery, Preview.class);
    }
    
    public List<Preview> getValidation() {
        final ViewQuery viewQuery = createQuery(VALIDATION).includeDocs(false);
        return db.queryView(viewQuery, Preview.class);
    }
    
    public List<Preview> getAllByValidationState(final boolean valid) {
        final ViewQuery viewQuery = createQuery(VALIDATION).includeDocs(false).key(valid);
        return db.queryView(viewQuery, Preview.class);
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
