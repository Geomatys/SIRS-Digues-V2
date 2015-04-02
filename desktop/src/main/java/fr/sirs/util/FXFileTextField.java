/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import fr.sirs.util.property.SirsPreferences;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    
    public FXFileTextField() {
        rootPath.addListener(this::updateRoot);
        rootPath.set(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.DOCUMENT_ROOT));        
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
            return new URI(inputText);
        } else {
            return Paths.get(rootPath.get(), inputText == null? "" : inputText).toUri();
        }
    }
    
}
