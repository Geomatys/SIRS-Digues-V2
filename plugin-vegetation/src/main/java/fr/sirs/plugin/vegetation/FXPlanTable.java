
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
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

    private static final String AUTO_STYLE = "-fx-border-color: lightgray;-fx-border-insets: 0;-fx-border-width: 0 0 0 3;-fx-label-padding: 0;";
    private static final String CHECKBOX_NOPADDING = "-fx-label-padding: 0;";

    private final PlanVegetation plan;
    private final boolean exploitation;
    private final List<EstimationCell> esimations = new ArrayList<>();
    private final Session session = Injector.getSession();
    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    public FXPlanTable(PlanVegetation plan, TronconDigue troncon, boolean exploitation){
        this.plan = plan;
        this.exploitation = exploitation;
        

        final GridPane gridCenter = new GridPane();
        final GridPane gridTop = new GridPane();
        final GridPane gridBottom = new GridPane();
        gridCenter.setMinSize(50, 50);
        gridCenter.setVgap(0);
        gridCenter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridTop.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridBottom.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridBottom.setStyle("-fx-background-color: lightgray;");

        final ScrollPane scroll = new ScrollPane(gridCenter);
        scroll.setMinSize(200, 200);
        scroll.setPrefSize(200, 200);
        scroll.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        setCenter(scroll);
        setBottom(gridBottom);
        setTop(gridTop);

        //on crée et synchronize toutes les colonnes
        int dateStart = plan.getAnneDebut();
        //NOTE : on ne peut pas afficher plus de X ans sur la table
        //on considere que l'enregistrement est mauvais et on evite de bloquer l'interface
        int dateEnd = Math.min(plan.getAnneFin(),dateStart+20);

        //nom des types
        final Label fake0 = new Label();
        fake0.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        fake0.setMinSize(150, 20);
        final Label lblYear = new Label("Année");
        final Label lblSum;
        if(!exploitation){
            lblSum  = new Label("Somme*");
        }else{
            lblSum  = new Label("Somme");
        }
        lblYear.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblSum .setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblYear.getStyleClass().add("pojotable-header");
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
        if(!exploitation){
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
        final ParcelleVegetationRepository parcelleRepo = (ParcelleVegetationRepository)session.getRepositoryForClass(ParcelleVegetation.class);
        final SirsStringConverter cvt = new SirsStringConverter();
        int rowIndex = 0;
        int colIndex = 0;
        final List<ParcelleVegetation> planifParcelle = parcelleRepo.getByPlanId(plan.getDocumentId());
        for(ParcelleVegetation parcelle : planifParcelle){
            gridCenter.getRowConstraints().add(new RowConstraints(30, 30, 30, Priority.NEVER, VPos.CENTER, true));
            
            //on vérifie que la parcelle fait partie du troncon
            if(troncon!=null && !Objects.equal(parcelle.getForeignParentId(),troncon.getDocumentId())){
                continue;
            }

            colIndex=0;
            gridCenter.add(new Label(cvt.toString(parcelle)), colIndex, rowIndex);
            colIndex++;

            for(int year=dateStart;year<dateEnd;year++,colIndex++){
                gridCenter.add(new ParcelleDateCell(parcelle,year,year-dateStart), colIndex, rowIndex);
            }

            //on ajoute la colonne 'Mode auto'
            colIndex++;
            if(!exploitation){
                gridCenter.add(new ParcelleAutoCell(parcelle), colIndex, rowIndex);
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
        for(int year=dateStart;year<dateEnd;year++,colIndex++){
            final Label lblYearN = new Label(""+year);
            lblYearN.getStyleClass().add("pojotable-header");
            lblYearN.setAlignment(Pos.CENTER);
            lblYearN.setPrefSize(20, 20);
            lblYearN.setMinSize(20, 20);
            lblYearN.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            gridTop.add(lblYearN, colIndex, 0);
            gridBottom.add(new EstimationCell(year), colIndex, 0);
        }

        //ligne de commentaire
        if(!exploitation){
            gridBottom.add(new Label("* La somme prend en compte le coût de traitements des invasives"), 0, 1, 6, 1);
        }

    }

    private void save(){
        final AbstractSIRSRepository<PlanVegetation> repo = session.getRepositoryForClass(PlanVegetation.class);
        repo.update(plan);
    }

    public BooleanProperty editableProperty(){
        return editable;
    }

    public PlanVegetation getPlanVegetation() {
        return plan;
    }

    /**
     * Cellule de date.
     * 
     */
    private final class ParcelleDateCell extends CheckBox{

        private final ParcelleVegetation parcelle;
        private final int year;
        private final int index;

        public ParcelleDateCell(ParcelleVegetation parcelle, int year,  int index) {
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

            setSelected(getVal());
            selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                setVal(newValue);
            });
            updateColor();
        }

        private boolean getVal(){
            final List<Boolean> planifications = parcelle.getPlanifications();
            while(planifications.size()<=index){
                planifications.add(Boolean.FALSE);
            }
            return planifications.get(index);
        }

        private void setVal(Boolean v){
            final List<Boolean> planifications = parcelle.getPlanifications();
            while(planifications.size()<=index){
                planifications.add(Boolean.FALSE);
            }
            final Boolean old = planifications.set(index,v);
            if(!Objects.equal(old,v)){
                save();
                updateColor();
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
            if(!exploitation) return;
            
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
                save();
            });
        }
    }

    /**
     * Ligne des couts
     */
    private final class EstimationCell extends BorderPane{

        private final int year;
        private final Label label = new Label("200.000");

        public EstimationCell(int year) {
            this.year = year;
            label.getStyleClass().add("pojotable-header");
            setCenter(label);
        }

        private void update(){
            //TODO
        }

    }

}
