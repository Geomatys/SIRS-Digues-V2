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

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Element;
import java.awt.Color;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

/**
 *
 * @author Samuel Andrés (Geomatys)
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
    private final BooleanProperty deletableProperty = new SimpleBooleanProperty(true);
    
    /**
     * Controls the visibility of the delete button.
     * 
     * @return 
     */
    public final BooleanProperty deletableProperty(){return deletableProperty;}
    
    private final ChangeListener<C> changeListener;
    
    private Class<C> childClass;
    private ObjectProperty<C> property;
    private P parent;
    
    private ResourceBundle bundle;
    
    public FXComponentField() {
        
        setSpacing(10);
        
        removeButton.visibleProperty().bind(deletableProperty);
        removeButton.setOnAction((ActionEvent event) -> {
            final Alert alert = new Alert(Alert.AlertType.NONE, "Le/La "+bundle.getString(SIRS.BUNDLE_KEY_CLASS)+" sera définitivement supprimé(e).", ButtonType.OK, ButtonType.CANCEL);
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
            final Alert alert = new Alert(Alert.AlertType.NONE, "Vous allez créer un(e) "+bundle.getString(SIRS.BUNDLE_KEY_CLASS)+".", ButtonType.OK, ButtonType.CANCEL);
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
                    label.textProperty().bind(new LabelBinding(newValue.designationProperty()));
                } else {
                    label.setText("");
                }
            }
        });
    }

    final private class LabelBinding extends StringBinding {

        final StringProperty designationProperty;
        public LabelBinding(final StringProperty designationProperty){
            super();
            bind(designationProperty);
            this.designationProperty = designationProperty;
        }

        @Override
        protected String computeValue() {
            return computeLabel(designationProperty);
        }
    }
    
    private String computeLabel(final StringProperty designationProp) {
        if(designationProp!=null 
                && designationProp.get()!=null 
                && !"".equals(designationProp.get()))
            return "Désignation : " + bundle.getString(SIRS.BUNDLE_KEY_CLASS_ABREGE)+ " - " + designationProp.get();
        else
            return "";
    }
    
    public void initChildClass(final Class<C> childClass){
        this.childClass = childClass;
        bundle = ResourceBundle.getBundle(childClass.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
        openPathButton.setTooltip(new Tooltip("Accéder au/à la "+bundle.getString(SIRS.BUNDLE_KEY_CLASS)));
        removeButton.setTooltip(new Tooltip("Supprimer le/la "+bundle.getString(SIRS.BUNDLE_KEY_CLASS)));
        addButton.setTooltip(new Tooltip("Créer un(e) "+bundle.getString(SIRS.BUNDLE_KEY_CLASS)));
    }
    
    public void setParent(final P parent, final ObjectProperty<C> property){
        this.parent = parent;
        this.property = property;
        this.property.addListener(changeListener);
        removeButton.disableProperty().bind(property.isNull().or(disableFieldsProperty));
        openPathButton.disableProperty().bind(property.isNull());
        addButton.disableProperty().bind(property.isNotNull().or(disableFieldsProperty));
        if(property.get()!=null) label.textProperty().bind(new LabelBinding(property.get().designationProperty()));
    }
    
}
