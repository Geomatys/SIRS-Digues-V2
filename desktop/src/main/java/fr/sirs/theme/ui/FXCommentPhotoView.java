package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Photo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
        uiPhotoScroll.setMin(0);
        
        valueProperty.addListener((ObservableValue<? extends Element> observable, Element oldValue, Element newValue) -> {
            setElement(newValue);
        });
        
        uiPhotoScroll.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                uiPhotoLibelle.setText("");
                uiPhotoDate.setText("");
                if (! (valueProperty.get() instanceof Objet)) return;
                
                final ObservableList<Photo> photos = ((Objet)valueProperty.get()).photo;
                final int imageIndex = newValue.intValue();
                if (photos == null || photos.isEmpty() || imageIndex > photos.size()) {
                    return;
                }
                
                final Photo selected = photos.get(imageIndex);
                
                // TODO : How to manage image loading error ? No exception is thrown here...
                uiPhotoView.setImage(new Image(selected.getReference()));
                /* We want the image to be resized to fit it's stage bounding box, while
                 * keeping its proportions as the original image.
                 * /!\ We are forced to repeat this operation each time we change
                 * image, because the ImageView internally reset all its size 
                 * properties to fit new image dimension.
                 */
                uiPhotoView.minWidth(0);
                uiPhotoView.minHeight(0);
                uiPhotoView.fitWidthProperty().bind(widthProperty());
                uiPhotoView.fitHeightProperty().bind(heightProperty());
                uiPhotoView.setPreserveRatio(true);
        
                uiPhotoLibelle.textProperty().bind(selected.libelleProperty());
                uiPhotoDate.textProperty().bind(selected.dateProperty().asString());
            }
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
    }
    
    public ObjectProperty<Element> valueProperty() {
        return valueProperty;
    }
    
    public ObjectProperty<Element> objetProperty() {
        return valueProperty;
    }
}