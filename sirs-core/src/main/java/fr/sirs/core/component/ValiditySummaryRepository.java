package fr.sirs.core.component;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.ValiditySummary;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.ViewQuery;
import org.ektorp.support.Views;

@Views({
        @View(name = "pseudoId", map="classpath:PseudoId-map.js"),
        @View(name = "validation", map="classpath:Validation-map.js")})
public class ValiditySummaryRepository extends
        CouchDbRepositorySupport<ValiditySummary> {

    public ValiditySummaryRepository(CouchDbConnector couchDbConnector) {
        super(ValiditySummary.class, couchDbConnector);
        initStandardDesignDocument();
    }
    
    public List<ValiditySummary> getPseudoIdsForClass(final Class clazz){
        ArgumentChecks.ensureNonNull("Class", clazz);
        final ViewQuery viewQuery = createQuery("pseudoId").includeDocs(false).key(clazz.getCanonicalName());
        final List<ValiditySummary> usages = db.queryView(viewQuery, ValiditySummary.class);
        return usages;
    }
    
    public List<ValiditySummary> getPseudoIds(){
        final ViewQuery viewQuery = createQuery("pseudoId").includeDocs(false);
        final List<ValiditySummary> usages = db.queryView(viewQuery, ValiditySummary.class);
        return usages;
    }
    
    public List<ValiditySummary> getValidation(){
        final ViewQuery viewQuery = createQuery("validation").includeDocs(false);
        final List<ValiditySummary> usages = db.queryView(viewQuery, ValiditySummary.class);
        return usages;
    }
    
    public List<ValiditySummary> getValidation(final boolean valid){
        final ViewQuery viewQuery = createQuery("validation").includeDocs(false).key(valid);
        final List<ValiditySummary> usages = db.queryView(viewQuery, ValiditySummary.class);
        return usages;
    }
}
