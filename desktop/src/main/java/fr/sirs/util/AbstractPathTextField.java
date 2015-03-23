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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.beans.DefaultProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
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
 * Note : Override {@link #choosePath() } method, to allow user to choose a path
 * when he clicks on {@link #chooseFileButton}.
 * 
 * @author Alexis Manin (Geomatys)
 */
@DefaultProperty("text")
public abstract class AbstractPathTextField extends HBox {
    
    public static final Image ICON_FIND = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FOLDER_OPEN, 16, Color.DARK_GRAY), null);
    public static final Image ICON_FORWARD = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FORWARD, 16, Color.DARK_GRAY), null);

    private final TextField inputText = new TextField();
    private final StringProperty textProperty = inputText.textProperty();
    
    private final DirectoryContent completor = new DirectoryContent(inputText);
    
    private final Button choosePathButton = new Button("", new ImageView(ICON_FIND));
    private final Button openPathButton = new Button("", new ImageView(ICON_FORWARD));
    
    public AbstractPathTextField() {
        choosePathButton.setOnAction((ActionEvent e)-> {
            Loggers.JAVAFX.info("ACTION TRIGGERED !");
            Path chosen = choosePath();
            if (chosen != null) {
                setText(chosen.toString());
            }
        });
        
        // TODO : put style rules in CSS
        choosePathButton.setBackground(new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY)));
        choosePathButton.setBorder(Border.EMPTY);
        choosePathButton.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        
        openPathButton.setBackground(new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY)));
        openPathButton.setBorder(Border.EMPTY);
        openPathButton.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        
        final SimpleBooleanProperty notValidPath = new SimpleBooleanProperty(true);
        textProperty.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            notValidPath.set((textProperty == null || textProperty.get().isEmpty() || !Files.isReadable(Paths.get(textProperty.get()))));
        });
        
        openPathButton.disableProperty().bind(notValidPath);
        
        setSpacing(5);
        getChildren().addAll(inputText, choosePathButton);
        
        if (Desktop.isDesktopSupported()) {
            getChildren().add(openPathButton);
            openPathButton.setOnAction((ActionEvent e) -> {
                TaskManager.INSTANCE.submit("Ouverture d'un fichier...", () -> {
                    Path toOpen = Paths.get(textProperty.get()).toAbsolutePath();
                    try {
                        Desktop.getDesktop().open(toOpen.toFile());
                    } catch (IOException ex) {
                        SIRS.newExceptionDialog("Impossible d'ouvrir le fichier " + toOpen, ex);
                    }
                });
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
    
    protected abstract Path choosePath();
}
