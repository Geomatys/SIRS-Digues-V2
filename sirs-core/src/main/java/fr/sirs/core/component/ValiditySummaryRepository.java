package fr.sirs.core.component;

import static fr.sirs.core.component.ValiditySummaryRepository.DESIGNATION;
import static fr.sirs.core.component.ValiditySummaryRepository.VALIDATION;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.ValiditySummary;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.ViewQuery;
import org.ektorp.support.Views;

@Views({
        @View(name = DESIGNATION, map="classpath:PseudoId-map.js"),
        @View(name = VALIDATION, map="classpath:Validation-map.js")})
public class ValiditySummaryRepository extends CouchDbRepositorySupport<ValiditySummary> {
    
    public static final String DESIGNATION = "designation";
    public static final String VALIDATION = "validation";

    public ValiditySummaryRepository(CouchDbConnector couchDbConnector) {
        super(ValiditySummary.class, couchDbConnector);
        initStandardDesignDocument();
    }
    
    public List<ValiditySummary> getDesignationsForClass(final Class clazz){
        ArgumentChecks.ensureNonNull("Class", clazz);
        final ViewQuery viewQuery = createQuery(DESIGNATION).includeDocs(false).key(clazz.getCanonicalName());
        final List<ValiditySummary> usages = db.queryView(viewQuery, ValiditySummary.class);
        return usages;
    }
    
    public List<ValiditySummary> getAllDesignations(){
        final ViewQuery viewQuery = createQuery(DESIGNATION).includeDocs(false);
        final List<ValiditySummary> usages = db.queryView(viewQuery, ValiditySummary.class);
        return usages;
    }
    
    public List<ValiditySummary> getValidation(){
        final ViewQuery viewQuery = createQuery(VALIDATION).includeDocs(false);
        final List<ValiditySummary> usages = db.queryView(viewQuery, ValiditySummary.class);
        return usages;
    }
    
    public List<ValiditySummary> getValidation(final boolean valid){
        final ViewQuery viewQuery = createQuery(VALIDATION).includeDocs(false).key(valid);
        final List<ValiditySummary> usages = db.queryView(viewQuery, ValiditySummary.class);
        return usages;
    }
}
