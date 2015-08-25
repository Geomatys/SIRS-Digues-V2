
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ParamCoutTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.TraitementZoneVegetation;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.ZoneVegetation;
import static fr.sirs.plugin.vegetation.FXPlanTable.Mode.PLANIFICATION;
import fr.sirs.util.SirsStringConverter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import org.elasticsearch.common.base.Objects;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanTable extends BorderPane{

    public enum Mode{PLANIFICATION, EXPLOITATION};

    private static final String AUTO_STYLE = "-fx-border-color: lightgray;-fx-border-insets: 0;-fx-border-width: 0 0 0 3;-fx-label-padding: 0;";
    private static final String CHECKBOX_NOPADDING = "-fx-label-padding: 0;";

    private final PlanVegetation plan;
    private final List<ParcelleVegetation> tableParcelles = new ArrayList<>();
    private final Mode mode;
    private final List<EstimationCell> estimations = new ArrayList<>();
    private final Session session = Injector.getSession();
    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    
    public FXPlanTable(final PlanVegetation plan, final TronconDigue troncon, final Mode mode){
        this.plan = plan;
        this.mode = mode;

        // En-têtes
        final GridPane gridTop = new GridPane();
        gridTop.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setTop(gridTop);

        // Planifications des traitements
        final GridPane gridCenter = new GridPane();
        gridCenter.setMinSize(50, 50);
        gridCenter.setVgap(0);
        gridCenter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        final ScrollPane scroll = new ScrollPane(gridCenter);
        scroll.setMinSize(200, 200);
        scroll.setPrefSize(200, 200);
        scroll.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        setCenter(scroll);

        // Côut des traitements
        final GridPane gridBottom = new GridPane();
        gridBottom.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridBottom.setStyle("-fx-background-color: lightgray;");
        setBottom(gridBottom);

        //on crée et synchronize toutes les colonnes
        int dateStart = plan.getAnneeDebut();
        //NOTE : on ne peut pas afficher plus de X ans sur la table
        //on considere que l'enregistrement est mauvais et on evite de bloquer l'interface
        int dateEnd = Math.min(plan.getAnneeFin(), dateStart+20);

        //nom des types
        final Label fake0 = new Label();
        fake0.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        fake0.setMinSize(150, 20);
        final Label lblYear = new Label("Parcelle | Année");
        final Label lblSum;
        if(mode==PLANIFICATION){
            lblSum  = new Label("Somme*");
        }
        else{
            lblSum  = new Label("Somme");
        }
        lblYear.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblYear.getStyleClass().add("pojotable-header");
        lblSum .setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblSum.getStyleClass().add("pojotable-header");


        //colonne des noms
        ColumnConstraints cstTop    = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,USE_PREF_SIZE,Priority.NEVER,HPos.LEFT,true);
        ColumnConstraints cstCenter = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,USE_PREF_SIZE,Priority.NEVER,HPos.LEFT,true);
        ColumnConstraints cstBottom = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,USE_PREF_SIZE,Priority.NEVER,HPos.LEFT,true);
        gridTop.getColumnConstraints().add(cstTop);
        gridCenter.getColumnConstraints().add(cstCenter);
        gridBottom.getColumnConstraints().add(cstBottom);

        //une colonne par année
        for(int year=dateStart;year<dateEnd;year++){
            cstTop    = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,Double.MAX_VALUE,Priority.ALWAYS,HPos.CENTER,true);
            cstCenter = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,Double.MAX_VALUE,Priority.ALWAYS,HPos.CENTER,true);
            cstBottom = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,Double.MAX_VALUE,Priority.ALWAYS,HPos.CENTER,true);
            gridTop.getColumnConstraints().add(cstTop);
            gridCenter.getColumnConstraints().add(cstCenter);
            gridBottom.getColumnConstraints().add(cstBottom);
        }

        //on ajoute la colonne 'Mode auto'
        if(mode==PLANIFICATION){
            //colonne vide
            cstTop    = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,Double.MAX_VALUE,Priority.SOMETIMES,HPos.CENTER,true);
            cstCenter = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,Double.MAX_VALUE,Priority.SOMETIMES,HPos.CENTER,true);
            cstBottom = new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,Double.MAX_VALUE,Priority.SOMETIMES,HPos.CENTER,true);
            gridTop.getColumnConstraints().add(cstTop);
            gridCenter.getColumnConstraints().add(cstCenter);
            gridBottom.getColumnConstraints().add(cstBottom);
            
            //colonne mode auto
            cstTop    = new ColumnConstraints(67,67,67,Priority.NEVER,HPos.CENTER,true);
            cstCenter = new ColumnConstraints(50,50,50,Priority.NEVER,HPos.CENTER,true);
            cstBottom = new ColumnConstraints(50,50,50,Priority.NEVER,HPos.CENTER,true);
            gridTop.getColumnConstraints().add(cstTop);
            gridCenter.getColumnConstraints().add(cstCenter);
            gridBottom.getColumnConstraints().add(cstBottom);

            final Label lblAuto = new Label("Mode auto");
            lblAuto.setAlignment(Pos.CENTER);
            lblAuto.setWrapText(true);
            lblAuto.getStyleClass().add("pojotable-header");
            lblAuto.setStyle(AUTO_STYLE);

            gridTop   .add(lblAuto,       gridTop   .getColumnConstraints().size()-1, 0);
            gridBottom.add(new Label(""), gridBottom.getColumnConstraints().size()-1, 0);
        }


        //une ligne par parcelle
        final ParcelleVegetationRepository parcelleRepo = (ParcelleVegetationRepository) session.getRepositoryForClass(ParcelleVegetation.class);
        final SirsStringConverter cvt = new SirsStringConverter();
        int rowIndex = 0;
        int colIndex = 0;

        final List<ParcelleVegetation> planifParcelle = parcelleRepo.getByPlanId(plan.getDocumentId());
        for(final ParcelleVegetation parcelle : planifParcelle){
            gridCenter.getRowConstraints().add(new RowConstraints(30, 30, 30, Priority.NEVER, VPos.CENTER, true));
            
            //on vérifie que la parcelle fait partie du troncon
            if(troncon!=null && !Objects.equal(parcelle.getForeignParentId(), troncon.getDocumentId())){
                continue;
            }
            
            // On ajoute la parcelle aux parcelles contenues dans le tableau (on en aura besoin pour les calculs des coûts).
            tableParcelles.add(parcelle);

            colIndex=0;
            gridCenter.add(new Label(cvt.toString(parcelle)), colIndex, rowIndex);
            colIndex++;

            final CheckBox modeAuto = new ParcelleAutoCell(parcelle);
            
            for(int year=dateStart; year<dateEnd; year++,colIndex++){
                final CheckBox parcelleDateCell = new ParcelleDateCell(parcelle, year, year-dateStart);
                parcelleDateCell.disableProperty().bind(modeAuto.selectedProperty());
                gridCenter.add(parcelleDateCell, colIndex, rowIndex);
            }

            //on ajoute la colonne 'Mode auto'
            colIndex++;
            if(mode==PLANIFICATION){
                gridCenter.add(modeAuto, colIndex, rowIndex);
            }

            rowIndex++;
        }

        //on bind la taille des cellules
        gridCenter.add(fake0, 0, rowIndex);
        lblYear.prefWidthProperty().bind(fake0.widthProperty());
        lblSum .prefWidthProperty().bind(fake0.widthProperty());


        //ligne de dates et d'estimation
        gridTop.add(lblYear, 0, 0);
        gridBottom.add(lblSum, 0, 0);
        colIndex=1;
        for(int year=dateStart; year<dateEnd; year++,colIndex++){
            final Label lblYearN = new Label(""+year);
            lblYearN.getStyleClass().add("pojotable-header");
            lblYearN.setAlignment(Pos.CENTER);
            lblYearN.setPrefSize(20, 20);
            lblYearN.setMinSize(20, 20);
            lblYearN.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            gridTop.add(lblYearN, colIndex, 0);
            gridBottom.add(new EstimationCell(year-dateStart), colIndex, 0);
        }

        //ligne de commentaire
        if(mode==PLANIFICATION){
            gridBottom.add(new Label("* La somme prend en compte le coût de traitements des invasives"), 0, 1, 6, 1);
        }

    }

    private void save(ParcelleVegetation parcelle){
        VegetationSession.INSTANCE.getParcelleRepo().update(parcelle);
    }

    public BooleanProperty editableProperty(){
        return editable;
    }

