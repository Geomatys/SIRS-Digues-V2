package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.core.model.Element;
import java.awt.Color;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @param <P> Container element type (parent)
 * @param <C> Contained element type (child)
 */
public class FXComponentField<P extends Element, C extends Element> extends HBox {
    
    private static final Image ICON_FORWARD = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXTERNAL_LINK, 16, Color.DARK_GRAY), null);
    protected final Button openPathButton = new Button("", new ImageView(ICON_FORWARD));
    private static final Image ICON_ADD = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PLUS, 16, Color.DARK_GRAY),null);
    protected final Button addButton = new Button(null, new ImageView(ICON_ADD));
    private static final Image ICON_REMOVE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O, 16, Color.DARK_GRAY),null);
    protected final Button removeButton = new Button(null, new ImageView(ICON_REMOVE));
    
    protected final Label label = new Label();
    
    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(false);
    
    private final ChangeListener<C> changeListener;
    
    private Class<C> childClass;
    private ObjectProperty<C> property;
    private P parent;
    
    
    public FXComponentField() {
        
        setSpacing(10);
        
        removeButton.setOnAction((ActionEvent event) -> {
            final Alert alert = new Alert(Alert.AlertType.NONE, "L'élément sera définitivement supprimé.", ButtonType.OK, ButtonType.CANCEL);
            alert.setResizable(true);
            final Optional<ButtonType> answer = alert.showAndWait();
            if(answer.isPresent() && answer.get()==ButtonType.OK){
                FXComponentField.this.property.set(null);
            }
        });
        
        openPathButton.setOnAction((ActionEvent event) -> {
            Injector.getSession().showEditionTab(property.get());
        });
        
        addButton.setOnAction((ActionEvent event) -> {
            final Alert alert = new Alert(Alert.AlertType.NONE, "Vous allez créer un élément.", ButtonType.OK);
            alert.setResizable(true);
            final Optional<ButtonType> answer = alert.showAndWait();
            if(answer.isPresent() && answer.get()==ButtonType.OK){
                final C childElement = Injector.getSession().getElementCreator().createElement(childClass);
                childElement.getId(); // Il faut attribuer un ID à l'élément en invoquant la méthode getId() de manière à ce que son panneau soit correctement indexé.
                property.set(childElement);
                childElement.setParent(parent);
                Injector.getSession().showEditionTab(property.get());
            }
        });
        
        label.setPrefWidth(USE_COMPUTED_SIZE);
        label.setPrefHeight(getHeight());
        label.setAlignment(Pos.CENTER_LEFT);
        
        getChildren().add(addButton);
        getChildren().add(openPathButton);
        getChildren().add(removeButton);
        getChildren().add(label);
        
        
        changeListener = new WeakChangeListener<>(new ChangeListener<C>() {
            @Override
            public void changed(ObservableValue<? extends C> observable, C oldValue, C newValue) {
                label.textProperty().unbind();
                if(newValue!=null){
                    label.textProperty().bind(newValue.designationProperty());
                }
            }
        });
    }
    
    public void initChildClass(final Class<C> childClass){
        this.childClass = childClass;
    }
    
    public void setParent(final P parent, final ObjectProperty<C> property){
        this.parent = parent;
        this.property = property;
        this.property.addListener(changeListener);
        removeButton.disableProperty().bind(property.isNull().or(disableFieldsProperty));
        addButton.disableProperty().bind(property.isNull());
        addButton.disableProperty().bind(property.isNotNull().or(disableFieldsProperty));
        label.textProperty().bind(property.get().designationProperty());
    }
    
}
