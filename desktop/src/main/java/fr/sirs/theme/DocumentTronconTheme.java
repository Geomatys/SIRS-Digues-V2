

package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.core.component.PreviewLabelRepository;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.SIRSDocument;
import fr.sirs.core.model.TronconDigue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DocumentTronconTheme extends AbstractTronconTheme {
    
    private static final PreviewLabelRepository PREVIEW_LABEL_REPOSITORY = Injector.getSession().getPreviewLabelRepository();

    private static final ThemeGroup GROUP1 = new ThemeGroup("Conventions localisées", "Conventions localisées", PositionDocument.class, 
            (TronconDigue t) -> t.documentTroncon.filtered(new DocumentPredicate<Convention>(Convention.class)),
            (TronconDigue t, Object c) -> t.documentTroncon.remove(c));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Articles localisés", "Articles localisés", PositionDocument.class, 
            (TronconDigue t) -> t.documentTroncon.filtered(new DocumentPredicate<ArticleJournal>(ArticleJournal.class)),
            (TronconDigue t, Object c) -> t.documentTroncon.remove(c));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Marchés localisés", "Marchés localisés", PositionDocument.class, 
            (TronconDigue t) -> t.documentTroncon.filtered(new DocumentPredicate<Marche>(Marche.class)),
            (TronconDigue t, Object c) -> t.documentTroncon.remove(c));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Rapports d'étude localisés", "Rapports d'étude localisés", PositionDocument.class, 
            (TronconDigue t) -> t.documentTroncon.filtered(new DocumentPredicate<RapportEtude>(RapportEtude.class)),
            (TronconDigue t, Object c) -> t.documentTroncon.remove(c));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Documents à grande échelle localisés", "Documents à grande échelle localisés", PositionDocument.class, 
            (TronconDigue t) -> t.documentTroncon.filtered(new DocumentPredicate<DocumentGrandeEchelle>(DocumentGrandeEchelle.class)),
            (TronconDigue t, Object c) -> t.documentTroncon.remove(c));
    private static final ThemeGroup GROUP6 = new ThemeGroup("Profils en long localisés", "Profils en long localisés", PositionDocument.class, 
            (TronconDigue t) -> t.documentTroncon.filtered(new DocumentPredicate<ProfilLong>(ProfilLong.class)),
            (TronconDigue t, Object c) -> t.documentTroncon.remove(c));
    private static final ThemeGroup GROUP7 = new ThemeGroup("Profils en travers localisés", "Profils en travers localisés", PositionProfilTravers.class, 
            (TronconDigue t) -> t.documentTroncon.filtered(new DocumentPredicate<ProfilTravers>(ProfilTravers.class)),
            (TronconDigue t, Object c) -> t.documentTroncon.remove(c));
    
    
    
    public DocumentTronconTheme() {
        super("Documents localisés", GROUP1, GROUP2, GROUP3, GROUP4, GROUP5, GROUP6, GROUP7);
    }
    
    private static class DocumentPredicate<T extends SIRSDocument> implements Predicate{
        
        private final Class<T> documentClass;
        private final List<PreviewLabel> previews;
        private final Map<String, String> cache;
        
        DocumentPredicate(final Class<T> documentClass){
            this.documentClass = documentClass;
            previews = PREVIEW_LABEL_REPOSITORY.getPreviewLabels(documentClass);
            cache = new HashMap<>();
            for(final PreviewLabel preview : previews){
                cache.put(preview.getId(), preview.getType());
            }
        }

        @Override
        public boolean test(Object t) {
            final String documentId = ((AbstractPositionDocument) t).getSirsdocument();
            if(documentId!=null){
                if(documentClass.getName().equals(cache.get(documentId))){
                    return true;
                }
            }
            return false;
        }
    }
}
