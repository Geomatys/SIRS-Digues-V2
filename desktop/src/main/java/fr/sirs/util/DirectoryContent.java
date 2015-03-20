/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextInputControl;
import org.geotoolkit.internal.Loggers;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DirectoryContent extends TextFieldCompletion {
    
    public DirectoryContent(final TextInputControl source) {
        super(source);
    }

    @Override
    protected ObservableList<String> getChoices(String text) {
        final ArrayList<String> result = new ArrayList<>();

        try {
            Path origin = Paths.get(text);
            if (Files.isRegularFile(origin)) {
                result.add(origin.toString());
            } else if (Files.isDirectory(origin)) {
                Files.walk(origin, 1).forEach((final Path child) -> result.add(child.toString()));
            } else if (Files.isDirectory(origin.getParent())) {
                final String fileStart = origin.getFileName().toString().toLowerCase();
                Files.walk(origin.getParent(), 1)
                        .filter((final Path p) -> p.getFileName().toString().toLowerCase().startsWith(fileStart))
                        .forEach((final Path child) -> result.add(child.toString()));
            }
        } catch (Exception e) {
            Loggers.JAVAFX.log(Level.FINE, "Cannot find completion for input path", e);
        }
        return FXCollections.observableList(result);
    }
}
