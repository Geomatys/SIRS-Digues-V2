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
package fr.sirs.plugin.document;

import fr.sirs.Plugin;
import java.io.IOException;
import java.util.Optional;
import javafx.scene.image.Image;

/**
 * Document d'ouvrage. Permet de générer des rapports concernant les ouvrages
 * présents sur les digues
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginDocument extends Plugin {
    private static final String NAME = "plugin-document";
    private static final String TITLE = "Module dossier d'ouvrages";

    public PluginDocument() {
        name = NAME;
        final FileTreeItem root = new FileTreeItem(false);
        loadingMessage.set("module dossier d'ouvrages");
        final DynamicDocumentTheme dynDcTheme = new DynamicDocumentTheme(root);
        themes.add(new DocumentManagementTheme(root, dynDcTheme));
        themes.add(dynDcTheme);

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
        return Optional.empty();
    }
}
