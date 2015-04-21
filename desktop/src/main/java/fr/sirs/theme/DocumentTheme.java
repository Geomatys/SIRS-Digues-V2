package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.SIRSDocument;
import fr.sirs.theme.ui.PojoTable;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Parent;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public class DocumentTheme<T extends SIRSDocument> extends Theme {

    private final Class<T> documentClass;
    private static final Map<Class<? extends SIRSDocument>, String> THEME_NAMES = new HashMap<>();
    
    static{
        THEME_NAMES.put(Convention.class, "Conventions");
        THEME_NAMES.put(Marche.class, "Marchés");
        THEME_NAMES.put(RapportEtude.class, "Rapports d'étude");
        THEME_NAMES.put(DocumentGrandeEchelle.class, "Documents à grande échelle");
        THEME_NAMES.put(ArticleJournal.class, "Articles de presse");
        THEME_NAMES.put(ProfilTravers.class, "Profils en travers");
    }
        
    public DocumentTheme(final Class<T> documentClass) {
        super(THEME_NAMES.get(documentClass), Type.OTHER);
        this.documentClass = documentClass;
    }
    
    public DocumentTheme(final Class<T> documentClass, final String name) {
        super(name, Type.OTHER);
        this.documentClass = documentClass;
    }

    @Override
    public Parent createPane() {
        return new PojoTable(Injector.getSession().getRepositoryForClass(documentClass), getName());
    }
    
}
