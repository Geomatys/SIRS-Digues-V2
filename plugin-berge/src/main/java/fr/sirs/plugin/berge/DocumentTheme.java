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
package fr.sirs.plugin.berge;

import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.SIRSDocument;
import fr.sirs.plugin.berge.ui.AbstractDescriptionPane;
import fr.sirs.plugin.berge.util.TabContent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DocumentTheme extends AbstractDescriptionTheme {
    
    public DocumentTheme() {
        super("Documents localisés", "Documents localisés");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent(getTitle(ArticleJournal.class), getTitle(ArticleJournal.class), ArticleJournal.class));
        content.add(new TabContent(getTitle(Marche.class), getTitle(Marche.class), Marche.class));
        content.add(new TabContent(getTitle(RapportEtude.class), getTitle(RapportEtude.class), RapportEtude.class));
        content.add(new TabContent(getTitle(DocumentGrandeEchelle.class), getTitle(DocumentGrandeEchelle.class), DocumentGrandeEchelle.class));
        content.add(new TabContent("Profils en long", "Profils en long", ProfilLong.class));
        content.add(new TabContent("Positions de profils en travers", "Positions de profils en travers", PositionProfilTravers.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }

    private static <T extends SIRSDocument> String getTitle(final Class<T> documentClass){
        return LabelMapper.get(documentClass).mapClassName(true)+" (localisations)";
    }
}
