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

import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.util.property.DocumentRoots;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.FileChooser;
import org.geotoolkit.gui.javafx.util.AbstractPathTextField;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXFileTextField extends AbstractPathTextField {

    private final SimpleObjectProperty<Path> rootPath = new SimpleObjectProperty<>();

    public final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty();

    public final SimpleObjectProperty<SIRSFileReference> refProperty = new SimpleObjectProperty<>();

    public FXFileTextField() {
        refProperty.addListener(this::updateRef);
        rootPath.addListener(this::updateRoot);

        inputText.disableProperty().bind(disableFieldsProperty);
        choosePathButton.disableProperty().bind(disableFieldsProperty);
    }

    private void updateRef(final ObservableValue<? extends SIRSFileReference> obs, final SIRSFileReference oldRef, final SIRSFileReference newRef) {
        final Path tmpRoot = DocumentRoots.getRoot(newRef).orElse(null);
        rootPath.set(tmpRoot);
    }

    private void updateRoot(final ObservableValue<? extends Path> obs, final Path oldValue, final Path newValue) {
        completor.root = newValue;
    }

    @Override
    protected String chooseInputContent() {
        final FileChooser chooser = new FileChooser();
        try {
            URI uriForText = getURIForText(getText());
            final Path basePath = Paths.get(uriForText);
            if (Files.isDirectory(basePath)) {
                chooser.setInitialDirectory(basePath.toFile());
            } else if (Files.isDirectory(basePath.getParent())) {
                chooser.setInitialDirectory(basePath.getParent().toFile());
            }
        } catch (Exception e) {
            // Well, we'll try without it...
            SirsCore.LOGGER.log(Level.FINE, "Input path cannot be decoded.", e);
        }
        File returned = chooser.showOpenDialog(null);
        if (returned == null) {
            return null;
        } else {
            return (completor.root != null)?
                    completor.root.relativize(returned.toPath()).toString() : returned.getAbsolutePath();
        }
    }

    @Override
    protected URI getURIForText(String inputText) throws Exception {
        updateRef(refProperty, null, refProperty.get()); // Force root update.
        if (rootPath.get() == null) {
            return inputText.matches("[A-Za-z]+://.+")? new URI(inputText) : Paths.get(inputText).toUri();
        } else if (inputText == null || inputText.isEmpty()) {
            return rootPath.get().toUri();
        } else {
            return SIRS.concatenatePaths(rootPath.get(), inputText).toUri();
        }
    }

    public URI getURI() {
        try {
            return getURIForText(getText());
        } catch(Exception e) {
            SIRS.LOGGER.log(Level.FINEST, "Unable to build URI from "+getText());
            return null;
        }
    }

}
