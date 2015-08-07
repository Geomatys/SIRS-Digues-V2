/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.core.component;

import fr.sirs.core.model.Element;
import java.util.List;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An repository whose role is to create global views useful for all repositories.
 * For exemple, instead of creating an "all" view for each repository, we can use
 * global repository to to make a single view whose keys are repository model class.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
@Views({
    @View(name=GlobalRepository.BY_CLASS_AND_LINEAR_VIEW, map="function(doc) {if(doc['@class']) {emit([doc['@class'], doc.linearId], doc._id)}}")
})
public class GlobalRepository extends CouchDbRepositorySupport<Element> {

    protected static final String BY_CLASS_AND_LINEAR_VIEW = "byClassAndLinear";

    @Autowired
    private GlobalRepository(CouchDbConnector db) {
        super(Element.class, db);
        initStandardDesignDocument();
    }

    protected <T> ViewQuery createByClassQuery(Class<T> type) {
        final ComplexKey startKey = ComplexKey.of(type.getCanonicalName());
        final ComplexKey endKey = ComplexKey.of(type.getCanonicalName(), ComplexKey.emptyObject());
        return createQuery(BY_CLASS_AND_LINEAR_VIEW)
                .startKey(startKey)
                .endKey(endKey)
                .includeDocs(true);
    }

    protected <T> ViewQuery createByLinearIdQuery(Class<T> type, final String linearId) {
        return createQuery(BY_CLASS_AND_LINEAR_VIEW)
                .key(ComplexKey.of(type == null? ComplexKey.emptyObject() : type.getCanonicalName(), linearId))
                .includeDocs(true);
    }

    <T> List<T> getAllForClass(Class<T> type) {
        return db.queryView(createByClassQuery(type), type);
    }

    <T> List<T> getByLinearId(Class<T> type, final String linearId) {
        return db.queryView(createByLinearIdQuery(type, linearId), type);
    }
}
