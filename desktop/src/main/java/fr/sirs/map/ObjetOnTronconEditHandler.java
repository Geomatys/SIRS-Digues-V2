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
package fr.sirs.map;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Objet;
import static fr.sirs.map.EditModeObjet.CREATE_OBJET;
import java.util.ArrayList;
import java.util.Collections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.opengis.filter.identity.Identifier;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @param <T>
 */
public class ObjetOnTronconEditHandler<T extends Objet> extends AbstractOnTronconEditHandler<T> {


    /**
     * Same as
     * {@link #ObjetEditHandler(org.geotoolkit.gui.javafx.render2d.FXMap, java.lang.String, java.lang.Class, fr.sirs.map.FXObjetEditPane)}
     * but with a default Pane of edition {@link FXAbstractEditOnTronconPane}
     * and default type name (tronçon).
     *
     * @param map
     * @param clazz
     */
    public ObjetOnTronconEditHandler(final FXMap map, final Class<T> clazz) {
        this(map, clazz, new FXObjetEditPane(map, "troncon", clazz), true);
    }

    /**
     * {@link AbstractNavigationHandler} permettant l'édition d'{@link Objet} du
     * SIRS depuis une carte de l'application.
     *
     * @param map : carte à partir de laquelle on permet l'édition.
     * @param clazz : classe éditée.
     * @param editPane : panneau d'édition associé.
     * @param instantiateMouseEditListener : boolean indiquant si l'on souhaite
     * que ce constructeur instantie {@link #mouseInputListener} with a default
     * value.
     */
    public ObjetOnTronconEditHandler(final FXMap map, final Class<T> clazz, final FXAbstractEditOnTronconPane editPane, final boolean instantiateMouseEditListener) {
        super(map, clazz, editPane, instantiateMouseEditListener);
        ArgumentChecks.ensureNonNull("Panneau d'édition", editPane);

//        this.map = map;

//        session = Injector.getSession();
        dialog.getIcons().add(SIRS.ICON);
        this.editPane = editPane;

        // Prepare footer to set an "exit" button
        final Button exitButton = new Button("Fermer");
        exitButton.setCancelButton(true);
        exitButton.setOnAction(event -> dialog.hide());
        final Separator sep = new Separator();
        sep.setVisible(false);
        final HBox footer = new HBox(sep, exitButton);
        footer.setPadding(new Insets(0, 10, 10, 0));
        HBox.setHgrow(sep, Priority.ALWAYS);
        final BorderPane bp = new BorderPane(editPane, null, null, footer, null);

        dialog.setResizable(true);
        dialog.initModality(Modality.NONE);
        dialog.initOwner(map.getScene().getWindow());
        dialog.setScene(new Scene(bp));

        //on ecoute la selection du troncon et des bornes pour les mettre en surbrillant
        editPane.tronconProperty().addListener(new ChangeListener<TronconDigue>() {
            @Override
            public void changed(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
                if (tronconLayer == null) {
                    return;
                }

//                borne = null;
                updateGeometry();
                if (objetLayer != null) {
                    objetLayer.setSelectionFilter(null);
                }

                if (newValue == null) {
                    tronconLayer.setSelectionFilter(null);
                } else {
                    final Identifier id = new DefaultFeatureId(newValue.getDocumentId());
                    tronconLayer.setSelectionFilter(GO2Utilities.FILTER_FACTORY.id(Collections.singleton(id)));
                }
            }
        });

        //fin de l'edition
        dialog.setOnHiding((WindowEvent event) -> {
            TronconDigue troncon = editPane.getTronconProperty();
            if (troncon != null) {
                //on recupère la derniere version, la maj des sr entraine la maj des troncons
                troncon = session.getRepositoryForClass(TronconDigue.class).get(troncon.getDocumentId());
                //on recalcule les geometries des positionables du troncon.
                TronconUtils.updatePositionableGeometry(troncon, session);
            }
            editPane.save();
            editPane.reset();
        });

        editPane.tronconProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dialog.show();
            } else {
                dialog.hide();
            }
        });

        editPane.getModeProperty().bindBidirectional(modeProperty);
//                .addListener((observable, oldValue, newValue) -> {
//            modeProperty.setValue(newValue);
//        });

        dialog.show();

        if (instantiateMouseEditListener) {
            mouseInputListener = new EditionOnTronconMouseListen(this);
        }
    }

    String getObjetLayerName() {
        final LabelMapper mapper = LabelMapper.get(objetClass);
        return mapper.mapClassName();
    }

    @Override
    protected FXPanMouseListen getMouseInputListener() {
        return mouseInputListener;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void install(final FXMap component) {
        super.install(component);

        //recuperation du layer de troncon
        tronconLayer = null;

        //on passe en mode sélection de troncon
        editPane.reset();

        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        toActivateBack = new ArrayList<>();
        for (MapLayer layer : context.layers()) {
            if (layer.getName().equalsIgnoreCase(TRONCON_LAYER_NAME)) {
                tronconLayer = (FeatureMapLayer) layer;
                layer.setSelectable(true);
            } else if (layer.getName().equalsIgnoreCase(getObjetLayerName())) {
                objetLayer = (FeatureMapLayer) layer;
                layer.setSelectable(true);
            } else if (layer.isSelectable()) {
                toActivateBack.add(layer);
                layer.setSelectable(false);
            }
        }

        helperTroncon = new EditionHelper(map, tronconLayer);
        helperTroncon.setMousePointerSize(6);

        helperObjet = new EditionHelper(map, objetLayer);
        helperObjet.setMousePointerSize(6);

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean uninstall(final FXMap component) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la fin du mode édition.",
                ButtonType.YES, ButtonType.NO);
        alert.setResizable(true);
        if (editPane.tronconProperty().get() == null
                || ButtonType.YES.equals(alert.showAndWait().get())) {

            super.uninstall(component);
            if (toActivateBack != null) {
                for (final MapLayer layer : toActivateBack) {
                    layer.setSelectable(true);
                }
            }

            //déselection borne et troncon
            if (tronconLayer != null) {
                tronconLayer.setSelectionFilter(null);
            }
            if (objetLayer != null) {
                objetLayer.setSelectionFilter(null);
            }

            dialog.close();
            return true;
        }

        return false;
    }

