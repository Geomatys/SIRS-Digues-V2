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
package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.AvecCommentaire;
import fr.sirs.core.model.AvecPhotos;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Observation;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 * Un panneau qui affiche (en lecture seule) le commentaire et les éventuelles
 * photos attachées à un objet.
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXCommentPhotoView extends SplitPane {

    @FXML private ScrollBar uiPhotoScroll;
    @FXML private Label uiCommentArea;
    @FXML private ImageView uiPhotoView;
    @FXML private Label uiPhotoLibelle;
    @FXML private Label uiPhotoDate;
    @FXML private ProgressIndicator uiImageProgress;

    private final SimpleObjectProperty<Element> valueProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<List<AbstractPhoto>> photos = new SimpleObjectProperty<>();

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
        uiPhotoScroll.setVisibleAmount(0.5);
        uiPhotoScroll.setMin(0);

        valueProperty.addListener(this::elementSet);

        uiPhotoScroll.valueProperty().addListener(this::updateDisplayedPhoto);
        photos.addListener(this::updatePhotos);
    }

    private void elementSet(ObservableValue<? extends Element> observable, Element oldValue, Element newValue) {
        if (oldValue instanceof AvecCommentaire) {
            uiCommentArea.textProperty().unbind();
        }

        if (newValue instanceof AvecCommentaire) {
            final StringProperty comProperty = ((AvecCommentaire)newValue).commentaireProperty();
            final StringBinding binding = Bindings.createStringBinding(() -> {
                final String value = comProperty.get();
                if (value == null || value.trim().isEmpty())
                    return "Aucun commentaire";
                return value;
            }, comProperty);
            uiCommentArea.textProperty().bind(binding);
        } else {
            uiCommentArea.setText("Aucun commentaire");
        }

        final ArrayList<AbstractPhoto> tmpPhotos = new ArrayList<>();
        if (newValue instanceof AvecPhotos) {
            final AvecPhotos photoContainer = (AvecPhotos) newValue;
            uiPhotoScroll.setVisible(true);
            if (photoContainer.getPhotos() != null && !photoContainer.getPhotos().isEmpty()) {
                tmpPhotos.addAll(photoContainer.getPhotos());
            }
        } else if (newValue instanceof AbstractPhoto) {
            tmpPhotos.add((AbstractPhoto) newValue);
        } else if (newValue instanceof Desordre) {
            final Desordre tmpDisorder = (Desordre) newValue;
            Observation target = null;
            if (tmpDisorder.observations != null && !tmpDisorder.observations.isEmpty()) {
                target = tmpDisorder.observations.get(0);
                Observation current;
                LocalDate date;
                for (int i = 1 ; i < tmpDisorder.observations.size() ; i++) {
                    current = tmpDisorder.observations.get(i);
                    date = current.dateProperty().get();
                    if (date != null && (target.dateProperty().get() == null || date.isAfter(target.dateProperty().get()))) {
                        target = current;
                    }
                }
            }

            if (target != null && target.getPhotos() != null && !target.getPhotos().isEmpty()) {
                tmpPhotos.addAll(target.getPhotos());
            }
        }

        if (!tmpPhotos.isEmpty()) {
            tmpPhotos.sort((o1, o2) -> {
                if (o1 == null || o1.getDate() == null)
                    return 1;
                else if (o2 == null || o2.getDate() == null)
                    return -1;
                else return o1.getDate().compareTo(o2.getDate());
            });
        }

        photos.set(tmpPhotos);
    }

    public ObjectProperty<Element> valueProperty() {
        return valueProperty;
    }

    private void updateDisplayedPhoto(final ObservableValue<? extends Number> obs, Number oldIndex, Number newIndex) {
        uiPhotoLibelle.textProperty().unbind();
        uiPhotoLibelle.setText("Pas de photo associée");
        uiPhotoDate.textProperty().unbind();
        uiPhotoDate.setText("");

        uiPhotoView.setImage(null);

        if (newIndex == null)
            return;

        final int index = newIndex.shortValue();
        if (index < 0)
            return;

        final List<? extends AbstractPhoto> tmpPhotos = photos.get();
        if (tmpPhotos == null || tmpPhotos.size() <= index) {
            return;
        }

        final AbstractPhoto selected = tmpPhotos.get(index);
        if (selected.getChemin() == null || selected.getChemin().isEmpty()) {
            uiPhotoLibelle.setText("Aucun fichier n'est associé à la photo.");
        } else {
            uiPhotoLibelle.setText("");
            // Do not bind directly date as string because it can return ugly "null" text.
            try {
                    // Try to find a dateProperty() method in AbstractPhoto subclasses, otherwise use directly
                // the getDate() value, but will not be bind.
                final Method methodDateProperty = selected.getClass().getMethod("dateProperty");
                final Object returnObj = methodDateProperty.invoke(selected);
                if (returnObj instanceof ObjectProperty) {
                    final ObjectProperty<LocalDate> dateProperty = (ObjectProperty<LocalDate>) returnObj;
                    uiPhotoDate.textProperty().bind(
                            Bindings.createStringBinding(() -> {
                                String result = dateProperty.asString().get();
                                if (result == null || result.equals("null"))
                                    result = "";
                                return result;
                            }, dateProperty));
                } else {
                    uiPhotoDate.setText(selected.getDate() == null || selected.getDate().toString().equals("null")
                            ? "" : selected.getDate().toString());
                }
            } catch (ReflectiveOperationException e) {
                uiPhotoDate.setText(selected.getDate() == null || selected.getDate().toString().equals("null")
                        ? "" : selected.getDate().toString());
            }

            final Task<Image> loader = new TaskManager.MockTask<>("Lecture d'image",
                    () -> {
                        try {
                            return SIRS.getOrLoadImage(SIRS.getDocumentAbsolutePath(selected).toUri().toURL().toExternalForm());
                        } catch (IllegalStateException e) {
                            // Illegal state exception here means no root folder has been configured for photos,
                            // just use the given path for the photo.
                            SIRS.LOGGER.log(Level.INFO, e.getLocalizedMessage());
                            return SIRS.getOrLoadImage(Paths.get(selected.getChemin()).toUri().toURL().toExternalForm());
                        }
                    });

            loader.setOnFailed(event -> Platform.runLater(() -> uiPhotoLibelle.setText("Le chemin de l'image est invalide : " + selected.getLibelle())));
            loader.setOnCancelled(event -> Platform.runLater(() -> uiPhotoLibelle.setText("Le chargement de l'image a été annulé")));
            loader.setOnSucceeded(event -> Platform.runLater(() -> {
                uiPhotoLibelle.setText(selected.getLibelle());
                // TODO : How to manage image loading error ? No exception is thrown here...
                uiPhotoView.setImage(loader.getValue());
                /* We want the image to be resized to fit it's stage bounding box, while
                 * keeping its proportions as the original image.
                 * /!\ We are forced to repeat this operation each time we change
                 * image, because the ImageView internally reset all its size
                 * properties to fit new image dimension.
                 */
                uiPhotoView.minWidth(0);
                uiPhotoView.minHeight(0);
            }));

            uiImageProgress.visibleProperty().bind(loader.runningProperty());
            TaskManager.INSTANCE.submit(loader);
        }
    }

    private void updatePhotos(final ObservableValue<? extends List<AbstractPhoto>> obs, final List<AbstractPhoto> oldList, List<AbstractPhoto> newList) {
        final boolean manualUpdate = uiPhotoScroll.getValue() == 0;
        if (newList == null || newList.isEmpty()) {
            uiPhotoScroll.setVisible(false);
            uiPhotoScroll.setMax(0);
        } else {
            uiPhotoScroll.setVisible(true);
            uiPhotoScroll.setMax(newList.size() - 1);
        }

        if (manualUpdate) {
            updateDisplayedPhoto(uiPhotoScroll.valueProperty(), 0, 0);
        } else {
            uiPhotoScroll.setValue(0);
        }
    }
}
