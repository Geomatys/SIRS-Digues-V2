
package fr.sirs.plugin.vegetation.map;

import fr.sirs.CorePlugin;
import fr.sirs.Injector;
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
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.VBox;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;

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
                GeotkFX.ICON_ADD);
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


    private final FXPositionableForm form = new FXPositionableForm();
    private final MouseListen mouseInputListener = new MouseListen();

    private final BorderPane wizard = new BorderPane();

    private ParcelleVegetation parcell = new ParcelleVegetation();
    private TronconDigue tronconDigue = null;
    private final Label lblTroncon = new Label();
    private final Label lblFirstPoint = new Label();
    private final Label lblLastPoint = new Label();
    private final Button end = new Button("Enregistrer");

    private MapLayer tronconLayer = null;
    private MapLayer borneLayer = null;

    public CreateParcelleTool(FXMap map) {
        super(SPI);

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


        final VBox vbox = new VBox(10,
                new Label("Troncon :"),
                lblTroncon,
                new Label("Borne de début :"),
                lblFirstPoint,
                new Label("Borne de fin :"),
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
                tronconLayer = layer;
            }
            if(layer.getName().equalsIgnoreCase(CorePlugin.BORNE_LAYER_NAME)){
                borneLayer = layer;
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
