package fr.sirs.core.component;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.PreviewLabel;

@View(name = "all", map = "function(doc) {"
        + "if(doc.libelle) {"
        + " emit(doc._id, doc.libelle) "
        + "} else if (doc.nom) { "
        + "emit(doc._id, doc.nom) "
        + "}"
        + "}")
public class PreviewLabelRepository extends
        CouchDbRepositorySupport<PreviewLabel> {

    public PreviewLabelRepository(CouchDbConnector couchDbConnector) {
        super(PreviewLabel.class, couchDbConnector);
        initStandardDesignDocument();
    }

    public String getPreview(String id) {

        List<String> res = db.queryView(createQuery("all").includeDocs(false)
                .key(id), String.class);
        if (res.isEmpty())
            return null;
        return res.get(0);
    }

}
