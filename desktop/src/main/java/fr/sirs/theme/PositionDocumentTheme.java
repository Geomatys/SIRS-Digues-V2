/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.SIRSDocument;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;


/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PositionDocumentTheme extends TronconTheme {

    private static final ThemeManager ARTICLE = generateThemeManager(PositionDocument.class, ArticleJournal.class);
    private static final ThemeManager MARCHE = generateThemeManager(PositionDocument.class, Marche.class);
    private static final ThemeManager RAPPORT_ETUDE = generateThemeManager(PositionDocument.class, RapportEtude.class);
    private static final ThemeManager DOCUMENT_GRANDE_ECHELLE = generateThemeManager(PositionDocument.class, DocumentGrandeEchelle.class);
    private static final ThemeManager AUTRE = generateThemeManager(PositionDocument.class, null);
    private static final ThemeManager PROFIL_LONG = generateThemeManager(ProfilLong.class);
    private static final ThemeManager PROFIL_TRAVERS = generateThemeManager(PositionProfilTravers.class);

    public PositionDocumentTheme() {
        super("Documents localisés", ARTICLE, MARCHE, RAPPORT_ETUDE, DOCUMENT_GRANDE_ECHELLE, AUTRE, PROFIL_LONG, PROFIL_TRAVERS);
    }
   
    public static <T extends Positionable, D extends SIRSDocument> ThemeManager<T> generateThemeManager(final Class<T> themeClass, Class<D> documentClass){
        return generateThemeManager(null, themeClass, documentClass);
    }

    public static <T extends Positionable, D extends SIRSDocument> ThemeManager<T> generateThemeManager(final String specifiedTitle, final Class<T> themeClass, Class<D> documentClass){
        final String title;
        if(specifiedTitle != null) {
            title = specifiedTitle;
        } else if(documentClass != null) {
            title = LabelMapper.get(documentClass).mapClassName(true)+" (localisations)";
        } else{
            title = "Localisations sans document associé";
        }
        return new ThemeManager<>(title, 
                title, 
                themeClass,               
            (String linearId) -> {

                final FilteredList filtered = FXCollections.observableList(((AbstractPositionableRepository<T>) Injector.getSession().getRepositoryForClass(themeClass)).getByLinearId(linearId)).filtered(new DocumentPredicate(documentClass));
                // Copy the result in a new observable list to not return a FilteredList in which it is impossible to add new elements.
                return FXCollections.observableArrayList(filtered);
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
