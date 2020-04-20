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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Objet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.opengis.filter.identity.Identifier;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @param <T>
 */
public class ObjetEditHandler <T extends Objet> extends AbstractNavigationHandler {

    static final int CROSS_SIZE = 5;

    private final EditionMouseListen mouseInputListener = new EditionMouseListen();
    final FXGeometryLayer geomlayer = new FXGeometryLayer() {
        @Override
        protected Node createVerticeNode(Coordinate c, boolean selected) {
            final Line h = new Line(c.x - CROSS_SIZE, c.y, c.x + CROSS_SIZE, c.y);
            final Line v = new Line(c.x, c.y - CROSS_SIZE, c.x, c.y + CROSS_SIZE);
            h.setStroke(Color.RED);
            v.setStroke(Color.RED);
            return new Group(h, v);
        }
    };

    final Session session;

    //edition variables
    FeatureMapLayer tronconLayer = null;
    EditionHelper helperTroncon;

    final Class<T> objetClass;
    private T objet = null;
    FeatureMapLayer objetLayer = null;
    private EditionHelper helperObjet;

    final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();


    //Panneaux d'édition
    final Stage dialog = new Stage();
    FXObjetEditPane editPane;

    // overriden variable by init();
    protected String TRONCON_LAYER_NAME = CorePlugin.TRONCON_LAYER_NAME;

    /**
     * List of layers deactivated on tool install. They will be activated back
     * at uninstallation.
     */
    List<MapLayer> toActivateBack;

    /**
     * Same as {@link #ObjetEditHandler(org.geotoolkit.gui.javafx.render2d.FXMap, java.lang.String, java.lang.Class, fr.sirs.map.FXObjetEditPane)}
     * but with a default Pane of edition {@link FXObjetEditPane} and default type
     * name (tronçon).
     *
     * @param map
     * @param clazz
     */
    public ObjetEditHandler(final FXMap map, final Class<T> clazz) {
        this(map,clazz, new FXObjetEditPane(map, "troncon", clazz, true, true));
    }

