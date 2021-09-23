/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.core.component;

import static fr.sirs.core.component.AmenagementHydrauliqueViewRepository.AH_VIEW_QUERY;
import fr.sirs.core.model.AmenagementHydrauliqueView;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author maximegavens
 */
@Component
@View(name=AH_VIEW_QUERY, map="classpath:Amenagement-hydraulique-view-map.js")
public class AmenagementHydrauliqueViewRepository extends CouchDbRepositorySupport<AmenagementHydrauliqueView> {

    public static final String AH_VIEW_QUERY = "amenagement_hydraulique_specific_view";

    @Autowired
    public AmenagementHydrauliqueViewRepository(CouchDbConnector couchDbConnector) {
        super(AmenagementHydrauliqueView.class, couchDbConnector);
        initStandardDesignDocument();
    }

    public List<AmenagementHydrauliqueView> getAmenagementHydrauliqueView(final String ahId) {
        ArgumentChecks.ensureNonNull("Amenagement hydraulique id", ahId);
        final ViewQuery viewQuery = createQuery(AH_VIEW_QUERY).includeDocs(false).key(ahId);
        return db.queryView(viewQuery, AmenagementHydrauliqueView.class);
    }

    public List<AmenagementHydrauliqueView> getAmenagementHydrauliqueView() {
        final ViewQuery viewQuery = createQuery(AH_VIEW_QUERY).includeDocs(false);
        return db.queryView(viewQuery, AmenagementHydrauliqueView.class);
    }
    
}
