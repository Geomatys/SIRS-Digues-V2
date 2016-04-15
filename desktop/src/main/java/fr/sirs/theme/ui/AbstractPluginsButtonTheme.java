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
package fr.sirs.theme.ui;

import fr.sirs.theme.Theme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Boutons qui seront affichés dans la toolbar des plugins, après avoir sélectionné un plugin dans la liste
 * déroulante.
 *
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractPluginsButtonTheme extends Theme {
    /**
     * Description du bouton affichée au survol.
     */
    protected final String description;

    /**
     * Image à utiliser pour ce bouton.
     */
    protected final Image img;

    /**
     * Génère un bouton à partir du nom, de la description et de l'image fournie.
     *
     * @param name Nom du bouton. Non {@code null}.
     * @param description Description du bouton, sera affiché dans une tooltip.
     * @param img Image du bouton.
     */
    protected AbstractPluginsButtonTheme(String name, String description, Image img) {
        super(name, Type.PLUGINS);
        this.description = description;
        this.img = img;
    }

    /**
     * Description du bouton.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Image du bouton.
     */
    public Image getImg() {
        return img;
    }

    /**
     * {@inheritDoc}
     */
    public abstract Parent createPane();
}
