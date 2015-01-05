package fr.sirs.core.component;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import fr.sirs.core.model.PreviewLabel;
import java.util.function.Predicate;

@View(name = "all", map = "classpath:PreviewLabel-map.js")
public class PreviewLabelRepository extends
        CouchDbRepositorySupport<PreviewLabel> {

    public PreviewLabelRepository(CouchDbConnector couchDbConnector) {
        super(PreviewLabel.class, couchDbConnector);
        initStandardDesignDocument();
    }

    public String getPreview(String id) {
        List<PreviewLabel> res = db.queryView(createQuery("all").includeDocs(false)
                .key(id), PreviewLabel.class);
        if (res.isEmpty())
            return null;
        return res.get(0).getLabel();
    }
    
    public PreviewLabel getPreviewLabel(String id) {
        List<PreviewLabel> res = db.queryView(createQuery("all").includeDocs(false)
                .key(id), PreviewLabel.class);
        if (res.isEmpty())
            return null;
        return res.get(0);
    }
    
    public List<PreviewLabel> getPreviewLabels(){
        return db.queryView(createQuery("all").includeDocs(false), PreviewLabel.class);
    }

    public List<PreviewLabel> getPreviewLabels(Class type){
        if(type!=null){
            final List<PreviewLabel> previews = db.queryView(createQuery("all").includeDocs(false), PreviewLabel.class);
            
            previews.removeIf(new Predicate<PreviewLabel>() {

                @Override
                public boolean test(PreviewLabel t) {
                    return !type.getCanonicalName().equals(t.getType());
                }
            });
            return previews;
        }
        else{
            return null;
        }
    }
    
    
}
