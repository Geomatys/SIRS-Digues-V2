package fr.sirs.core.component;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.PreviewLabel;

@View(name="all", map="function(doc) {if(doc.libelle) emit(doc._id, doc.libelle)}")
public class PreviewLabelRepository extends CouchDbRepositorySupport<PreviewLabel>{

    public PreviewLabelRepository(CouchDbConnector couchDbConnector) {
        super(PreviewLabel.class, couchDbConnector);
        initStandardDesignDocument();
    }
    
    public String findById(String id) {
         List<PreviewLabel> queryView = queryView("all", id);
         if(queryView.isEmpty())
             return null;
         return queryView.get(0).getLabel();
    }

}