    /**
     * {@link AbstractNavigationHandler} permettant l'édition d'{@link Objet} du
     * SIRS depuis une carte de l'application.
     *
     * @param map : carte à partir de laquelle on permet l'édition.
     * @param clazz : classe éditée.
     * @param editPane : panneau d'édition associé.
     */
    public ObjetEditHandler(final FXMap map, final Class<T> clazz, final FXObjetEditPane editPane) {
        ArgumentChecks.ensureNonNull("Panneau d'édition", editPane);
        objetClass =clazz;
        session = Injector.getSession();
        dialog.getIcons().add(SIRS.ICON);
        this.editPane = editPane;
//        this.editPane.setTypeNameLabel(typeName);

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

//        editPane.objetProperties().addListener(new ListChangeListener<SystemeReperageBorne>() {
//            @Override
//            public void onChanged(ListChangeListener.Change<? extends SystemeReperageBorne> c) {
//                if (borneLayer == null) {
//                    return;
//                }
//
//                final ObservableList<SystemeReperageBorne> lst = editPane.objetProperties();
//                final Set<Identifier> ids = new HashSet<>();
//                for (SystemeReperageBorne borne : lst) {
//                    ids.add(new DefaultFeatureId(borne.getBorneId()));
//                }
//
//                final Id filter = GO2Utilities.FILTER_FACTORY.id(ids);
//                borneLayer.setSelectionFilter(filter);
//
//                if (ids.size() == 1) {
//                    //borne edition mode
//                    final String borneId = lst.get(0).getBorneId();
//                    borne = session.getRepositoryForClass(BorneDigue.class).get(borneId);
//                    updateGeometry();
//                } else {
//                    borne = null;
//                    updateGeometry();
//                }
//
//            }
//        });

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

        editPane.tronconProperty().addListener(new ChangeListener<TronconDigue>() {
            @Override
            public void changed(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
                if (newValue != null) {
                    dialog.show();
                } else {
                    dialog.hide();
                }
            }
        });

        dialog.show();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void install(final FXMap component) {
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.setCursor(Cursor.CROSSHAIR);
        map.addDecoration(0, geomlayer);

        //recuperation du layer de troncon
        tronconLayer = null;

        //on passe en mode sélection de troncon
        editPane.reset();

        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        toActivateBack = new ArrayList<>();
        LabelMapper mapper = LabelMapper.get(objetClass);
        for (MapLayer layer : context.layers()) {
            if (layer.getName().equalsIgnoreCase(TRONCON_LAYER_NAME)) {
                tronconLayer = (FeatureMapLayer) layer;
                layer.setSelectable(true);
            } else if (layer.getName().equalsIgnoreCase(mapper.mapClassName())) {
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
            component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
            component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
            component.removeDecoration(geomlayer);
            component.setBottom(null);

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

    private void updateGeometry() {
        if (objet == null) {
            editGeometry.reset();
        } else {
            editGeometry.geometry.set(objet.getGeometry());
        }

        if (editGeometry.geometry == null) {
            geomlayer.getGeometries().clear();
        } else {
            geomlayer.getGeometries().setAll(editGeometry.geometry.get());
        }
    }

    class EditionMouseListen extends FXPanMouseListen {

        final ContextMenu popup = new ContextMenu();
        double startX;
        double startY;

        double initialX;
        double initialY;

        public EditionMouseListen() {
            super(ObjetEditHandler.this);
            popup.setAutoHide(true);
        }

        public EditionMouseListen(final ObjetEditHandler editHandler) {
            super(editHandler);
            popup.setAutoHide(true);
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (tronconLayer == null) {
                return;
            }

            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();

            final FXSystemeReperagePane.ObjetEditMode mode = editPane.getMode();

            if (FXSystemeReperagePane.ObjetEditMode.PICK_TRONCON.equals(mode)) {
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
            } else if (FXSystemeReperagePane.ObjetEditMode.EDIT_OBJET.equals(mode)) {
                throw new UnsupportedOperationException("unsupported EDIT_OBJET on mouseclicked");
//                final SystemeReperage sr = editPane.systemeReperageProperty().get();
//
//                if (objet == null || editGeometry.selectedNode[0] < 0) {
//                    //selection d'une borne
//                    final Feature feature = helperObjet.grabFeature(e.getX(), e.getY(), false);
//                    if (feature != null) {
//                        final Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
//                        if (bean instanceof BorneDigue && session.editionAuthorized((BorneDigue) bean)) {
//                            final BorneDigue candidate = (BorneDigue) bean;
//                            final String candidateId = candidate.getDocumentId();
//
//                            //on vérifie que la borne fait bien partie du SR sélectionné
//                            final List<SystemeReperageBorne> srbs = sr.getSystemeReperageBornes();
//                            for (SystemeReperageBorne srb : srbs) {
//                                if (srb.getBorneId().equals(candidateId)) {
//                                    editPane.selectSRB(srb);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }

            } else if (FXSystemeReperagePane.ObjetEditMode.CREATE_OBJET.equals(mode)) {

                final Coordinate coord = helperObjet.toCoord(startX, startY);
                final Point point = GO2Utilities.JTS_FACTORY.createPoint(coord);
                JTS.setCRS(point, session.getProjection());
                //les event vont induire le repaint de la carte
                editPane.createObjet(point);
            }

        }

        @Override
        public void mousePressed(final MouseEvent e) {
            super.mousePressed(e);

            initialX = getMouseX(e);
            initialY = getMouseY(e);
            startX = getMouseX(e);
            startY = getMouseY(e);

            if (editGeometry.geometry.get() != null) {
                helperObjet.grabGeometryNode(startX, startY, editGeometry);
            }
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            //do not use getX/getY to calculate difference
            //JavaFX Bug : https://javafx-jira.kenai.com/browse/RT-34608

            //calcul du deplacement
            startX = getMouseX(me);
            startY = getMouseY(me);

            if (objet != null && editGeometry.selectedNode[0] >= 0) {
                //deplacement d'une borne
                editGeometry.moveSelectedNode(helperObjet.toCoord(startX, startY));
                updateGeometry();
            } else {
                super.mouseDragged(me);
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            mouseDragged(me);

            if (objet != null && editGeometry.selectedNode[0] >= 0) {
                //On demande à l'utilisateur s'il souhaite sauvegarder en base de données les modifications apportées.
                final Alert alert = new Alert(Alert.AlertType.WARNING, "Confirmer le déplacement de l'objet?", ButtonType.OK, ButtonType.CANCEL);
                alert.setResizable(true);
                Optional<ButtonType> clickedButton = alert.showAndWait();

                if (clickedButton.get() == ButtonType.OK) {

                    objet.setGeometry((Point) editGeometry.geometry.get());
                    session.getRepositoryForClass(objetClass).update(objet);
//                    editPane.selectSRB(null);
                    //les event vont induire le repaint de la carte
                    final TronconDigue troncon = editPane.getTronconProperty();
                    if (troncon != null) {
                        //on recalcule les geometries des positionables du troncon.
                        TronconUtils.updatePositionableGeometry(troncon, session);
                    }
                } else {
                    startX = initialX;
                    startY = initialY;

                    // La position initiale de la borne est rétablie.
                    editGeometry.moveSelectedNode(helperObjet.toCoord(startX, startY));
                    updateGeometry();

                    // Probablement pas nécessaire de sauvegarder le non changement de la borne
                    // mais permet d'actualiser la carte
                    //-> TODO : appliquer un refresh de la map uniquement.
                    objet.setGeometry((Point) editGeometry.geometry.get());
                    session.getRepositoryForClass(objetClass).update(objet);
                }
            } else {
                super.mouseReleased(me);
            }
        }
    }

}
