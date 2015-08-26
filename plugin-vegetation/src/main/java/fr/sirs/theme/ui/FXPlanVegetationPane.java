
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.ParamCoutTraitementVegetation;
import fr.sirs.core.model.ParamFrequenceTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.RefSousTraitementVegetation;
import fr.sirs.core.model.RefTraitementVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import fr.sirs.plugin.vegetation.TraitementSummary;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
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
    private final SirsStringConverter converter = new SirsStringConverter();
    private final Previews previews = session.getPreviews();

    /**
     * CellFactory pour le tableau récapitulatif des traitements.
     * => essaye d'interpréter la chaine de caractères comme un identifiant en
     * allant chercher le Preview correspondant, puis en délégant l'affichage
     * à un SirsStringConverter.
     * => si cela échoue, affiche la chaine de caractère donnée en paramètre.
     */
    private final Callback<TableColumn<ParamFrequenceTraitementVegetation, String>, TableCell<ParamFrequenceTraitementVegetation, String>> fromIdCellFactory =
            (TableColumn<ParamFrequenceTraitementVegetation, String> param) -> {
                return new TableCell<ParamFrequenceTraitementVegetation, String>(){
                    @Override
                    protected void updateItem(String item, boolean empty){
                        if(item == getItem()) return;

                        super.updateItem(item, empty);

                        if(item == null){
                            super.setText(null);
                        }
                        else {
                            try{
                                super.setText(converter.toString(previews.get(item)));
                            } catch (Exception e){
                                super.setText(item);
                            }
                        }
                        setGraphic(null);
                    }
                };
            };

    /**
     * CellFactory pour le tableau récapitulatif des traitements.
     * => essaye d'interpréter la chaine de caractères comme un identifiant en
     * allant chercher le Preview correspondant, puis en délégant l'affichage
     * à un SirsStringConverter.
     * => si cela échoue, affiche la chaine de caractère donnée en paramètre.
     */
    private final Callback<TableColumn<ParamCoutTraitementVegetation, String>, TableCell<ParamCoutTraitementVegetation, String>> fromIdCellFactory2 =
            (TableColumn<ParamCoutTraitementVegetation, String> param) -> {
                return new TableCell<ParamCoutTraitementVegetation, String>(){
                    @Override
                    protected void updateItem(String item, boolean empty){
                        if(item == getItem()) return;

                        super.updateItem(item, empty);

                        if(item == null){
                            super.setText(null);
                        }
                        else {
                            try{
                                super.setText(converter.toString(previews.get(item)));
                            } catch (Exception e){
                                super.setText(item);
                            }
                        }
                        setGraphic(null);
                    }
                };
            };

    private final PlanVegetation plan;