//    private void updateGeometry() {
//        if (editedObjet == null) {
//            editGeometry.reset();
//        } else {
//            editGeometry.geometry.set(editedObjet.getGeometry());
//        }
//
//        if (editGeometry.geometry == null) {
//            geomLayer.getGeometries().clear();
//        } else {
//            geomLayer.getGeometries().setAll(editGeometry.geometry.get());
//        }
//    }
    class EditionOnTronconMouseListen extends SIRSEditMouseListen<T> {

//        final ContextMenu popup = new ContextMenu();
//        double startX;
//        double startY;

//        public EditionOnTronconMouseListen() {
//            super(AbstractOnTronconEditHandler.this);
////            popup.setAutoHide(true);
//        }
        public EditionOnTronconMouseListen(final ObjetOnTronconEditHandler editHandler) {
            super(editHandler);
//            popup.setAutoHide(true);
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (tronconLayer == null) {
                return;
            }

            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();

//            final EditModeObjet mode = editPane.getMode();
            if (EditModeObjet.PICK_TRONCON.equals(mode)) {
                if (mousebutton == MouseButton.PRIMARY) {
                    //selection d'un troncon
                    final Feature feature = helperTroncon.grabFeature(e.getX(), e.getY(), false);
                    if (feature != null) {
                        Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if (bean instanceof TronconDigue && session.editionAuthorized((TronconDigue) bean)) {
                            bean = session.getRepositoryForClass(TronconDigue.class).get(((TronconDigue) bean).getDocumentId());
                            editPane.tronconProperty().set((TronconDigue) bean);
                        }
                    }
                }
            } else {
                super.mouseClicked(e);
            }

        }

        @Override
        protected void chooseTypesAndCreate() {

            editPane = new FXObjetEditPane(map, TRONCON_LAYER_NAME, objetClass);
//            final Stage stage = new Stage();
//            stage.getIcons().add(SIRS.ICON);
//            stage.setTitle("Création d'objet");
//            stage.initModality(Modality.WINDOW_MODAL);
//            stage.setAlwaysOnTop(true);

            final Scene sceneChoices = new Scene(editPane);
            dialog.setScene(sceneChoices);
            dialog.showAndWait();

            objetHelper = getHelperObjet();
            final AbstractSIRSRepository<T> repo = Injector.getSession().getRepositoryForClass(editedClass);
            editedObjet = repo.create();
//                newCreatedObjet = true;
            modeProperty.setValue(CREATE_OBJET);

            switch (((FXObjetEditPane) editPane).getGeomType()) {
                case "Ponctuel":
                    newGeomType = Point.class;
                    break;
                case "Linéaire":
                    newGeomType = LineString.class;
                    break;
                case "Surfacique":
                    newGeomType = Polygon.class;
                    break;
                default:
                    newGeomType = Point.class;
            }

//                final Stage stage = new Stage();
//                stage.getIcons().add(SIRS.ICON);
//                stage.setTitle("Création d'objet");
//                stage.initModality(Modality.WINDOW_MODAL);
//                stage.setAlwaysOnTop(true);
//                final GridPane gridPane = new GridPane();
//                gridPane.setVgap(10);
//                gridPane.setHgap(5);
//                gridPane.setPadding(new Insets(10));
//                gridPane.add(new Label("Choisir un type de d'objet"), 0, 0);
//
//                final ComboBox<String> geomTypeBox = new ComboBox<>();
//                geomTypeBox.setItems(FXCollections.observableArrayList("Ponctuel", "Linéaire", "Surfacique"));
//                geomTypeBox.getSelectionModel().selectFirst();
//                final Label geomChoiceLbl = new Label("Choisir une forme géométrique");
//                geomChoiceLbl.visibleProperty().bind(geomTypeBox.visibleProperty());
//                gridPane.add(geomChoiceLbl, 0, 1);
//                gridPane.add(geomTypeBox, 1, 1);
//
//                final Button validateBtn = new Button("Valider");
//                validateBtn.setOnAction(event -> stage.close());
//                gridPane.add(validateBtn, 2, 3);
//
//                final Scene sceneChoices = new Scene(gridPane);
//                stage.setScene(sceneChoices);
//                stage.showAndWait();
//
////                final Class clazz = DesordreDependance.class;
////                objetHelper = new EditionHelper(map, objetLayer);
//                objetHelper = editHandler.getHelperObjet();
//
//                final AbstractSIRSRepository<G> repo = Injector.getSession().getRepositoryForClass(editedClass);
//                editedObjet = repo.create();
////                newCreatedObjet = true;
//                modeProperty.setValue(CREATE_OBJET);
//
//
//                switch (geomTypeBox.getSelectionModel().getSelectedItem()) {
//                    case "Ponctuel":
//                        newGeomType = Point.class;
//                        break;
//                    case "Linéaire":
//                        newGeomType = LineString.class;
//                        break;
//                    case "Surfacique":
//                        newGeomType = Polygon.class;
//                        break;
//                    default:
//                        newGeomType = Point.class;
//                }
        }

    }

}
