package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Photo;
import java.net.MalformedURLException;
import java.nio.file.Path;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;

/**
 * Un panneau qui affiche (en lecture seule) le commentaire et les éventuelles 
 * photos attachées à un objet.
 * 
 * TODO : Etendre la gestion à des éléments qui ne sont pas forcément des {@link Objet}
 * 
 * @author Alexis Manin (Geomatys)
 */
public class FXCommentPhotoView extends SplitPane {

    @FXML
    private ScrollBar uiPhotoScroll;

    @FXML
    private WebView uiCommentArea;

    @FXML
    private ImageView uiPhotoView;
    
    @FXML
    private Label uiPhotoLibelle;
    
    @FXML
    private Label uiPhotoDate;
    
    private final SimpleObjectProperty<Element> valueProperty = new SimpleObjectProperty<>();

    public FXCommentPhotoView() {
        SIRS.loadFXML(this);
        uiPhotoScroll.setBlockIncrement(1.0);
        uiPhotoScroll.setUnitIncrement(1.0);
        uiPhotoScroll.setMin(0);
        
        valueProperty.addListener((ObservableValue<? extends Element> observable, Element oldValue, Element newValue) -> {
            setElement(newValue);
        });
        
        uiPhotoScroll.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updatePhoto();
        });
    }
    
    public void setElement(final Element input) {
        if (!(input instanceof Objet)) {
            return;
        }
        final Objet obj = (Objet) input;
        uiCommentArea.getEngine().loadContent(obj.getCommentaire());
        if (obj.photo != null && obj.photo.size() > 1 ) {
            uiPhotoScroll.setVisible(true);
            uiPhotoScroll.setMax(obj.photo.size()-1);
        } else {
            uiPhotoScroll.setVisible(false);
        }
        
        uiPhotoScroll.setValue(0);
        updatePhoto();
    }
    
    public ObjectProperty<Element> valueProperty() {
        return valueProperty;
    }
    
    public ObjectProperty<Element> objetProperty() {
        return valueProperty;
    }

    public void updatePhoto() {
        uiPhotoLibelle.textProperty().unbind();
        uiPhotoLibelle.setText("Pas de photo associée");
        uiPhotoDate.textProperty().unbind();
        uiPhotoDate.setText("");
        if (!(valueProperty.get() instanceof Objet)) {
            return;
        }

        final ObservableList<Photo> photos = ((Objet) valueProperty.get()).photo;
        final int imageIndex = uiPhotoScroll.valueProperty().intValue();
        if (photos == null || photos.isEmpty() || imageIndex > photos.size()) {
            return;
        }

        final Photo selected = photos.get(imageIndex);

        try {
            /* TODO : It appears that image relative path is stored in 
             * libelle property. It's a bit strange, it should be watched.
             */
            final Path imagePath = SIRS.getDocumentAbsolutePath(selected.getReferenceNumerique());
            // TODO : How to manage image loading error ? No exception is thrown here...
            uiPhotoView.setImage(new Image(imagePath.toUri().toURL().toExternalForm()));
            uiPhotoLibelle.textProperty().bind(selected.commentaireProperty());
            uiPhotoDate.textProperty().bind(selected.dateProperty().asString());
        } catch (IllegalStateException e) {
            uiPhotoLibelle.setText(e.getLocalizedMessage());
        } catch (IllegalArgumentException | MalformedURLException e) {
            uiPhotoLibelle.setText("Le chemin de l'image est invalide : " + selected.getLibelle());
        }
        /* We want the image to be resized to fit it's stage bounding box, while
         * keeping its proportions as the original image.
         * /!\ We are forced to repeat this operation each time we change
         * image, because the ImageView internally reset all its size 
         * properties to fit new image dimension.
         */
        uiPhotoView.minWidth(0);
        uiPhotoView.minHeight(0);
        uiPhotoView.fitWidthProperty().bind(((Region)uiPhotoView.getParent()).widthProperty());
        uiPhotoView.fitHeightProperty().bind(((Region)uiPhotoView.getParent()).heightProperty());
        uiPhotoView.setPreserveRatio(true);
    }
}