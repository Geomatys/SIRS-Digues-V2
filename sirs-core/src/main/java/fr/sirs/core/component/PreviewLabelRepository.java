package fr.sirs.core.component;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.PreviewLabel;
import java.util.function.Predicate;
import org.apache.sis.util.ArgumentChecks;

@View(name = "all", map = "classpath:PreviewLabel-map.js")
public class PreviewLabelRepository extends
        CouchDbRepositorySupport<PreviewLabel> {

    public PreviewLabelRepository(CouchDbConnector couchDbConnector) {
        super(PreviewLabel.class, couchDbConnector);
        initStandardDesignDocument();
    }

    public String getPreview(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        List<PreviewLabel> res = db.queryView(createQuery("all").includeDocs(false)
                .key(id), PreviewLabel.class);
        if (res.isEmpty())
            return null;
        return res.get(0).getLabel();
    }
    
    public PreviewLabel getPreviewLabel(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        List<PreviewLabel> res = db.queryView(createQuery("all").includeDocs(false)
                .key(id), PreviewLabel.class);
        if (res.isEmpty())
            return null;
        return res.get(0);
    }
    
    public List<PreviewLabel> getPreviewLabels(){
        return db.queryView(createQuery("all").includeDocs(false), PreviewLabel.class);
    }

    public List<PreviewLabel> getPreviewLabels(Class type) {
        ArgumentChecks.ensureNonNull("Element type", type);
        return getPreviewLabels(type.getCanonicalName());
    }

    public List<PreviewLabel> getPreviewLabels(final String type) {
        ArgumentChecks.ensureNonNull("Element type", type);
        final List<PreviewLabel> previews = db.queryView(createQuery("all").includeDocs(false), PreviewLabel.class);
        previews.removeIf((PreviewLabel t) -> !type.equals(t.getType()));
        return previews;
    }
    
    
}
