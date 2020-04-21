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
package fr.sirs.plugin.dependance.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.DesordreDependanceRepository;
import fr.sirs.core.model.AvecSettableGeometrie;
import fr.sirs.core.model.DesordreDependance;
import fr.sirs.map.AbstractSIRSEditHandler;
import fr.sirs.map.SIRSEditMouseListen;
import fr.sirs.plugin.dependance.PluginDependance;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;

/**
 * Contrôle les actions possibles pour le bouton d'édition et de modification de dépendances
 * sur la carte.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class DesordreEditHandler extends AbstractSIRSEditHandler {
//    private final MouseListen mouseInputListener = new MouseListen();
//    private final FXGeometryLayer geomLayer = new FXGeometryLayer();

    private SIRSEditMouseListen mouseInputListener;

    /**
     * Couches présentant les dépendances sur la carte.
     */
//    private FeatureMapLayer objetLayer;

    /**
     * La dépendance en cours.
     */
//    private DesordreDependance editedObjet;

    /**
     * Outil d'aide pour éditer une {@linkplain #editGeometry géométrie} existante.
     */
//    private EditionHelper objetHelper;

    /**
     * Géométrie en cours d'édition.
     */
//    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();

    /**
     * Coordonnées de la {@linkplain #editGeometry géométrie}.
     */
//    private final List<Coordinate> coords = new ArrayList<>();

    /**
     * Vrai si une dépendance vient d'être créée.
     */
//    private boolean newCreatedObjet = false;

    /**
     * Définit le type de géométries à dessiner, pour les dépendances de types "ouvrages de voirie" ou "autres"
     * pour lesquelles plusieurs choix sont possibles.
     */
//    private Class newGeomType = Point.class;

    /**
     * Vrai si la {@linkplain #coords liste des coordonnées} de la {@linkplain #editGeometry géométrie}
     * vient d'être créée.
     */
//    private boolean justCreated = false;

    public DesordreEditHandler() {
//        super(DesordreDependance.class,  new FXGeometryLayer());
        super(DesordreDependance.class);
//        mouseInputListener = new MouseListen();
//        mouseInputListener = new SIRSEditMouseListen(this);
    }

    public DesordreEditHandler(final DesordreDependance dependance) {
        this();
        this.editedObjet = dependance;

        final boolean newCreatedObjet;
        if (dependance.getGeometry() != null) {
            editGeometry.geometry.set((Geometry)dependance.getGeometry().clone());
            geomLayer.getGeometries().setAll(editGeometry.geometry.get());
            newCreatedObjet = false;
        } else {
            newCreatedObjet = true;
        }

        mouseInputListener = new SIRSEditMouseListen(this);
        mouseInputListener.setNewCreatedObjet(newCreatedObjet);
    }

    @Override
    public SIRSEditMouseListen getMouseInputListener() {
        return mouseInputListener;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void install(FXMap component) {
        super.install(component);
//        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
//        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);
//        map.setCursor(Cursor.CROSSHAIR);
//        map.addDecoration(0, geomLayer);

        objetLayer = PluginDependance.getDesordreLayer();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean uninstall(FXMap component) {
        if (editGeometry.geometry.get() == null) {
            super.uninstall(component);
            component.removeDecoration(geomLayer);
            component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
            component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
            return true;
        }

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la fin du mode édition ? Les modifications non sauvegardées seront perdues.",
                ButtonType.YES,ButtonType.NO);
        if (ButtonType.YES.equals(alert.showAndWait().get())) {
            super.uninstall(component);
            component.removeDecoration(geomLayer);
            component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
            component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
            return true;
        }

        return false;
    }

}
