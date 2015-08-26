
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.ParamCoutTraitementVegetation;
import fr.sirs.core.model.ParamFrequenceTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.RefSousTraitementVegetation;
import fr.sirs.core.model.RefTraitementVegetation;
import fr.sirs.core.model.RefTypeInvasiveVegetation;
import fr.sirs.core.model.RefTypePeuplementVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import fr.sirs.plugin.vegetation.TraitementSummary;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.FXDoubleCell;


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
    
//    private TableView<TraitementSummary> uiTraitementsTable;
    private TableView<ParamFrequenceTraitementVegetation> uiFrequenceTable;
    
    private final TableView<ParamCoutTraitementVegetation> uiCoutTable;
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
    private final Callback<TableColumn<TraitementSummary, String>, TableCell<TraitementSummary, String>> fromIdCellFactory =
            (TableColumn<TraitementSummary, String> param) -> {
                return new TableCell<TraitementSummary, String>(){
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

    public FXPlanVegetationPane(PlanVegetation plan) {
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

        initTraitements(null);
        uiFrequenceTable = new TableView<>(plan.getParamFrequence());

        final TableColumn<TraitementSummary, Class<? extends ZoneVegetation>> vegetationColumn = new TableColumn<>("Type de zone");
        vegetationColumn.setCellValueFactory((TableColumn.CellDataFeatures<TraitementSummary, Class<? extends ZoneVegetation>> param) -> param.getValue().typeVegetationClass());
        vegetationColumn.setCellFactory((TableColumn<TraitementSummary, Class<? extends ZoneVegetation>> param) -> {
                return new TableCell<TraitementSummary, Class<? extends ZoneVegetation>>() {

                    @Override
                    protected void updateItem(Class<? extends ZoneVegetation> item, boolean empty) {
                        if (item == getItem()) return;

                        super.updateItem(item, empty);

                        if (item == null) {
                            super.setText(null);
                        } else {
                            super.setText(converter.toString(item));
                        }
                        super.setGraphic(null);
                    }
                };
        });

        final TableColumn<TraitementSummary, String> typeTraitementColumn = new TableColumn<>("Type de traitement");
        typeTraitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<TraitementSummary, String> param) -> param.getValue().typeTraitementId());
        typeTraitementColumn.setCellFactory(fromIdCellFactory);
        final TableColumn<TraitementSummary, String> typeSousTraitementColumn = new TableColumn<>("Sous-type de traitement");
        typeSousTraitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<TraitementSummary, String> param) -> param.getValue().typeSousTraitementId());
        typeSousTraitementColumn.setCellFactory(fromIdCellFactory);
        final TableColumn<TraitementSummary, String> frequenceTraitementColumn = new TableColumn<>("Fréquence de traitement");
        frequenceTraitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<TraitementSummary, String> param) -> {
            final TraitementSummary sum = param.getValue();
            if(sum.ponctuel().get())
                return new SimpleStringProperty("Ponctuel");
            else
                return sum.typeFrequenceId();
                });
        frequenceTraitementColumn.setCellFactory(fromIdCellFactory);
