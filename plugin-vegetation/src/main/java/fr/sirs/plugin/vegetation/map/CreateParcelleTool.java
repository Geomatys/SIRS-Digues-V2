
package fr.sirs.plugin.vegetation.map;

import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import static fr.sirs.SIRS.CSS_PATH;
import fr.sirs.Session;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.ResourceInternationalString;
import fr.sirs.util.SirsStringConverter;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.opengis.filter.identity.FeatureId;

/**
 *
 * @author Johann Sorel
 */
public class CreateParcelleTool extends AbstractEditionTool{

    public static final Spi SPI = new Spi();
    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("CreateParcelle",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle", 
                        "fr.sirs.plugin.vegetation.map.CreateParcelleTool.title",CreateParcelleTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle", 
                        "fr.sirs.plugin.vegetation.map.CreateParcelleTool.abstract",CreateParcelleTool.class.getClassLoader()),
                new Image("fr/sirs/plugin/vegetation/parcelle.png"));
        }

        @Override
        public boolean canHandle(Object candidate) {
            return true;
        }

        @Override
        public EditionTool create(FXMap map, Object layer) {
            return new CreateParcelleTool(map);
        }
    };


    //session and repo
    private final Session session;
    private final AbstractSIRSRepository<BorneDigue> borneRepo;
    private final AbstractSIRSRepository<TronconDigue> tronconRepo;

    private final MouseListen mouseInputListener = new MouseListen();
    private final BorderPane wizard = new BorderPane();

    private ParcelleVegetation parcell = new ParcelleVegetation();
    private TronconDigue tronconDigue = null;
    private final Label lblTroncon = new Label();
    private final Label lblFirstPoint = new Label();
    private final Label lblLastPoint = new Label();
    private final Button end = new Button("Enregistrer");

    private FeatureMapLayer tronconLayer = null;
    private FeatureMapLayer borneLayer = null;

    public CreateParcelleTool(FXMap map) {
        super(SPI);
        wizard.getStylesheets().add(CSS_PATH);

        session = Injector.getSession();
        borneRepo = session.getRepositoryForClass(BorneDigue.class);
        tronconRepo = session.getRepositoryForClass(TronconDigue.class);

        end.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                parcell.setValid(true);

                //calcule de la geometrie
                parcell.setGeometry(LinearReferencingUtilities.buildGeometry(
                        tronconDigue.getGeometry(), parcell, Injector.getSession().getRepositoryForClass(BorneDigue.class)));

                //recuperation des PR
                final String srId = tronconDigue.getSystemeRepDefautId();
                final AbstractSIRSRepository<SystemeReperage> srRepo = session.getRepositoryForClass(SystemeReperage.class);
                final SystemeReperage sr = srRepo.get(srId);

                for(SystemeReperageBorne srb : sr.getSystemeReperageBornes()){
                    if(srb.getBorneId().equals(parcell.getBorneDebutId())){
                        parcell.setPrDebut(srb.getValeurPR());
                    }else if(srb.getBorneId().equals(parcell.getBorneFinId())){
                        parcell.setPrFin(srb.getValeurPR());
                    }
                }

                final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo = session.getRepositoryForClass(ParcelleVegetation.class);
                parcelleRepo.add(parcell);
                map.setHandler(new FXPanHandler(true));
                reset();
            }
        });

        final Label lbl1 = new Label("Tronçon :");
        final Label lbl2 = new Label("Borne de début :");
        final Label lbl3 = new Label("Borne de fin :");
        lbl1.getStyleClass().add("label-header");
        lbl2.getStyleClass().add("label-header");
        lbl3.getStyleClass().add("label-header");
        lblTroncon.getStyleClass().add("label-text");
        lblFirstPoint.getStyleClass().add("label-text");
        lblLastPoint.getStyleClass().add("label-text");
        end.getStyleClass().add("btn-single");
        wizard.getStyleClass().add("blue-light");

        final VBox vbox = new VBox(15,
                lbl1,
                lblTroncon,
                lbl2,
                lblFirstPoint,
                lbl3,
                lblLastPoint,
                end);
        vbox.setMaxSize(USE_PREF_SIZE,USE_PREF_SIZE);
        wizard.setCenter(vbox);
    }

    private void reset(){
        parcell = new ParcelleVegetation();
        end.disableProperty().unbind();
        end.disableProperty().bind( parcell.linearIdProperty().isNull()
                                .or(parcell.borneDebutIdProperty().isNull()
                                .or(parcell.borneFinIdProperty().isNull())));
        lblTroncon.setText("Sélectionner un tronçon sur la carte");
        lblFirstPoint.setText("");
        lblLastPoint.setText("");

        if(tronconLayer!=null) tronconLayer.setSelectionFilter(null);
        if(borneLayer!=null) borneLayer.setSelectionFilter(null);
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

        //on rend les couches troncon et borne selectionnables
        final MapContext context = component.getContainer().getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase(CorePlugin.TRONCON_LAYER_NAME)){
                tronconLayer = (FeatureMapLayer) layer;
            }
            if(layer.getName().equalsIgnoreCase(CorePlugin.BORNE_LAYER_NAME)){
                borneLayer = (FeatureMapLayer) layer;
            }
        }
        component.setCursor(Cursor.CROSSHAIR);
    }

    @Override
    public boolean uninstall(FXMap component) {
        super.uninstall(component);
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.setCursor(Cursor.DEFAULT);
        reset();
        return true;
    }

    private class MouseListen extends FXPanMouseListen {

        private final SirsStringConverter cvt = new SirsStringConverter();

        public MouseListen() {
            super(CreateParcelleTool.this);
        }

        @Override
        public void mouseClicked(MouseEvent event) {

            final Rectangle2D clickArea = new Rectangle2D.Double(event.getX()-2, event.getY()-2, 4, 4);

            if(parcell.getLinearId()==null || parcell.getLinearId().isEmpty()){
                tronconLayer.setSelectable(true);
                borneLayer.setSelectable(false);
                //recherche un troncon sous la souris
                map.getCanvas().getGraphicsIn(clickArea, new AbstractGraphicVisitor() {
                    @Override
                    public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D area) {
                        final Object bean = graphic.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof TronconDigue){
                            tronconDigue = (TronconDigue) bean;
                            //on recupere l'object complet
                            tronconDigue = Injector.getSession().getRepositoryForClass(TronconDigue.class).get(tronconDigue.getDocumentId());
                            parcell.setLinearId(tronconDigue.getId());
                            lblTroncon.setText(cvt.toString(tronconDigue));
                            lblFirstPoint.setText("Sélectionner une borne sur la carte");

                            //troncon et bornes sur la carte
                            tronconLayer.setSelectionFilter(GO2Utilities.FILTER_FACTORY.id(Collections.singleton(graphic.getCandidate().getIdentifier())));
                            final Set<FeatureId> borneIds = new HashSet<>();
                            for(String str : tronconDigue.getBorneIds()) borneIds.add(new DefaultFeatureId(str));
                            borneLayer.setSelectionFilter(GO2Utilities.FILTER_FACTORY.id(borneIds));
                        }
                    }
                    @Override
                    public boolean isStopRequested() {
                        return parcell.getLinearId()!=null && !parcell.getLinearId().isEmpty();
                    }
                    @Override
                    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}
                }, VisitFilter.INTERSECTS);
            }else if(parcell.getBorneDebutId()==null || parcell.getBorneDebutId().isEmpty()){
                tronconLayer.setSelectable(false);
                borneLayer.setSelectable(true);
                //recherche une borne sous la souris
                map.getCanvas().getGraphicsIn(clickArea, new AbstractGraphicVisitor() {
                    @Override
                    public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D area) {
                        final Object bean = graphic.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof BorneDigue && tronconDigue.getBorneIds().contains(((BorneDigue)bean).getId())){
                            parcell.setBorneDebutId(((BorneDigue)bean).getId());
                            lblFirstPoint.setText(cvt.toString(bean));
                            lblLastPoint.setText("Sélectionner une borne sur la carte");
                        }
                    }
                    @Override
                    public boolean isStopRequested() {
                        return parcell.getBorneDebutId()!=null && !parcell.getBorneDebutId().isEmpty();
                    }
                    @Override
                    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}
                }, VisitFilter.INTERSECTS);
            }
            else if(parcell.getBorneFinId()==null || parcell.getBorneFinId().isEmpty()){
                tronconLayer.setSelectable(false);
                borneLayer.setSelectable(true);
                //recherche une borne sous la souris
                map.getCanvas().getGraphicsIn(clickArea, new AbstractGraphicVisitor() {
                    @Override
                    public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D area) {
                        final Object bean = graphic.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof BorneDigue && tronconDigue.getBorneIds().contains(((BorneDigue)bean).getId())){
                            parcell.setBorneFinId(((BorneDigue)bean).getId());
                            lblLastPoint.setText(cvt.toString(bean));
                        }
                    }
                    @Override
                    public boolean isStopRequested() {
                        return parcell.getBorneFinId()!=null && !parcell.getBorneFinId().isEmpty();
                    }
                    @Override
                    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}
                }, VisitFilter.INTERSECTS);
            }

        }

    }

}
