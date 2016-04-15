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
package fr.sirs.plugin.vegetation.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import fr.sirs.Injector;
import static fr.sirs.SIRS.CSS_PATH;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.TraitementZoneVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import fr.sirs.theme.ui.FXPositionableExplicitMode;
import fr.sirs.util.SirsStringConverter;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.VBox;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.internal.Loggers;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @param <T>
 */
public abstract class CreateVegetationPolygonTool<T extends ZoneVegetation> extends AbstractEditionTool{


    //session and repo
    private final Session session;
    private final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo;
    private final SirsStringConverter cvt = new SirsStringConverter();

    private final MouseListen mouseInputListener = new MouseListen();
    private final BorderPane wizard = new BorderPane();
    private final Class<T> vegetationClass;

    private T vegetation = null;
    protected ParcelleVegetation parcelle = null;
    private final Label lblParcelle = new Label();
    private final Label lblGeom = new Label();

    private final Button end = new Button("Enregistrer");
    private final Button cancel = new Button("Annuler");

    private FeatureMapLayer parcelleLayer = null;

    //geometry en cours
    private EditionHelper helper;
    private final FXGeometryLayer geomLayer = new FXGeometryLayer();
    private Polygon geometry = null;
    private final List<Coordinate> coords = new ArrayList<>();
    private boolean justCreated = false;
    private final BooleanProperty ended = new SimpleBooleanProperty(false);

    /** List of layers deactivated on tool install. They will be activated back at uninstallation. */
    private List<MapLayer> toActivateBack;

