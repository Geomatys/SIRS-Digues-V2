
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ParamCoutTraitementVegetation;
import fr.sirs.core.model.ParamFrequenceTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.zoneVegetationClasses;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.geotoolkit.gui.javafx.util.FXListTableCell;


/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXPlanVegetationPane extends BorderPane {
    
    @FXML private TextField uiPlanName;
    @FXML private TextField uiDesignation;
    @FXML private Spinner uiPlanDebut;
    @FXML private Spinner uiPlanFin;
    @FXML private VBox uiVBox;
    
    private final PojoTable uiFrequenceTable;
    
    private final PojoTable uiCoutTable;
    @FXML private Button uiSave;

    private final Session session = Injector.getSession();
    private final AbstractSIRSRepository<PlanVegetation> planRepo = session.getRepositoryForClass(PlanVegetation.class);
    private final ParcelleVegetationRepository parcelleRepo = (ParcelleVegetationRepository) session.getRepositoryForClass(ParcelleVegetation.class);

    private final PlanVegetation plan;

    // Ces entiers mémorisent la date initiale du plan, puis les dates lors du précédent enregistrement :
    // si elles ont changé alors cele signifie qu'il faut mettre à jour les planifications des parcelles du plan.
    private int initialDebutPlan, initialFinPlan;

    public FXPlanVegetationPane(final PlanVegetation plan) {
        SIRS.loadFXML(this, FXPlanVegetationPane.class);
        this.plan = plan;
        
        uiDesignation.textProperty().bindBidirectional(plan.designationProperty());
        uiPlanName.textProperty().bindBidirectional(plan.libelleProperty());
        uiPlanDebut.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, LocalDate.now().getYear()));
        uiPlanDebut.setEditable(true);
        uiPlanDebut.getValueFactory().valueProperty().bindBidirectional(plan.anneeDebutProperty());
        uiPlanFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, LocalDate.now().getYear()+10));
        uiPlanFin.setEditable(true);
        uiPlanFin.getValueFactory().valueProperty().bindBidirectional(plan.anneeFinProperty());
        initialDebutPlan = plan.getAnneeDebut();
        initialFinPlan = plan.getAnneeFin();
        uiPlanFin.valueProperty().addListener((ChangeListener) new ChangeListener<Integer>() {

            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if(newValue.compareTo((Integer) uiPlanDebut.getValue())<=0) uiPlanDebut.decrement();
            }
        });

        uiPlanDebut.valueProperty().addListener((ChangeListener) new ChangeListener<Integer>() {

            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if(newValue.compareTo((Integer) uiPlanFin.getValue())>=0) uiPlanFin.increment();
            }
        });
        
        uiSave.setOnAction((ActionEvent event) -> {

            // On sauvegarde la plan.
            planRepo.update(FXPlanVegetationPane.this.plan);
            
            // Si les dates ont changé, il faut également mettre à jour les
            // planifications des parcelles !

            // On mémorise le décalade des années du début afin de décaler les
            // planifications avec le même décalage.
            final int beginShift = initialDebutPlan - this.plan.getAnneeDebut();

            /*
            On réinitialise la mémorisation des dates de début et de fin et on
            détermine également s'il est nécessaire de mettre à jour la liste de
            planifications des parcelles.
            */
            boolean updatePlanifs = false;
            if(this.plan.getAnneeDebut()!=initialDebutPlan) {
                initialDebutPlan = this.plan.getAnneeDebut();
                updatePlanifs = true;
            }
            if(this.plan.getAnneeFin()!=initialFinPlan) {
                initialFinPlan = this.plan.getAnneeFin();
                if(!updatePlanifs) updatePlanifs=true;
            }

            /*
            Si les dates de début et de fin du plan ont bougé, il faut mettre à
            jour les planifications des parcelles.
            */
            if(updatePlanifs){
                PluginVegetation.updatePlanifs(this.plan, beginShift);
            }

        });


        ////////////////////////////////////////////////////////////////////////
        // Construction des résumés des traitements planifiés sur les zones.
        ////////////////////////////////////////////////////////////////////////
        uiFrequenceTable = new ParamPojoTable(ParamFrequenceTraitementVegetation.class, "Paramétrage des traitements de zones");
        uiFrequenceTable.setTableItems(() -> (ObservableList) plan.paramFrequence);
        uiFrequenceTable.commentAndPhotoProperty().set(false);

        ////////////////////////////////////////////////////////////////////////
        // Construction des paramètes de coûts.
        ////////////////////////////////////////////////////////////////////////
        uiCoutTable = new ParamPojoTable(ParamCoutTraitementVegetation.class, "Paramétrage des coûts des traitements");
        uiCoutTable.setTableItems(() -> (ObservableList) plan.paramCout);
        uiCoutTable.commentAndPhotoProperty().set(false);
        
        // Mise en page
        uiVBox.getChildren().addAll(uiCoutTable, uiFrequenceTable);
    }

    private static class ParamPojoTable<T> extends PojoTable {

        private final List<Class<? extends ZoneVegetation>> vegetationClasses;
        private final SirsStringConverter converter = new SirsStringConverter();

        public ParamPojoTable(Class<T> pojoClass, String title) {
            super(pojoClass, title);

            if(pojoClass==ParamFrequenceTraitementVegetation.class){
                // On garde les classes de zones de végétation.
                vegetationClasses = zoneVegetationClasses();
                final TableColumn<T, Class> classColumn = new TableColumn<>("Type de zone");
                classColumn.setCellValueFactory( param -> {
                    return ((ParamFrequenceTraitementVegetation) param.getValue()).typeProperty();
                });
                classColumn.setCellFactory( param -> new FXListTableCell<>(vegetationClasses, converter));
                getTable().getColumns().add(2, (TableColumn) classColumn);
            }else{
                vegetationClasses=null;
            }
        }
    }
}
