package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.util.property.SirsPreferences;
import java.awt.Color;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import static org.geotoolkit.gui.javafx.util.AbstractPathTextField.ICON_FORWARD;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @param <P> Container element type (parent)
 * @param <C> Contained element type (child)
 */
public class FXComponentField<P, C> extends HBox {
    
    private static final Image ICON_FORWARD = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXTERNAL_LINK, 16, Color.DARK_GRAY), null);
    protected final Button openPathButton = new Button("", new ImageView(ICON_FORWARD));
    private static final Image ICON_ADD = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PLUS, 16, Color.DARK_GRAY),null);
    protected final Button addButton = new Button(null, new ImageView(ICON_ADD));
    private static final Image ICON_REMOVE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O, 16, Color.DARK_GRAY),null);
    protected final Button removeButton = new Button(null, new ImageView(ICON_REMOVE));

    private final SimpleStringProperty rootPath = new SimpleStringProperty();
    
    public final SimpleBooleanProperty disableFieldsProperty = new SimpleBooleanProperty();
    public final ObjectProperty<C> property;
    
    public FXComponentField(final ObjectProperty<C> property) {
        this.property = property;
        
        removeButton.setOnAction((ActionEvent event) -> FXComponentField.this.property.set(null));
        addButton.setOnAction((ActionEvent event) -> {
//            Injector.getSession().getElementCreator().createElement(null)
        });
        
        getChildren().add(addButton);
        getChildren().add(openPathButton);
        getChildren().add(removeButton);
//        rootPath.addListener(this::updateRoot);
//        rootPath.set(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.DOCUMENT_ROOT));
//        
//        inputText.disableProperty().bind(disableFieldsProperty);
//        choosePathButton.disableProperty().bind(disableFieldsProperty);
    }
    
    private void updateRoot(final ObservableValue<? extends String> obs, final String oldValue, final String newValue) {
//        if (newValue == null || newValue.isEmpty()) {
//            completor.root = null;
//        } else {
//            completor.root = Paths.get(newValue);
//        }
    }
    
//    @Override
//    protected String chooseInputContent() {
//        final FileChooser chooser = new FileChooser();
//        try {
//            URI uriForText = getURIForText(getText());
//            final Path basePath = Paths.get(uriForText);
//            if (Files.isDirectory(basePath)) {
//                chooser.setInitialDirectory(basePath.toFile());
//            } else if (Files.isDirectory(basePath.getParent())) {
//                chooser.setInitialDirectory(basePath.getParent().toFile());
//            }
//        } catch (Exception e) {
//            // Well, we'll try without it...
//            SirsCore.LOGGER.log(Level.FINE, "Input path cannot be decoded.", e);
//        }
//        File returned = chooser.showOpenDialog(null);
//        if (returned == null) {
//            return null;
//        } else {
//            return (completor.root != null)? 
//                    completor.root.relativize(returned.toPath()).toString() : returned.getAbsolutePath();
//        }
//    }
//
//    @Override
//    protected URI getURIForText(String inputText) throws Exception {
//        rootPath.set(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.DOCUMENT_ROOT));
//        if (rootPath.get() == null) {
//            return inputText.matches("[A-Za-z]+://.+")? new URI(inputText) : Paths.get(inputText).toUri();
//        } else {
//            return SIRS.getDocumentAbsolutePath(inputText == null? "" : inputText).toUri();
//        }
//    }
//    
//    public URI getURI() {
//        try{
//            return getURIForText(getText());
//        } catch(Exception e){
//            SIRS.LOGGER.log(Level.FINEST, "Unable to build URI from "+getText());
//            return null;
//        }
//    }
    
}