    public CreateVegetationPolygonTool(FXMap map, Spi spi, Class<T> clazz) {
        super(spi);
        vegetationClass = clazz;
        wizard.getStylesheets().add(CSS_PATH);

        end.disableProperty().bind(ended.not());
        end.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //on sauvegarde
                vegetation.setGeometryMode(FXPositionableExplicitMode.MODE);
                vegetation.setValid(true);
                vegetation.setForeignParentId(parcelle.getDocumentId());
                final AbstractSIRSRepository vegetationRepo = session.getRepositoryForClass(vegetationClass);
                vegetationRepo.add(vegetation);
                startGeometry();
            }
        });
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reset();
                map.setHandler(new FXPanHandler(true));
            }
        });
        end.getStyleClass().add("btn-single");
        cancel.getStyleClass().add("btn-single");


        session = Injector.getSession();
        parcelleRepo = session.getRepositoryForClass(ParcelleVegetation.class);

        final Label lbl1 = new Label("Parcelle :");
        final Label lbl2 = new Label("Géométrie :");
        lbl1.getStyleClass().add("label-header");
        lbl2.getStyleClass().add("label-header");
        wizard.getStyleClass().add("blue-light");
        lblParcelle.getStyleClass().add("label-text");
        lblParcelle.setWrapText(true);
        lblGeom.getStyleClass().add("label-text");
        lblGeom.setWrapText(true);

        final VBox vbox = new VBox(15,
                lbl1,
                lblParcelle,
                lbl2,
                lblGeom,
                new HBox(30, end,cancel));
        vbox.setMaxSize(USE_PREF_SIZE,USE_PREF_SIZE);
        wizard.setCenter(vbox);
    }

    protected T newVegetation(){
        final T candidate = Injector.getSession().getElementCreator().createElement(vegetationClass);
        candidate.setTraitement(Injector.getSession().getElementCreator().createElement(TraitementZoneVegetation.class));
        return candidate;
    }

    private void reset(){
        vegetation = newVegetation();
        parcelle = null;
        lblParcelle.setText("Sélectionner une parcelle sur la carte");
        lblGeom.setText("");
        if(parcelleLayer!=null) parcelleLayer.setSelectionFilter(null);

        geometry = null;
        coords.clear();
        justCreated = false;
        ended.set(false);
        geomLayer.getGeometries().clear();
    }

    private void startGeometry(){

        geometry = null;
        coords.clear();
        justCreated = false;
        ended.set(false);
        geomLayer.getGeometries().clear();

        vegetation = newVegetation();
        lblParcelle.setText(cvt.toString(parcelle));
        lblGeom.setText("Cliquer sur la carte pour créer la géométrie, faire un double click pour terminer la géométrie");
        parcelleLayer.setSelectionFilter(GO2Utilities.FILTER_FACTORY.id(
                Collections.singleton(new DefaultFeatureId(parcelle.getDocumentId()))));

        final Geometry constraint = parcelle.getGeometry().buffer(1000000,10,BufferParameters.CAP_FLAT);
        try {
            JTS.setCRS(constraint, JTS.findCoordinateReferenceSystem(parcelle.getGeometry()));
        } catch (FactoryException ex) {
            Loggers.JAVAFX.log(Level.WARNING, ex.getMessage(), ex);
        }

        helper.setConstraint(constraint);
    }

    @Override
    public Node getConfigurationPane() {
        return wizard;
    }

    @Override
    public Node getHelpPane() {
        return null;
    }

    @Override
    public void install(FXMap component) {
        reset();
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);

        // On instancie une nouvelle liste pour les couches à désactiver provisoirement (le temps de l'activation de l'outil)
        toActivateBack = new ArrayList<>();
        
        //on rend les couches troncon et borne selectionnables
        final MapContext context = component.getContainer().getContext();
        for(MapLayer layer : context.layers()){
            if(layer.getName().equalsIgnoreCase(PluginVegetation.PARCELLE_LAYER_NAME)){
                parcelleLayer = (FeatureMapLayer) layer;
            } 
            else if (layer.isSelectable()) {
                toActivateBack.add(layer);
                layer.setSelectable(false);
            }
        }

        helper = new EditionHelper(map, parcelleLayer);
        component.setCursor(Cursor.CROSSHAIR);
        component.addDecoration(geomLayer);
    }

    @Override
    public boolean uninstall(FXMap component) {
        super.uninstall(component);
        if (toActivateBack != null) {
            for (final MapLayer layer : toActivateBack) {
                layer.setSelectable(true);
            }
        }
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
        component.setCursor(Cursor.DEFAULT);
        component.removeDecoration(geomLayer);
        reset();
        return true;
    }

    private class MouseListen extends FXPanMouseListen {

        public MouseListen() {
            super(CreateVegetationPolygonTool.this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(ended.get()) return;

            mousebutton = e.getButton();
            if(mousebutton!=MouseButton.PRIMARY){
                super.mouseClicked(e);
                return;
            }

            if(parcelle==null){
                final Rectangle2D clickArea = new Rectangle2D.Double(e.getX()-2, e.getY()-2, 4, 4);

                parcelleLayer.setSelectable(true);
                //recherche une parcelle sous la souris
                map.getCanvas().getGraphicsIn(clickArea, new AbstractGraphicVisitor() {
                    @Override
                    public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D area) {
                        final Object bean = graphic.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof ParcelleVegetation){
                            //on recupere l'object complet
                            parcelle = (ParcelleVegetation) bean;
                            //on recupere l'object complet
                            parcelle = parcelleRepo.get(parcelle.getDocumentId());
                            startGeometry();
                        }
                    }
                    @Override
                    public boolean isStopRequested() {
                        return parcelle!=null;
                    }
                    @Override
                    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}
                }, VisitFilter.INTERSECTS);
            }else if(parcelle!=null){

                if(e.getClickCount()>1){
                    //on sauvegarde
                    if(geometry.isEmpty() || !geometry.isValid()){
                        //il faut un polygon valid
                        return;
                    }

                    vegetation.setExplicitGeometry(geometry);
                    vegetation.setGeometry(geometry);
                    ended.set(true);
                }else{

                    final double x = getMouseX(e);
                    final double y = getMouseY(e);
                    mousebutton = e.getButton();

                    if(mousebutton == MouseButton.PRIMARY){

                        if(justCreated){
                            justCreated = false;
                            //we must modify the second point since two point where added at the start
                            coords.remove(2);
                            coords.remove(1);
                            coords.add(helper.toCoord(x,y));
                            coords.add(helper.toCoord(x,y));

                        }else if(coords.isEmpty()){
                            justCreated = true;
                            //this is the first point of the geometry we create
                            //add 3 points that will be used when moving the mouse around
                            coords.add(helper.toCoord(x,y));
                            coords.add(helper.toCoord(x,y));
                            coords.add(helper.toCoord(x,y));
                        }else{
                            justCreated = false;
                            coords.add(helper.toCoord(x,y));
                        }

                        geometry = EditionHelper.createPolygon(coords);
                        JTS.setCRS(geometry, map.getCanvas().getObjectiveCRS2D());
                        geomLayer.getGeometries().setAll(geometry);
                    }
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if(ended.get()) return;
            final MouseButton button = e.getButton();
            if(button!=MouseButton.PRIMARY) super.mouseMoved(e);

            if(coords.size() > 2){
                final double x = getMouseX(e);
                final double y = getMouseY(e);
                if(justCreated){
                    coords.remove(coords.size()-1);
                    coords.remove(coords.size()-1);
                    coords.add(helper.toCoord(x,y));
                    coords.add(helper.toCoord(x,y));
                }else{
                    coords.remove(coords.size()-1);
                    coords.add(helper.toCoord(x,y));
                }
                geometry = EditionHelper.createPolygon(coords);
                JTS.setCRS(geometry, map.getCanvas().getObjectiveCRS2D());
                geomLayer.getGeometries().setAll(geometry);
                return;
            }
            super.mouseMoved(e);
        }

    }

}
