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

import fr.sirs.core.model.Element;
import fr.sirs.plugin.lit.ui.SuiviLitPane;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Exemple de bouton de plugins
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class SuiviLitTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            SuiviLitTheme.class.getResourceAsStream("images/lit-suivi.png"));

    SuiviLitPane pane;
    
    public SuiviLitTheme() {
        super("Suivi des lits", "Suivi des lits", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        if(pane==null) pane = new SuiviLitPane();
        return pane;
    }

    public void display(final Element element){
        if(pane==null) pane = new SuiviLitPane();
        pane.displayElement(element);
    }
}
