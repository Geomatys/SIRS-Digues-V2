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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.geotoolkit.gui.javafx.util.AbstractPathTextField;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Hbox containing an error message in case the path isn't valid or the file does not exist.
 * Possibility to set the list of supported formats.
 *
 * @author Estelle Idée (Geomatys)
 */
public class FXFileWithoutRefTextField extends AbstractPathTextField {

    final SimpleObjectProperty<Path> rootPath = new SimpleObjectProperty<>();

    public final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty();

    protected Label ui_errorMessage = new Label();
    protected String supportedFormatsExtention;


    /**
     * Supported formats for timestamped files.
     */
    private final List<String> supportedFormats = new ArrayList<>();

    public FXFileWithoutRefTextField() {
        rootPath.addListener(this::updateRoot);

        inputText.disableProperty().bind(disableFieldsProperty);
        choosePathButton.disableProperty().bind(disableFieldsProperty);
        inputText.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                checkInputTextValid();
            }
        });

        // Override the OnAction set in the parent.
        choosePathButton.setOnAction((ActionEvent e)-> {
            final String content = chooseInputContent();
            if (content != null) {
                setText(content);
            }
            checkInputTextValid();
        });

        getChildren().addAll(ui_errorMessage);
        ui_errorMessage.setTextFill(Color.RED);
    }

    public void setSupportedFormats(final List<String> supportedFormats) {
        this.supportedFormats.clear();
        this.supportedFormatsExtention = "";
        if (supportedFormats != null) {
            final StringBuilder builder = new StringBuilder();
            this.supportedFormats.addAll(supportedFormats);
            for (String supportedFormat : this.supportedFormats) {
                final String format = supportedFormat.substring(supportedFormat.lastIndexOf("."));
                builder.append("\n- " + format);
            }
            this.supportedFormatsExtention = builder.toString();
        }
    }

    public void checkInputTextValid() {
        final String text = this.getText();
        if (text != null && !text.isEmpty()) {
            if (!supportedFormats.isEmpty()) {
                boolean validFormat = false;
                for (String supportedFormat : supportedFormats) {
                    if (text.endsWith(supportedFormat.replace("*", ""))) {
                        validFormat = true;
                        break;
                    }
                }
                if (!validFormat) {
                    ui_errorMessage.setText("Veuillez sélectionner un fichier au format : " + supportedFormatsExtention + ".");
                    return;
                }
            }

            final File syntheseFile = new File(text);
            if (!syntheseFile.exists()) {
                ui_errorMessage.setText("Le fichier est introuvable.");
                return;
            }
        }
        ui_errorMessage.setText("");
    }

    private void updateRoot(final ObservableValue<? extends Path> obs, final Path oldValue, final Path newValue) {
        completor.root = newValue;
    }

    @Override
    protected String chooseInputContent() {
        final FileChooser chooser = new FileChooser();
        if (!supportedFormats.isEmpty()) {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported formats for cover and conclusion pages", supportedFormats));
        }
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
