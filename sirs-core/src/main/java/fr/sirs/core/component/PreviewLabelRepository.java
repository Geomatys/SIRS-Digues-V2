package fr.sirs.core.component;

import fr.sirs.core.SirsCore;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;

import fr.sirs.core.model.PreviewLabel;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.support.Views;

@Views({
    @View(name = "all", map = "classpath:PreviewLabel-map.js"),
    @View(name = "byClass", map = "classpath:PreviewLabelByClass-map.js")
})
public class PreviewLabelRepository extends
        AbstractSIRSRepository<PreviewLabel> {

    public PreviewLabelRepository(CouchDbConnector couchDbConnector) {
        super(PreviewLabel.class, couchDbConnector);
        initStandardDesignDocument();
    }

    public String getPreview(String id) {
        final PreviewLabel p = get(id);
        return p==null? null : p.getLabel();
    }
    
    @Override
    public PreviewLabel get(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            return cache.getOrCreate(id, () -> {
                List<PreviewLabel> res = db.queryView(createQuery("all").includeDocs(false)
                        .key(id), PreviewLabel.class);
                if (res == null || res.isEmpty()) {
                    throw new IllegalArgumentException("No result for given ID :" + id);
                }
                return res.get(0);
            });
        } catch (Exception ex) {
            SirsCore.LOGGER.log(Level.FINE, "No preview for id " + id, ex);
        }
        return null;
    }

    @Override
    public List<PreviewLabel> getAll() {
        List<PreviewLabel> all = db.queryView(createQuery("all").includeDocs(false), PreviewLabel.class);
        final ArrayList<PreviewLabel> result = new ArrayList<>(all.size());
        for (PreviewLabel pl : all) {
            try {
                result.add(cache.getOrCreate(pl.getId(), () -> {
                    return pl;
                }));
            } catch (Exception ex) {
                // Should never happen ...
                throw new RuntimeException(ex);
            }
        }
        return result;
    }
    
    public List<PreviewLabel> getPreviewLabels(Class type) {
        ArgumentChecks.ensureNonNull("Element type", type);
        if(type.isInterface()) return getPreviewLabelsFromInterface(type.getCanonicalName());
        else return getPreviewLabels(type.getCanonicalName());
    }

    public List<PreviewLabel> getPreviewLabels(final String type) {
        ArgumentChecks.ensureNonNull("Element type", type);
        final List<PreviewLabel> all = db.queryView(createQuery("byClass").includeDocs(false).key(type), PreviewLabel.class);
        final ArrayList<PreviewLabel> result = new ArrayList<>(all.size());
        for (PreviewLabel pl : all) {
            try {
                result.add(cache.getOrCreate(pl.getId(), () -> {
                    return pl;
                }));
            } catch (Exception ex) {
                // Should never happen ...
                throw new RuntimeException(ex);
            }
        }
        return result;
    }

    private List<PreviewLabel> getPreviewLabelsFromInterface(final String type) {
        ArgumentChecks.ensureNonNull("Element type", type);
        final List<PreviewLabel> previews = db.queryView(createQuery("all").includeDocs(false), PreviewLabel.class);
        previews.removeIf((PreviewLabel t) -> {
                try {
                    return !Class.forName(type).isAssignableFrom(Class.forName(t.getType()));
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(PreviewLabelRepository.class.getName()).log(Level.SEVERE, null, ex);
                }
                return false;
        });
        return previews;
    }

    @Override
    public Class<PreviewLabel> getModelClass() {
        return PreviewLabel.class;
    }

    @Override
    public PreviewLabel create() {
        return new PreviewLabel();
    }
}
