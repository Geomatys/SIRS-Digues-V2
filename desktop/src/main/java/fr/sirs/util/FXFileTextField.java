/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.util.property.SirsPreferences;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.FileChooser;
import org.geotoolkit.gui.javafx.util.AbstractPathTextField;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXFileTextField extends AbstractPathTextField {

    private final SimpleStringProperty rootPath = new SimpleStringProperty();

    public final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty();

    public FXFileTextField() {
        rootPath.addListener(this::updateRoot);
        rootPath.set(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.DOCUMENT_ROOT));

        inputText.disableProperty().bind(disableFieldsProperty);
        choosePathButton.disableProperty().bind(disableFieldsProperty);
    }

    private void updateRoot(final ObservableValue<? extends String> obs, final String oldValue, final String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            completor.root = null;
        } else {
            completor.root = Paths.get(newValue);
        }
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
        rootPath.set(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.DOCUMENT_ROOT));
        if (rootPath.get() == null) {
            return inputText.matches("[A-Za-z]+://.+")? new URI(inputText) : Paths.get(inputText).toUri();
        } else if (inputText == null || inputText.isEmpty()) {
            return Paths.get(rootPath.get()).toUri();
        } else {
            return SIRS.getDocumentAbsolutePath(inputText == null? "" : inputText).toUri();
        }
    }

    public URI getURI() {
        try{
            return getURIForText(getText());
        } catch(Exception e){
            SIRS.LOGGER.log(Level.FINEST, "Unable to build URI from "+getText());
            return null;
        }
    }

}
