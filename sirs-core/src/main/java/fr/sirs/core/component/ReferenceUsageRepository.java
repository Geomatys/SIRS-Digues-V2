package fr.sirs.core.component;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.ReferenceUsage;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.ViewQuery;

@View(name = "usages", map="classpath:ReferenceUsages-map.js")
public class ReferenceUsageRepository extends
        CouchDbRepositorySupport<ReferenceUsage> {

    public ReferenceUsageRepository(CouchDbConnector couchDbConnector) {
        super(ReferenceUsage.class, couchDbConnector);
        initStandardDesignDocument();
    }
    
    public List<ReferenceUsage> getReferenceUsages(final String referenceId){
        ArgumentChecks.ensureNonNull("Reference id", referenceId);
        final ViewQuery viewQuery = createQuery("usages").includeDocs(false).key(referenceId);
        final List<ReferenceUsage> usages = db.queryView(viewQuery, ReferenceUsage.class);
        return usages;
    }
    
    public List<ReferenceUsage> getReferenceUsages(){
        final ViewQuery viewQuery = createQuery("usages").includeDocs(false);
        final List<ReferenceUsage> usages = db.queryView(viewQuery, ReferenceUsage.class);
        return usages;
    }
}
