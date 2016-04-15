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
package fr.sirs.plugin.reglementaire;

import fr.sirs.plugin.reglementaire.ui.RapportsPane;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Panneau regroupant les fonctionnalités de génération d'états.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class StatesGeneratorTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            StatesGeneratorTheme.class.getResourceAsStream("images/gen_etats.png"));

    public StatesGeneratorTheme() {
        super("Générateur d'états", "Générateur d'états", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        return new RapportsPane();
    }
    
}