//        uiFrequenceTable.getColumns().addAll(vegetationColumn, typeTraitementColumn, typeSousTraitementColumn, frequenceTraitementColumn);

        // Construction du titre
        final Label uiTraitementsTableTitle = new Label("Traitements par type de zone de végétation");
        uiTraitementsTableTitle.getStyleClass().add("pojotable-header");

        // Bouton de raffraîchissement du tableau
        final Button uiTraitementsRefresh = new Button("Mettre à jour");
        uiTraitementsRefresh.setOnAction(this::initTraitements);

        // Panneau de séparation (mise en forme)
        final Pane separaPane1 = new Pane();
        separaPane1.setPrefSize(USE_PREF_SIZE, USE_PREF_SIZE);
        separaPane1.setMaxSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        // Construction de l'en-tête
        final HBox uiTraitementsTableHeader = new HBox(uiTraitementsTableTitle, separaPane1, uiTraitementsRefresh);
        HBox.setHgrow(separaPane1, Priority.ALWAYS);
        uiTraitementsTableHeader.setPadding(new Insets(15, 5, 5, 5));


        ////////////////////////////////////////////////////////////////////////
        // Construction des paramètes de coûts.
        ////////////////////////////////////////////////////////////////////////
        uiCoutTable = new TableView<>(plan.getParamCout());
        uiCoutTable.setEditable(true);

        final TableColumn<ParamCoutTraitementVegetation, String> traitementColumn = new TableColumn<>("Type de traitement");
        traitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<ParamCoutTraitementVegetation, String> param) -> param.getValue().traitementIdProperty());
        traitementColumn.setCellFactory(fromIdCellFactory2);
        traitementColumn.setEditable(false);
        final TableColumn<ParamCoutTraitementVegetation, String> sousTraitementColumn = new TableColumn<>("Sous-type de traitement");
        sousTraitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<ParamCoutTraitementVegetation, String> param) -> param.getValue().sousTraitementIdProperty());
        sousTraitementColumn.setCellFactory(fromIdCellFactory2);
        sousTraitementColumn.setEditable(false);
        final TableColumn<ParamCoutTraitementVegetation, Number> coutColumn = new TableColumn<>("Coût");
        coutColumn.setCellValueFactory((TableColumn.CellDataFeatures<ParamCoutTraitementVegetation, Number> param) -> param.getValue().coutProperty());
        coutColumn.setCellFactory((TableColumn<ParamCoutTraitementVegetation, Number> param) -> new FXDoubleCell<>(0.));
        uiCoutTable.getColumns().addAll(traitementColumn, sousTraitementColumn, coutColumn);

        // Construction du titre
        final Label uiCoutTableTitle = new Label("Paramétrage des coûts");
        uiCoutTableTitle.getStyleClass().add("pojotable-header");

        //Bouton de raffraîchissement du tableau
        final Button uiCostRefresh = new Button("Mettre à jour");
        uiCostRefresh.setOnAction(this::initCosts);

        // Panneau de séparation (mise en forme)
        final Pane separaPane2 = new Pane();
        separaPane2.setPrefSize(USE_PREF_SIZE, USE_PREF_SIZE);
        separaPane2.setMaxSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        // Construction de l'en-tête
        final HBox uiCoutsTableHeader = new HBox(uiCoutTableTitle, separaPane2, uiCostRefresh);
        HBox.setHgrow(separaPane2, Priority.ALWAYS);
        uiCoutsTableHeader.setPadding(new Insets(15, 5, 5, 5));

        // Mise en page
        uiVBox.getChildren().addAll(uiCoutsTableHeader, uiCoutTable, uiTraitementsTableHeader, uiFrequenceTable);
    }

    /**
     * Méthode d'initialisation des coûts.
     *
     * Les coûts sont enregistrés dans la parcelle, mais dépendent des
     * traitements dans le sens où d'une part chaque combinaison de traitement
     * présente dans la table de recension des traitements doit avoir un coût
     * associé et réciproquement les coûts n'ont de sens que lorsqu'ils
     * correspondent à une combinaison de traitements existant dans le plan.
     *
     * Cela a plusieurs conséquences en termes d'édition :
     *
     * 1)
     * Il ne faut donc pas laisser totalement la main sur l'édition de la table
     * afin de ne pas permettre d'ajouter n'importe quel coût pour un traitement
     * qui ne correspondrait à rien, mais seulement autoriser l'édition des
     * montants.
     *
     * 2)
     * Il faut néanmoins pouvoir mettre le tableau à jour afin d'y ajouter des
     * paramétrages de coûts pour de nouvelles combinaisons de traitements et
     * d'en retirer les paramétrages de coûts qui ne correspondent plus à aucun
     * traitement.
     *
     * Ces opérations de mise à jour sont l'objet de cette méthode.
     *
     * @param event
     */
    private void initCosts(ActionEvent event){
        /*
        1) Les traitements sont disponibles dans la liste des traitements et les
        paramètres de coûts dans la liste des paramètres de coûts du plan.

        Il faut les parcourir et les comparer de manière à recenser les
        paramètres de coûts qui ne correspondent plus à aucun traitement et les
        traitements pour lesquels il n'existe pas de paramètre de coût.

        On parcours tous les paramètres et les traitements de manière à
        détecter les associations.
        */
//        final List<TraitementSummary> traitementsAvecCout = new ArrayList<>();
//        final List<ParamCoutTraitementVegetation> coutAvecTraitement = new ArrayList<>();
//        for(final ParamCoutTraitementVegetation param : plan.getParamCout()){
//            final TraitementSummary paramTraitementStub = TraitementSummary.toSummary(param);
//            for(final TraitementSummary traitement : traitements){
//                if(paramTraitementStub.equalsTraitementSummary(traitement)){
//                    traitementsAvecCout.add(traitement);
//                    coutAvecTraitement.add(param);
//                }
//            }
//        }

//        /*
//        On retranche des traitements, ceux qui ont un coût déterminé afin d'avoir une liste des traitements sans coût.
//        */
//        final List<TraitementSummary> traitementsSansCout = new ArrayList<>(traitements);
//        traitementsSansCout.removeAll(traitementsAvecCout);
//
//        /*
//        On retranche de la liste des coûts ceux qui n'ont pas de traitement associé.
//        */
//        final List<ParamCoutTraitementVegetation> coutSansTraitement = new ArrayList<>(plan.getParamCout());
//        coutSansTraitement.removeAll(coutAvecTraitement);

//        // On commence par retrancher de la liste des paramètres ceux qui ne servent plus à rien
//        plan.getParamCout().removeAll(coutSansTraitement);
//
//        // Puis on crée des paramètres pour les traitements qui n'ont pas de coût
//        for(final TraitementSummary traitement : traitementsSansCout){
//            final ParamCoutTraitementVegetation param = session.getElementCreator().createElement(ParamCoutTraitementVegetation.class);
//            param.getId();// On affecte un Id à l'élément imbriqué
//            param.setTraitementId(traitement.typeTraitementId().get());
//            param.setSousTraitementId(traitement.typeSousTraitementId().get());
//            plan.getParamCout().add(param);
//        }

        planRepo.update(plan);
    }

    /**
     * Méthode d'initialisation des paramètres de fréquences de traitement.
     *
     * On commence par récupérer les paramètres du plan.
     *
     *
     *
     * On commence par parcourir les zones de végétation de toutes les
     * parcelles du plan.
     *
     * Pour chaque zone, on récupère le traitement qui lui est associé.
     *
     * |-> S'il n'y a pas de traitement associé, ou si le traitement est hors
     *     gestion, on passe à la zone suivante.
     *
     * |-> Avec ce traitement, s'il exite et doit être pris en compte dans la
     *     gestion, on construit deux "résumés de traitement", un pour le
     *     traitement ponctuel et un pour le traitemnet non ponctuel,
     *     mémorisant le type de la zone de végétation, le type de traitement,
     *     le type de sous-traitement et un booleen indiquant la ponctualité
     *     du traitement. En plus, les traitements non ponctuels mémorisent la
     *     fréquence de traitement.
     *     [Note: on a besoin du booleen indiquant la ponctualité du traitement
     *     car la nullité de la fréquence n'est pas suffisante (on pourrait
     *     avoir une fréquence nulle pour un traitement non ponctuel si celle-
     *     -ci n'avait pas été initializée). On serait alors contraint de faire
     *     une requête sur la liste de référence du type de traitement afin de
     *     vérifier s'il est ponctuel ou non.]
     *     On vérifie ensuite que les résumés de traitement ne sont pas déjà
     *     dans la liste des traitements avant de les y ajouter.
     *
     *     À l'issue de cette étape on a donc une liste des traitements
     *     recensés dans le plan de gestion.
     * 
     * @param event
     */
    private void initTraitements(ActionEvent event){

        /*
        On commence par récupérer les paramètres du plan.
        */
        final ObservableList<ParamFrequenceTraitementVegetation> params = plan.getParamFrequence();

        /*
        Il faut ensuite vérifier que ces paramètres sont à jour, c'est-à-dire
        qu'ils concernent toujours des combinaisons de traitements existantes.

        Les combinaisons de traitement existantes sont composées :
        -d'un type de zone de végétation
        -d'un type de peuplement (qui selon le type de zone de végétation)
        -d'un type de traitement
        -d'un sous-type de traitement (selon le type de traitement)
        */
        final List<TraitementSummary> summaries = new ArrayList<>();
        
        final List<Class<? extends ZoneVegetation>> zoneTypes = new ArrayList<>();
        for(final Class clazz : Session.getElements()){
            if(ZoneVegetation.class.isAssignableFrom(clazz)){
                zoneTypes.add(clazz);
            }
        }

        //Récupération des types de peuplement
        final List<RefTypePeuplementVegetation> peuplementTypes = Injector.getSession().getRepositoryForClass(RefTypePeuplementVegetation.class).getAll();

        //Récupération des types d'invasives
        final List<RefTypeInvasiveVegetation> invasiveTypes = Injector.getSession().getRepositoryForClass(RefTypeInvasiveVegetation.class).getAll();

        // Maintenant qu'on a tout récupéré il faut construire les combinaisons possibles
        for(final Class<? extends ZoneVegetation> clazz : zoneTypes){
            if(PeuplementVegetation.class.isAssignableFrom(clazz)){
                for(final RefTypePeuplementVegetation peuplemenentType : peuplementTypes){
                    summaries.addAll(toSummaries(clazz, peuplemenentType.getId()));
                }
            }
            else if(InvasiveVegetation.class.isAssignableFrom(clazz)){
                for(final RefTypeInvasiveVegetation invasiveType : invasiveTypes){
                    summaries.addAll(toSummaries(clazz, invasiveType.getId()));
                }
            }
            else{
                summaries.addAll(toSummaries(clazz, null));
            }
        }


        /*
        À ce stade, on doit avoir toutes les combinaisons possibles de traitements.

        Il faut commencer par indexer les paramétrages de fréquences par leur résumé de traitement afin de pouvoir les comparer.
        */

        final Map<TraitementSummary, ParamFrequenceTraitementVegetation> index = new HashMap<>();
        for(final ParamFrequenceTraitementVegetation param : params){
            index.put(toSummary(param), param);
        }

        /*
        Il faut ensuite retirer des paramètres, ceux qui n'existent plus.
        */
        final Iterator<TraitementSummary> it = index.keySet().iterator();
        while(it.hasNext()){
            final TraitementSummary sum = it.next();
            for(final TraitementSummary refSum : summaries){
                if(false){// IL FAUT ICI TESTER L'ÉGALITÉ AU SENS DES PARAMÉTRAGES DE FRÉQUENCES
                    index.remove(sum);
                    it.remove();
                }
            }
        }

        /*
        Enfin, il faut ajouter comme paramètres les nouvelles combinaisons qui seraient apparues.
        */
        


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
        return new TraitementSummary(zonetype, sousTraitementId, sousTraitementId, traitementId, ponctuel);
    }
}
