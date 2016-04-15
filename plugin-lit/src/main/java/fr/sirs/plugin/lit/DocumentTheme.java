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
package fr.sirs.plugin.lit;

import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.plugin.lit.ui.AbstractDescriptionPane;
import fr.sirs.plugin.lit.util.TabContent;
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
        super("Documents", "Documents");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Articles", "Tableau des articles de journaux", ArticleJournal.class));
        content.add(new TabContent("Marchés", "Tableau des marchés", Marche.class));
        content.add(new TabContent("Rapports d'étude", "Tableau des rapports d'étude", RapportEtude.class));
        content.add(new TabContent("Documents à grande échelle", "Tableau des documents à grande échelle", DocumentGrandeEchelle.class));
        content.add(new TabContent("Profils en long", "Tableau des profils en long", ProfilLong.class));
        content.add(new TabContent("Profils en travers", "Tableau des profils en travers", PositionProfilTravers.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
