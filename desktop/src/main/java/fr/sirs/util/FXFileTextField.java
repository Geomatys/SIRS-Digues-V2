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
package fr.sirs.util;

import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.util.property.DocumentRoots;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.net.URI;
import java.nio.file.Path;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXFileTextField extends FXFileWithoutRefTextField {

    public final SimpleObjectProperty<SIRSFileReference> refProperty = new SimpleObjectProperty<>();

    public FXFileTextField() {
        super();
        refProperty.addListener(this::updateRef);
    }

    private void updateRef(final ObservableValue<? extends SIRSFileReference> obs, final SIRSFileReference oldRef, final SIRSFileReference newRef) {
        final Path tmpRoot = DocumentRoots.getRoot(newRef).orElse(null);
        rootPath.set(tmpRoot);
    }

    @Override
    protected URI getURIForText(String inputText) throws Exception {
        updateRef(refProperty, null, refProperty.get()); // Force root update.
        return super.getURIForText(inputText);
    }

}
