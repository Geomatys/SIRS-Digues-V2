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

import fr.sirs.core.model.DesordreBerge;
import fr.sirs.plugin.berge.ui.AbstractDescriptionPane;
import fr.sirs.plugin.berge.util.TabContent;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Estelle Idee (Geomatys)
 */
public class DesordresTheme extends AbstractDescriptionTheme {

    public DesordresTheme() {
        super("Désordres", "Désordres");
    }

    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("", "Tableau des désordres (Berge)", DesordreBerge.class));
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }

}