/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.core.component;

import fr.sirs.core.model.AmenagementHydraulique;
import fr.sirs.core.model.DescriptionAmenagementHydraulique;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author maximegavens
 */
public abstract class DescriptionAmenagementHydrauliqueRepository<T extends DescriptionAmenagementHydraulique> extends AbstractSIRSRepository<T> {
    
    public static final String BY_AMENAGEMENT_HYDRAULIQUE_ID = "byAmenagementHydrauliqueId";

    public DescriptionAmenagementHydrauliqueRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
    }
    
    public List<T> getByAmenagementHydrauliqueId(final String ahId) {
        ArgumentChecks.ensureNonNull("Amenagement hydraulique", ahId);
        return this.queryView(BY_AMENAGEMENT_HYDRAULIQUE_ID, ahId);
    }

    public List<T> getByAmenagementHydraulique(final AmenagementHydraulique ah) {
        ArgumentChecks.ensureNonNull("Amenagement hydraulique", ah);
        return this.queryView(BY_AMENAGEMENT_HYDRAULIQUE_ID, ah.getId());
    }
}
