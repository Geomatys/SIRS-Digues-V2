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

import fr.sirs.core.model.EpiBerge;
import fr.sirs.core.model.OuvrageRevancheBerge;
import fr.sirs.core.model.PiedBerge;
import fr.sirs.core.model.SommetBerge;
import fr.sirs.core.model.TalusBerge;
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
public class StructureElementTheme extends AbstractDescriptionTheme {

    public StructureElementTheme() {
        super("Elements de structure", "Elements de structure");
    }

    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Sommet", "Tableau des sommets", SommetBerge.class));
        content.add(new TabContent("Talus", "Tableau des talus", TalusBerge.class));
        content.add(new TabContent("Pieds de berge", "Tableau des pieds de berge", PiedBerge.class));
        content.add(new TabContent("Epis", "Tableau des épis", EpiBerge.class));
        content.add(new TabContent("Ouvrage de revanche", "Tableau des ouvrage de revanche", OuvrageRevancheBerge.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