//    public PlanVegetation getPlanVegetation() {
//        return plan;
//    }

    /**
     * Cellule de date.
     * 
     */
    private final class ParcelleDateCell extends CheckBox {

        private final ParcelleVegetation parcelle;
        private final int year;
        private final int index;

        public ParcelleDateCell(final ParcelleVegetation parcelle, final int year,  final int index) {
            disableProperty().bind(editable.not());
            this.parcelle = parcelle;
            this.year = year;
            this.index = index;
            setPadding(new Insets(10));
            setAlignment(Pos.CENTER);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setStyle(CHECKBOX_NOPADDING);

            if(parcelle.getPlanifications()==null){
                parcelle.setPlanifications(new ArrayList<>());
            }

            final WeakListChangeListener<Boolean> weakListener = new WeakListChangeListener<>(new ListChangeListener<Boolean>() {

                @Override
                public void onChanged(ListChangeListener.Change<? extends Boolean> c) {
                    setSelected(getVal());
                }
            });
            parcelle.getPlanifications().addListener(weakListener);

            setSelected(getVal());
            selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                setVal(newValue);
            });
            updateColor();
        }

        /**
         * return the value of the planification if exists. If there is no planification at the cell index, then returns false.
         * @return
         */
        private boolean getVal(){
            if(index<parcelle.getPlanifications().size()){
                return parcelle.getPlanifications().get(index);
            }
            else return false;
        }

        private void setVal(Boolean v){
            if(index<parcelle.getPlanifications().size()){
                final Boolean old = parcelle.getPlanifications().set(index, v);
                if(!Objects.equal(old,v)){
                    save(parcelle);
                    updateColor();
                }
            }
        }

        /**
         * On change la couleur en fonction de l'etat des traitements.
         * Void : Non planifié, Non traité
         * Orange : Non planifié, traité
         * Rouge : Planifié, Non traité
         * Vert : Planifié, traité
         */
        private void updateColor(){
            if(mode==PLANIFICATION) return;
            
            final boolean planifie = getVal();
            final Color color = VegetationSession.getParcelleEtatColor(parcelle, planifie, year);
            if(color==null){
                setBackground(Background.EMPTY);
            }else{
                setBackground(new Background(new BackgroundFill(color, new CornerRadii(30), Insets.EMPTY)));
            }
        }

    }

    /**
     * Colonne 'Mode Auto'.
     */
    private final class ParcelleAutoCell extends CheckBox{

        private final ParcelleVegetation parcelle;

        public ParcelleAutoCell(ParcelleVegetation parcelle) {
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            disableProperty().bind(editable.not());
            setSelected(parcelle.getModeAuto());
            selectedProperty().bindBidirectional(parcelle.modeAutoProperty());
            setStyle(AUTO_STYLE);
            this.parcelle = parcelle;
            setPadding(new Insets(5, 5, 5, 5));

            selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                // Si on sélectionne le modeAuto, alors il faut calculer la planification automatique
                if(newValue){
                    PluginVegetation.resetAutoPlanif(parcelle);
                }
                // Dans tous les cas, il faut sauvegader la modification faite sur la parcelle ne serait-ce que pour mémoriser le mode de planification.
                save(parcelle);
            });
        }
    }

    /**
     * Ligne des couts
     */
    private final class EstimationCell extends BorderPane{

        private final int index;
        private final Label label = new Label();

        public EstimationCell(final int index) {
            this.index = index;
            label.getStyleClass().add("pojotable-header");
            setCenter(label);

            /*
            En mode planification le coût suppose que l'on fasse la somme de
            tous les côuts de traitements des zones de la parcelle, dès que
            celle-ci est planifiée "traitée".
            
            Les coûts planifiés se trouvent dans le plan.
            */
            double cout = 0.;
            if(mode==PLANIFICATION){
                final ObservableList<ParamCoutTraitementVegetation> params = plan.getParamCout();
                final Map<TraitementSummary, ParamCoutTraitementVegetation> indexedParams = new HashMap<>();

                //On parcourt toutes les parcelles du tableau
                for (final ParcelleVegetation parcelle : tableParcelles){

                    // On vérifie que la parcelle est bien planifiée cette année
                    if(parcelle.getPlanifications().get(index)){

                        // On parcourt toutes les zones de végétation de la parcelle
                        final ObservableList<? extends ZoneVegetation> allZoneVegetationByParcelleId = AbstractZoneVegetationRepository.getAllZoneVegetationByParcelleId(parcelle.getId(), session);
                        for(final ZoneVegetation zone : allZoneVegetationByParcelleId){

                            // On vérifie que la zone a bien un traitement planifié et que celui-ci entre dans le plan de gestion
                            if(zone.getTraitement()!=null && !zone.getTraitement().getHorsGestion()){

                                // Dans ce cas, on construit un
                                final TraitementZoneVegetation traitement = zone.getTraitement();


                                // On vérifie ensuite que ce traitement se trouve dans les paramètres de coûts du plan en ce qui concerne son volet ponctuel et son volet non ponctuel
                                TraitementSummary ponctuelSummary = new TraitementSummary(zone.getClass(), traitement.getTraitementPonctuelId(), traitement.getSousTraitementPonctuelId(), null, true);
                                TraitementSummary nonPonctuelSummary = new TraitementSummary(zone.getClass(), traitement.getTraitementId(), traitement.getSousTraitementId(), traitement.getFrequenceId(), false);




                                // On parcourt enfin les paramètres de traitement du plan afin de trouver ceux qui correspondent aux traitements ponctuel et non ponctuel examinés

                                // 1- Un tel traitement a-t-il déjà été indexé ?
                                for(final TraitementSummary sum : indexedParams.keySet()){
                                    if(ponctuelSummary!=null && sum.equalsTraitementSummary(ponctuelSummary)){
                                        cout+=computeCost(zone, indexedParams.get(sum));
                                        ponctuelSummary=null;// On passe à null pour détecter qu'on a déjà fait le calcul
                                    }
                                    if(sum.equalsTraitementSummary(nonPonctuelSummary)){
                                        cout+=computeCost(zone, indexedParams.get(sum));
                                        nonPonctuelSummary=null; // On passe à null pour détecter qu'on a déjà fait le calcul
                                    }

                                    // On sort de la boucle dès qu'on détecte que les traitements ponctuel et non ponctuel ont été trouvés.
                                    if(ponctuelSummary==null && nonPonctuelSummary==null) break;
                                }

                                // 2- Si un tel traitement n'avait pas été indexé, on regarde les autres traitements
                                for(final ParamCoutTraitementVegetation param : params){
                                    // On vérifie que le paramètre n'a pas été déjà indexé
                                    if(!indexedParams.containsValue(param)){
                                        // Dans ce cas, on indexe le paramètre
                                        final TraitementSummary sum = TraitementSummary.toSummary(param);
                                        indexedParams.put(sum, param);

                                        // Puis on regarde s'il correspond au traitement ponctuel ou nonPonctuel
                                        if(ponctuelSummary!=null && sum.equalsTraitementSummary(ponctuelSummary)){
                                            cout+=computeCost(zone, param);
                                            ponctuelSummary=null;// On passe à null pour détecter qu'on a déjà fait le calcul
                                        }
                                        if(sum.equalsTraitementSummary(nonPonctuelSummary)){
                                            cout+=computeCost(zone, param);
                                            nonPonctuelSummary=null; // On passe à null pour détecter qu'on a déjà fait le calcul
                                        }

                                        // On sort de la boucle dès qu'on détecte que les traitements ponctuel et non ponctuel ont été trouvés.
                                        if(ponctuelSummary==null && nonPonctuelSummary==null) break;
                                    }
                                }
                            }
                        }
                    }
                }
            }



            final NumberFormat numberFormat = new DecimalFormat("0.00");
            label.setText(numberFormat.format(cout));
        }



        private void update(){
            //TODO
        }

    }

        private static double computeCost(final ZoneVegetation zone, final ParamCoutTraitementVegetation param){

            if(zone.getGeometry()!=null){
                return zone.getGeometry().getArea() * param.getCout();
            }
            else return 0.;
        }
}
