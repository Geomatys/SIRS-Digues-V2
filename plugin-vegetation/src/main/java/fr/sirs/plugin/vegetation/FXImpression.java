
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import fr.sirs.util.SirsStringConverter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javax.imageio.ImageIO;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.DefaultPortrayalService;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.odftoolkit.simple.TextDocument;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXImpression extends GridPane{

    @FXML private GridPane uiGrid;
    @FXML private ListView<Preview> uiTroncons;
    @FXML private ComboBox<Integer> uiDateStart;
    @FXML private ComboBox<Integer> uiDateEnd;
    @FXML private CheckBox uiAllTroncon;
    @FXML private Button uiPrint;
    @FXML private ProgressIndicator uiProgress;
    @FXML private Label uiProgressLabel;
    
    @FXML private CheckBox uiTraiteeNonPlanif;
    @FXML private CheckBox uiTraiteePlanif;
    @FXML private CheckBox uiNonTraiteeNonPlanif;
    @FXML private CheckBox uiNonTraiteePlanif;

    private final Spinner<Double> uiPRStart = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MIN_VALUE, Double.MAX_VALUE, 0));
    private final Spinner<Double> uiPREnd = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MIN_VALUE, Double.MAX_VALUE, 0));

    private final Session session = Injector.getSession();
    private final VegetationSession vegSession = VegetationSession.INSTANCE;
    private final ObjectProperty<PlanVegetation> planProperty = new SimpleObjectProperty<>();
    private final BooleanProperty running = new SimpleBooleanProperty(false);

    public FXImpression() {
        SIRS.loadFXML(this, Positionable.class);

        uiGrid.add(uiPRStart, 1, 3);
        uiGrid.add(uiPREnd, 3, 3);

        final StringConverter strCvt = new SirsStringConverter();
        uiTroncons.disableProperty().bind(uiAllTroncon.selectedProperty());
        uiTroncons.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiTroncons.setCellFactory((ListView<Preview> param) -> new TextFieldListCell<>(strCvt));
        uiPrint.disableProperty().bind(planProperty.isNull().or(running));
        uiProgress.visibleProperty().bind(running);
        uiProgressLabel.visibleProperty().bind(running);

        //on modifie les date de fin en fonction de la date de debut.
        uiDateStart.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                final ObservableList<Integer> items = FXCollections.observableArrayList();
                final Integer selectedVal = uiDateEnd.getValue();
                final PlanVegetation plan = planProperty.get();
                if(plan!=null){
                    for(int i=uiDateStart.getValue()+1;i<plan.getAnneeFin();i++){
                        items.add(i);
                    }
                }
                uiDateEnd.setItems(items);
                if(selectedVal!=null && items.contains(selectedVal)){
                    uiDateEnd.getSelectionModel().select(selectedVal);
                }else{
                    uiDateEnd.getSelectionModel().selectLast();
                }
            }
        });


        //on liste les troncons du plan actif.
        planProperty.addListener((ObservableValue<? extends PlanVegetation> observable, PlanVegetation oldValue, PlanVegetation newValue) -> update());
        planProperty.bind(vegSession.planProperty());

    }

    private void update(){
        final PlanVegetation plan = planProperty.get();
        if(plan!=null){
            //dates possible
            int debut = plan.getAnneeDebut();
            final ObservableList<Integer> items = FXCollections.observableArrayList();
            for(int i=debut;i<plan.getAnneeFin();i++){
                items.add(i);
            }
            uiDateStart.setItems(items);
            uiDateStart.getSelectionModel().selectFirst();

            //troncons possible
            final Previews previews = session.getPreviews();
            final List<ParcelleVegetation> parcelles = vegSession.getParcelleRepo().getByPlan(plan);
            final Map<String,Preview> troncons = new HashMap<>();
            for(ParcelleVegetation parcelle : parcelles){
                final String tronconId = parcelle.getLinearId();
                if(!troncons.containsKey(tronconId)){
                    final Preview preview = previews.get(tronconId);
                    troncons.put(tronconId, preview);
                }
            }
            final ObservableList<Preview> lst = FXCollections.observableArrayList(troncons.values());
            uiTroncons.setItems(lst);
        }else{
            uiDateStart.setItems(FXCollections.emptyObservableList());
            uiDateStart.getSelectionModel().selectFirst();
            uiTroncons.setItems(FXCollections.emptyObservableList());
        }
    }

    @FXML
    void print(ActionEvent event) {
        final PlanVegetation plan = planProperty.get();

        //liste de toutes les parcelles
        final List<ParcelleVegetation> parcelles = vegSession.getParcelleRepo().getByPlan(plan);


        final FileChooser chooser = new FileChooser();
        final File file = chooser.showSaveDialog(null);
        if(file==null) return;

        uiProgressLabel.setText("");
        running.set(true);

        new Thread(){
            @Override
            public void run() {
                try{
                    final TextDocument doc = TextDocument.newTextDocument();

                    //on recupere les dates
                    final List<Integer> years = new ArrayList<>();
                    for(int i=uiDateStart.getValue();i<=uiDateEnd.getValue();i++){
                        years.add(i);
                    }

                    //on recupere les parcelles a utiliser
                    Platform.runLater(()->uiProgressLabel.setText("Récupération des parcelles"));
                    final List<ParcelleVegetation> parcelles;
                    if(uiAllTroncon.isSelected()){
                        parcelles = vegSession.getParcelleRepo().getByPlan(plan);
                    }else{
                        parcelles = new ArrayList<>();
                        for(Preview p : uiTroncons.getSelectionModel().getSelectedItems()){
                            parcelles.addAll(vegSession.getParcelleRepo().getByLinearId(p.getElementId()));
                        }
                    }

                    //on enleve les parcelles qui n'intersect pas la zone des PR
                    final double prStart = uiPRStart.getValue();
                    final double prEnd = uiPREnd.getValue();
                    if(prStart!=0.0 && prEnd!=0.0){
                        for(int i=parcelles.size()-1;i>=0;i--){
                            final ParcelleVegetation parcelle = parcelles.get(i);
                            if(parcelle.getPrDebut()>prEnd || parcelle.getPrFin()<prStart){
                                parcelles.remove(i);
                            }
                        }
                    }


                    //generation des cartes et table pour chaque année
                    for(int year : years){
                        Platform.runLater(()->uiProgressLabel.setText("Génération de la carte "+year));
                        
                        //generation de la carte
                        final MapContext context = MapBuilder.createContext();
                        context.layers().add(VegetationSession.parcellePanifState(plan,year,parcelles));

                        final CanvasDef cdef = new CanvasDef(new Dimension(1280, 1024), Color.WHITE);
                        final SceneDef sdef = new SceneDef(context);
                        final ViewDef vdef = new ViewDef(context.getBounds());
                        final BufferedImage mapImage = DefaultPortrayalService.portray(cdef, sdef, vdef);

                        final File mapFile = File.createTempFile("map", ".png");
                        ImageIO.write(mapImage, "png", file);
                        ODTUtils.insertImage(doc, mapFile.toURI());

                        //on construit les listes
                        final List<ParcelleVegetation> planifieTraitee = new ArrayList<>();
                        final List<ParcelleVegetation> planifieNonTraitee = new ArrayList<>();
                        final List<ParcelleVegetation> NonPlanifieTraitee = new ArrayList<>();
                        final List<ParcelleVegetation> NonPlanifieNonTraitee = new ArrayList<>();
                        //TODO

                    }

                    Platform.runLater(()->uiProgressLabel.setText("Sauvegarde du fichier ODT"));
                    doc.save(file);


                    Platform.runLater(()->uiProgressLabel.setText("Génération terminée"));
                    try {sleep(2000);} catch (InterruptedException ex) {}

                }catch(Exception ex){
                    SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    Platform.runLater(()->GeotkFX.newExceptionDialog("Une erreur est survenue lors de la génération du rapport.", ex).show());
                }finally{
                    Platform.runLater(()->{
                        uiProgressLabel.setText("");
                        running.set(false);
                    });
                }
            }
        }.start();
        

    }

}