//    private final ObservableList<TraitementSummary> traitements = FXCollections.observableArrayList();

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

                final int index = initialFinPlan-initialDebutPlan;
                final List<ParcelleVegetation> parcelles = parcelleRepo.getByPlan(plan);
                for(final ParcelleVegetation parcelle : parcelles){
                    final List<Boolean> planifs = parcelle.getPlanifications();

                    // Si on est en mode automatique, il faut recalculer les planifications
                    if(parcelle.getModeAuto()){
                       PluginVegetation.resetAutoPlanif(parcelle, index);
                    }
                    // Si on n'est pas en mode automatique, il faut décaler les planifications déjà 
                    else{
                        /*==========================================================
                        Il faut commencer par décaler les planifications du nombre
                        d'années dont on a décalé le début, afin que les
                        planifications déjà programmées soient conservées et qu'on
                        ne soit pas obligé de tout refaire.
                        */

                        /*
                        Si la nouvelle date est antérieure, il faut ajouter des
                        planifications avant.
                        */
                        if(beginShift<0){
                            for(int i=0; i<(-beginShift); i++){
                                planifs.remove(0);
                            }
                        }

                        /*
                        Si la nouvelle date est postérieure, il faut retrancher autant
                        de planifications du début qu'il y a d'années de décalage, dans
                        la limite de la taille de la liste des planifications
                        */
                        else if(beginShift>0){
                            for(int i=0; i<beginShift && i<planifs.size(); i++){
                                planifs.add(0, Boolean.FALSE);
                            }
                        }



                        /*==========================================================
                        En dernier lieu, on ajuste la taille des listes de
                        planifications en ajoutant les planifications manquantes et
                        en retirant les planifications qui dépassent la date du plan.
                        */

                        // S'il n'y a pas assez d'éléments, il faut en rajouter
                        while(planifs.size()<index){
                            planifs.add(Boolean.FALSE);
                        }

                        // S'il y en a trop il faut en enlever
                        while(planifs.size()>index){
                            planifs.remove(index);
                        }
                    }
                }

                // Une fois toutes les planifications mises à jour, on
                // sauvegarde les parcelles du plan.
                parcelleRepo.executeBulk(parcelles);
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

    private static class ParamPojoTable<T> extends PojoTable{

        private final List<Class> vegetationClasses;
        private final SirsStringConverter converter = new SirsStringConverter();

        public ParamPojoTable(Class<T> pojoClass, String title) {
            super(pojoClass, title);
            // On garde les classes de zones de végétation.
            vegetationClasses = new ArrayList<>(Session.getElements());
            vegetationClasses.removeIf((Class c) -> !ZoneVegetation.class.isAssignableFrom(c));

            final TableColumn<T, Class> classColumn = new TableColumn<>("type de zone");
            classColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<T, Class>, ObservableValue<Class>>() {

                @Override
                public ObservableValue<Class> call(TableColumn.CellDataFeatures<T, Class> param) {

                    if(param.getValue() instanceof ParamFrequenceTraitementVegetation){
                        return ((ParamFrequenceTraitementVegetation) param.getValue()).typeProperty();
                    }
                    else if(param.getValue() instanceof ParamCoutTraitementVegetation){
                        return ((ParamCoutTraitementVegetation) param.getValue()).typeProperty();
                    }
                    else{
                        throw new IllegalArgumentException();
                    }
                }
            });
            classColumn.setCellFactory(new Callback<TableColumn<T, Class>, TableCell<T, Class>>() {

                @Override
                public TableCell<T, Class> call(TableColumn<T, Class> param) {
                    return new FXListTableCell<>(vegetationClasses, converter);
                }
            });
            getTable().getColumns().add(2, (TableColumn) classColumn);
        }

    }




    private static ParamFrequenceTraitementVegetation toParamFrequence(final TraitementSummary summary){
        final ParamFrequenceTraitementVegetation param = Injector.getSession().getElementCreator().createElement(ParamFrequenceTraitementVegetation.class);
        param.setType(summary.typeVegetationClass().get());
        param.setTypeVegetationId(summary.typeVegetationId().get());
        param.setTraitementId(summary.typeTraitementId().get());
        param.setSousTraitementId(summary.typeSousTraitementId().get());
        return param;
    }

    private static TraitementSummary toSummary(final ParamFrequenceTraitementVegetation param){
        return new TraitementSummary(param.getType(), param.getTypeVegetationId(), param.getTraitementId(), param.getSousTraitementId(), param.getFrequenceId(), true);
    }

    private static List<TraitementSummary> toSummaries(final Class<? extends ZoneVegetation> zoneType, final String typeZoneVegetationId){
        final List<TraitementSummary> summaries = new ArrayList<>();
        //Récupération des sous-types de traitement
        final List<RefSousTraitementVegetation> sousTraitements = Injector.getSession().getRepositoryForClass(RefSousTraitementVegetation.class).getAll();

        //Récupération des types de traitements
        final List<RefTraitementVegetation> traitements = Injector.getSession().getRepositoryForClass(RefTraitementVegetation.class).getAll();

        // On commence par créer une instance par type de traitement, pour un sous-type null
        for(final RefTraitementVegetation traitement : traitements){
            summaries.add(toSummary(zoneType, typeZoneVegetationId, traitement.getId(), null, traitement.getPonctuel()));
        }

        // Puis on parcours les sous-traitements
        for(final RefSousTraitementVegetation sousTraitement : sousTraitements){
            if(sousTraitement.getTraitementId()!=null){
                final RefTraitementVegetation traitement = Injector.getSession().getRepositoryForClass(RefTraitementVegetation.class).get(sousTraitement.getTraitementId());
                if(traitement!=null){
                    summaries.add(toSummary(zoneType, typeZoneVegetationId, sousTraitement.getTraitementId(), sousTraitement.getId(), traitement.getPonctuel()));
                }
            }
        }
        return summaries;
    }

    private static TraitementSummary toSummary(final Class<? extends ZoneVegetation> zonetype, final String typeZoneVegetationId, final String traitementId, final String sousTraitementId, final boolean ponctuel){
        return new TraitementSummary(zonetype, traitementId, sousTraitementId, null, ponctuel);
    }
}
