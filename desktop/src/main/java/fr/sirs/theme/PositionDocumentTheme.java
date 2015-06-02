package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.SIRSDocument;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.collections.FXCollections;


/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PositionDocumentTheme extends AbstractTronconTheme {
    
    private static final ThemeManager CONVENTION = generateThemeManager(PositionDocument.class, Convention.class);
    private static final ThemeManager ARTICLE = generateThemeManager(PositionDocument.class, ArticleJournal.class);
    private static final ThemeManager MARCHE = generateThemeManager(PositionDocument.class, Marche.class);
    private static final ThemeManager RAPPORT_ETUDE = generateThemeManager(PositionDocument.class, RapportEtude.class);
    private static final ThemeManager DOCUMENT_GRANDE_ECHELLE = generateThemeManager(PositionDocument.class, DocumentGrandeEchelle.class);
    private static final ThemeManager AUTRE = generateThemeManager(PositionDocument.class, null);
    private static final ThemeManager PROFIL_LONG = generateThemeManager(ProfilLong.class);
    private static final ThemeManager PROFIL_TRAVERS = generateThemeManager(PositionProfilTravers.class);
    
    public PositionDocumentTheme() {
        super("Documents localisés", CONVENTION, ARTICLE, MARCHE, RAPPORT_ETUDE, DOCUMENT_GRANDE_ECHELLE, AUTRE, PROFIL_LONG, PROFIL_TRAVERS);
    }
    
    private static <T extends Positionable, D extends SIRSDocument> ThemeManager<T> generateThemeManager(final Class<T> themeClass, Class<D> documentClass){
        final String title;
        if(documentClass!=null){
            final ResourceBundle bundle = ResourceBundle.getBundle(documentClass.getCanonicalName());
            title = bundle.getString(SIRS.BUNDLE_KEY_CLASS);
        } else{
            title = "sans document associé";
        }
        return new ThemeManager<>(title, 
                "Thème "+title, 
                themeClass,               
            (String linearId) -> {
                return FXCollections.observableArrayList(FXCollections.observableArrayList(((AbstractPositionableRepository<T>) Injector.getSession().getRepositoryForClass(themeClass)).getByLinearId(linearId)).filtered(new DocumentPredicate(documentClass)));
            },
            (T c) -> Injector.getSession().getRepositoryForClass(themeClass).remove(c));
    }
    
    private static class DocumentPredicate<T extends SIRSDocument> implements Predicate<AbstractPositionDocumentAssociable>{
        
        private final Class<T> documentClass;
        private final Map<String, String> cache;
        
        DocumentPredicate(final Class<T> documentClass){
            this.documentClass = documentClass;
            cache = new HashMap<>();
            if(documentClass!=null){
                List<Preview> previews = Injector.getSession().getPreviews().getByClass(documentClass);
                for(final Preview preview : previews){
                    cache.put(preview.getElementId(), preview.getElementClass());
                }
            }
        }

        @Override
        public boolean test(AbstractPositionDocumentAssociable t) {
            final String documentId = t.getSirsdocument();
            if(documentId!=null && documentClass!=null){
                if(documentClass.getName().equals(cache.get(documentId))){
                    return true;
                }
            } 
            // Dans le cas où documentClass==null, on retourne les positions de documents non associées à des documents.
            else if(documentId==null && documentClass==null){
                return true;
            }
            return false;
        }
    }
}
