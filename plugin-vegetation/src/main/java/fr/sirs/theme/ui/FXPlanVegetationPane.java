
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.ParamCoutTraitementVegetation;
import fr.sirs.core.model.ParamFrequenceTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.TraitementZoneVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.zoneVegetationClasses;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
        uiFrequenceTable = new ParamPojoTable("Paramétrage des traitements de zones");
        boolean costListModified=false, frequencyListModified=false;
        final List<ParamFrequenceTraitementVegetation> frequencesDuPlan = new ArrayList<>(plan.paramFrequence);
        final List<ParamCoutTraitementVegetation> coutsDuPlan = new ArrayList<>(plan.paramCout);
        final List<ParamFrequenceTraitementVegetation> candidateFrequencesToAdd = new ArrayList<>();
        final List<ParamCoutTraitementVegetation> candidateCoutsToAdd = new ArrayList<>();

        // Calcul de toutes les combinaisons de traitements existant dans le plan
        for(final ParcelleVegetation parcelle : parcelleRepo.getByPlan(plan)){
            if(parcelle.getId()!=null){
                final List<? extends ZoneVegetation> zones = AbstractZoneVegetationRepository.getAllZoneVegetationByParcelleId(parcelle.getId(), session);
                for(final ZoneVegetation zone : zones){

                    // On détermine la classe de la zone.
                    final Class type = zone.getClass();

                    // On détermine le type de végétation éventuel.
                    final String typeVegetation;
                    if(type==PeuplementVegetation.class){
                        typeVegetation = ((PeuplementVegetation) zone).getTypePeuplementId();
                    }
                    else if(type==InvasiveVegetation.class){
                        typeVegetation = ((InvasiveVegetation) zone).getTypeInvasive();
                    }
                    else typeVegetation = null;

                    // Il faut ensuite examiner les traitements ponctuel et non ponctuel de la zone
                    final TraitementZoneVegetation traitement = zone.getTraitement();
                    if(traitement!=null){
                        candidateFrequencesToAdd.add(toParamFrequence(type, typeVegetation, traitement.getTraitementPonctuelId(), traitement.getSousTraitementPonctuelId(), true));
                        candidateFrequencesToAdd.add(toParamFrequence(type, typeVegetation, traitement.getTraitementId(), traitement.getSousTraitementId(), false));
                        candidateCoutsToAdd.add(toParamCout(traitement.getTraitementId(), traitement.getSousTraitementId()));
                        candidateCoutsToAdd.add(toParamCout(traitement.getTraitementPonctuelId(), traitement.getSousTraitementPonctuelId()));
                    }
                }
            }
        }

        /*
        Toutes les combinaisons de paramètres de fréquences sont candidates à
        l'ajout dans le plan :
        Il faut examiner si le plan les contient déjà…
        */
        final Iterator<ParamFrequenceTraitementVegetation> freqIt = candidateFrequencesToAdd.iterator();
        while(freqIt.hasNext()){
            final ParamFrequenceTraitementVegetation candidate = freqIt.next();
            boolean found = false;
            for(final ParamFrequenceTraitementVegetation currentFrequence : frequencesDuPlan){
                // Si le plan contient un paramètre équivalent au candidat, on retire ce dernier de la liste à ajouter.
                if(equivParamFrequence(candidate, currentFrequence)){
                    freqIt.remove();
                    found = true;
                    break;
                }
            }

            if (!found) {
                frequencyListModified=true;
                frequencesDuPlan.add(candidate);
            }
        }

        /*
        Toutes les combinaisons de paramètres de cout sont candidates à
        l'ajout dans le plan :
        Il faut examiner si le plan les contient déjà…
        */
        final Iterator<ParamCoutTraitementVegetation> coutIt = candidateCoutsToAdd.iterator();
        while(coutIt.hasNext()){
            final ParamCoutTraitementVegetation candidate = coutIt.next();
            boolean found = false;
            for(final ParamCoutTraitementVegetation currentCout : coutsDuPlan){
                // Si le plan contient un paramètre équivalent au candidat, on retire ce dernier de la liste à ajouter.
                if(equivParamCout(candidate, currentCout)){
                    coutIt.remove();
                    found=true;
                    break;
                }
            }

            if (!found) {
                costListModified=true;
                coutsDuPlan.add(candidate);
            }
        }

        // Si nécessaire, on ajoute les paramètres trouvés.
        if(frequencyListModified) plan.setParamFrequence(frequencesDuPlan);
        if(costListModified) plan.setParamCout(coutsDuPlan);
        if(frequencyListModified || costListModified) planRepo.update(plan);

        uiFrequenceTable.setTableItems(() -> (ObservableList) plan.paramFrequence);
        uiFrequenceTable.commentAndPhotoProperty().set(false);

        ////////////////////////////////////////////////////////////////////////
        // Construction des paramètes de coûts.
        ////////////////////////////////////////////////////////////////////////
        uiCoutTable = new PojoTable(ParamCoutTraitementVegetation.class, "Paramétrage des coûts des traitements");
        uiCoutTable.setTableItems(() -> (ObservableList) plan.paramCout);
        uiCoutTable.commentAndPhotoProperty().set(false);
        
        // Mise en page
        uiVBox.getChildren().addAll(uiCoutTable, uiFrequenceTable);
    }

    private static ParamFrequenceTraitementVegetation toParamFrequence(final Class type, final String typeVegetationId, final String traitementId, final String sousTraitementId, final boolean ponctuel){
        final ParamFrequenceTraitementVegetation param = Injector.getSession().getElementCreator().createElement(ParamFrequenceTraitementVegetation.class);
        param.setType(type);
        param.setTypeVegetationId(typeVegetationId);
        param.setTraitementId(traitementId);
        param.setSousTraitementId(sousTraitementId);
        param.setPonctuel(ponctuel);
        return param;
    }

    private static boolean equivParamFrequence(final ParamFrequenceTraitementVegetation p1, final ParamFrequenceTraitementVegetation p2){
        if(!Objects.equals(p1.getType(), p2.getType())) return false;
        else if(!Objects.equals(p1.getTraitementId(), p2.getTraitementId())) return false;
        else if(!Objects.equals(p1.getSousTraitementId(), p2.getSousTraitementId())) return false;
        else if(!Objects.equals(p1.getTypeVegetationId(), p2.getTypeVegetationId())) return false;
        else if(p1.getPonctuel()!=p2.getPonctuel()) return false;
        else return true;
    }

    private static ParamCoutTraitementVegetation toParamCout(final String traitementId, final String sousTraitementId){
        final ParamCoutTraitementVegetation param = Injector.getSession().getElementCreator().createElement(ParamCoutTraitementVegetation.class);
        param.setTraitementId(traitementId);
        param.setSousTraitementId(sousTraitementId);
        return param;
    }

    private static boolean equivParamCout(final ParamCoutTraitementVegetation p1, final ParamCoutTraitementVegetation p2){
        if(!Objects.equals(p1.getTraitementId(), p2.getTraitementId())) return false;
        else if(!Objects.equals(p1.getSousTraitementId(), p2.getSousTraitementId())) return false;
        else return true;
    }

    private static class ParamPojoTable extends PojoTable {

        private final List<Class<? extends ZoneVegetation>> vegetationClasses;
        private final SirsStringConverter converter = new SirsStringConverter();

        public ParamPojoTable(String title) {
            super(ParamFrequenceTraitementVegetation.class, title);

                // On garde les classes de zones de végétation.
                vegetationClasses = zoneVegetationClasses();
                final TableColumn<ParamFrequenceTraitementVegetation, Class> classColumn = new TableColumn<>("Type de zone");
                classColumn.setCellValueFactory( param -> {
                    return ((ParamFrequenceTraitementVegetation) param.getValue()).typeProperty();
                });
                classColumn.setCellFactory( param -> new FXListTableCell<>(vegetationClasses, converter));
                getTable().getColumns().add(2, (TableColumn) classColumn);
        }
    }
}
