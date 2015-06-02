package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.AvecCommentaire;
import fr.sirs.core.model.AvecPhotos;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Photo;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;

/**
 * Un panneau qui affiche (en lecture seule) le commentaire et les éventuelles 
 * photos attachées à un objet.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class FXCommentPhotoView extends SplitPane {

    @FXML private ScrollBar uiPhotoScroll;
    @FXML private WebView uiCommentArea;
    @FXML private ImageView uiPhotoView;
    @FXML private Label uiPhotoLibelle;
    @FXML private Label uiPhotoDate;
    
    private final SimpleObjectProperty<Element> valueProperty = new SimpleObjectProperty<>();

    public FXCommentPhotoView() {
        SIRS.loadFXML(this);
        
        uiPhotoView.fitHeightProperty().bind(new DoubleBinding() {

            {
                bind(heightProperty());
            }
            
            @Override
            protected double computeValue() {
                return heightProperty().get() - uiPhotoLibelle.getHeight() - uiPhotoDate.getHeight();
            }
        });
        
        uiPhotoScroll.setBlockIncrement(1.0);
        uiPhotoScroll.setUnitIncrement(1.0);
        uiPhotoScroll.setMin(0);
        
        valueProperty.addListener(this::elementSet);
        
        uiPhotoScroll.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updatePhoto();
        });
    }
    
    private void elementSet(ObservableValue<? extends Element> observable, Element oldValue, Element newValue) {
        if (newValue instanceof AvecCommentaire) {
            uiCommentArea.getEngine().loadContent(((AvecCommentaire)newValue).getCommentaire());
        }
        if (newValue instanceof AvecPhotos) {
            final AvecPhotos photoContainer = (AvecPhotos) newValue;
            uiPhotoScroll.setVisible(true);
            if (photoContainer.getPhotos() != null && !photoContainer.getPhotos().isEmpty()) {
                uiPhotoScroll.setMax(photoContainer.getPhotos().size() - 1);
            } else {
                uiPhotoScroll.setMax(0);
            }
        } else if (newValue instanceof Photo) {
            uiPhotoScroll.setVisible(true);
            uiPhotoScroll.setMax(0);
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
        
        uiPhotoView.setImage(null);
        
        final Photo selected;
        final Object value = valueProperty.get();
        if (value instanceof AvecPhotos) {
            final List<Photo> photos = ((AvecPhotos)value).getPhotos();
            final int imageIndex = uiPhotoScroll.valueProperty().intValue();
            if (photos == null || photos.isEmpty() || imageIndex > photos.size()) {
                selected = null;
            } else {
                selected = photos.get(imageIndex);
            }
            
        } else if (value instanceof Photo) {
            selected = (Photo)value;
        } else {
            selected = null;
        }
        
        if (selected == null) {
            uiPhotoLibelle.setText("");
        } else if (selected.getChemin()==null || selected.getChemin().isEmpty()) {
            uiPhotoLibelle.setText("Aucun fichier n'est associé à la photo.");
        } else {
            try {
                final Path imagePath = SIRS.getDocumentAbsolutePath(selected.getChemin());
                // TODO : How to manage image loading error ? No exception is thrown here...
                uiPhotoView.setImage(new Image(imagePath.toUri().toURL().toExternalForm()));
                uiPhotoLibelle.textProperty().bind(selected.commentaireProperty());
                // Do not bind directly date as string because it can return ugly "null" text.
                uiPhotoDate.textProperty().bind(
                        Bindings.createStringBinding(() -> {
                            String result =  selected.dateProperty().asString().get();
                            if (result == null || result.equals("null")) 
                                result = "";
                            return result;
                        }, selected.dateProperty()));
            } catch (IllegalStateException e) {
                uiPhotoLibelle.setText(e.getLocalizedMessage());
            } catch (IllegalArgumentException | MalformedURLException e) {
                uiPhotoLibelle.setText("Le chemin de l'image est invalide : " + selected.getLibelle());
            }
        }
        /* We want the image to be resized to fit it's stage bounding box, while
         * keeping its proportions as the original image.
         * /!\ We are forced to repeat this operation each time we change
         * image, because the ImageView internally reset all its size 
         * properties to fit new image dimension.
         */
        uiPhotoView.minWidth(0);
        uiPhotoView.minHeight(0);
    }
}