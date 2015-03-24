/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import fr.sirs.SIRS;
import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javafx.beans.DefaultProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.internal.Loggers;

/**
 * A custom component which contains a text field designed to contain a file path.
 * 
 * Note : Override {@link #chooseInputContent() } method, to allow user to choose a path
 * when he clicks on {@link #chooseFileButton}.
 * 
 * @author Alexis Manin (Geomatys)
 */
@DefaultProperty("text")
public abstract class AbstractPathTextField extends HBox {
    
    public static final Image ICON_FIND = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FOLDER_OPEN, 16, Color.DARK_GRAY), null);
    public static final Image ICON_FORWARD = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXTERNAL_LINK, 16, Color.DARK_GRAY), null);

    protected final TextField inputText = new TextField();
    private final StringProperty textProperty = inputText.textProperty();
    
    protected final DirectoryContent completor = new DirectoryContent(inputText);
    
    protected final Button choosePathButton = new Button("", new ImageView(ICON_FIND));
    protected final Button openPathButton = new Button("", new ImageView(ICON_FORWARD));
    
    public AbstractPathTextField() {
        choosePathButton.setOnAction((ActionEvent e)-> {
            final String content = chooseInputContent();
            if (content != null) {
                setText(content);
            }
        });
        
        inputText.setMinSize(0, USE_PREF_SIZE);
        inputText.setMaxSize(Double.MAX_VALUE, USE_PREF_SIZE);
        
        // TODO : put style rules in CSS
        choosePathButton.setBackground(new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY)));
        choosePathButton.setBorder(Border.EMPTY);
        choosePathButton.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        
        openPathButton.setBackground(new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY)));
        openPathButton.setBorder(Border.EMPTY);
        openPathButton.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        
        final SimpleBooleanProperty notValidPath = new SimpleBooleanProperty(true);
        textProperty.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            notValidPath.set((textProperty.get() == null || textProperty.get().isEmpty()));
        });
        
        openPathButton.disableProperty().bind(notValidPath);
        
        setAlignment(Pos.CENTER);
        setSpacing(5);
        getChildren().addAll(inputText, choosePathButton);
        
        if (Desktop.isDesktopSupported()) {
            getChildren().add(openPathButton);
            openPathButton.setOnAction((ActionEvent e) -> {
                TaskManager.INSTANCE.submit(new OpenOnSystem(textProperty.get()));
            });
        }
    }
    
    public String getText() {
        return textProperty.get();
    }
    
    public void setText(final String input) {
        textProperty.set(input);
    }
    
    public StringProperty textProperty() {
        return textProperty;
    }
    
    protected abstract String chooseInputContent();
    
    protected abstract URI getURIForText(final String inputText) throws Exception;
    
    private class OpenOnSystem extends Task {

        private final String inputText;
        
        OpenOnSystem(final String inputText) {
            super();
            this.inputText = inputText;
            updateTitle("Ouverture d'un fichier...");
        }
        
        @Override
        protected Object call() throws Exception {
            final URI toOpen = getURIForText(inputText);
            // First, we try to open input file as a local file, to allow system to find best application to open it.
            try {
                Path local = Paths.get(toOpen.toString());
                Desktop.getDesktop().open(local.toFile());
                return null;
            } catch (Exception ex) {
                Loggers.JAVAFX.log(Level.FINE, "Input URI cannot be opened as a local file : " + toOpen, ex);
            }

            Desktop.getDesktop().browse(toOpen);
            return null;
        }
    }
    
}
