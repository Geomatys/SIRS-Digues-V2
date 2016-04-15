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
package fr.sirs.plugin.carto;

import fr.sirs.Plugin;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javafx.scene.image.Image;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PluginCarto extends Plugin {
    private static final String NAME = "plugin-carto";
    private static final String TITLE = "Module cartographie";

    public PluginCarto() {
        name = NAME;
        loadingMessage.set("Chargement du module de cartographie");
        themes.add(new AddLayerTheme());
        themes.add(new BookMarkTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
    }

    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    @Override
    public Image getImage() {
        // TODO: choisir une image pour ce plugin
        return null;
    }

    @Override
    public Optional<Image> getModelImage() throws IOException {
        final Image image;

        try (final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("fr/sirs/cartoModel.png")) {
            image = new Image(in);
        }
        return Optional.of(image);
    }
}
