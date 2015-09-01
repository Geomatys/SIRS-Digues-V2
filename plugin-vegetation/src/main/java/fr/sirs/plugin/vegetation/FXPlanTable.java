
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.plugin.vegetation.FXPlanTable.Mode.PLANIFICATION;
import fr.sirs.util.SirsStringConverter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
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
import javafx.scene.layout.Region;
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
    
    private final Session session = Injector.getSession();
    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    private final GridPane gridTop = new GridPane();
    private final GridPane gridCenter = new GridPane();
    private final GridPane gridBottom = new GridPane();
    private Region[] headerNodes;
    
    public FXPlanTable(final PlanVegetation plan, final TronconDigue troncon, final Mode mode){
        this.plan = plan;
        this.mode = mode;

        // En-têtes
        gridTop.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setTop(gridTop);

        // Planifications des traitements
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
        gridBottom.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridBottom.setStyle("-fx-background-color: lightgray;");
        setBottom(gridBottom);

        //on crée et synchronize toutes les colonnes
        int dateStart = plan.getAnneeDebut();
        //NOTE : on ne peut pas afficher plus de X ans sur la table
        //on considere que l'enregistrement est mauvais et on evite de bloquer l'interface
        int dateEnd = Math.min(plan.getAnneeFin(), dateStart+20);

        //nom des types
        final Label lblYear = new Label("Parcelle | Année");
        final Label lblSum;
        if(mode==PLANIFICATION){
            lblSum  = new Label("Somme*");
        }else{
            lblSum  = new Label("Somme");
        }
        lblYear.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblYear.getStyleClass().add("pojotable-header");
        lblSum .setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblSum.getStyleClass().add("pojotable-header");

        //ecouteur sur les composants de la premiere ligne
        final ChangeListener widthListener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> updateColumnWidth();

        //colonne des noms
        gridTop.getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,USE_PREF_SIZE,Priority.NEVER,HPos.LEFT,true));

        //une colonne par année
        for(int year=dateStart;year<dateEnd;year++){
            gridTop.getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,Double.MAX_VALUE,Priority.ALWAYS,HPos.CENTER,true));
        }

        //on ajoute la colonne 'Mode auto'
        if(mode==PLANIFICATION){
            headerNodes = new Region[1+dateEnd-dateStart+2];
            //colonne vide
            gridTop.getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE,USE_COMPUTED_SIZE,Double.MAX_VALUE,Priority.SOMETIMES,HPos.CENTER,true));
            final Label lblVide = new Label();
            lblVide.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            gridTop.add(lblVide, headerNodes.length-2, 0);
            headerNodes[headerNodes.length-2] = lblVide;
            lblVide.widthProperty().addListener(widthListener);

            //colonne mode auto
            gridTop.getColumnConstraints().add(new ColumnConstraints(67,67,67,Priority.NEVER,HPos.CENTER,true));

            final Label lblAuto = new Label("Mode auto");
            lblAuto.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            lblAuto.setAlignment(Pos.CENTER);
            lblAuto.setWrapText(true);
            lblAuto.getStyleClass().add("pojotable-header");
            lblAuto.setStyle(AUTO_STYLE);
            lblAuto.widthProperty().addListener(widthListener);
            headerNodes[headerNodes.length-1] = lblAuto;

            gridTop   .add(lblAuto,       headerNodes.length-1, 0);
            gridBottom.add(new Label(""), headerNodes.length-1, 0);
        }else{
            headerNodes = new Region[1+dateEnd-dateStart];
        }

        int colIndex = 0;

        headerNodes[0] = lblYear;

        //ligne de dates et d'estimation
        lblYear.widthProperty().addListener(widthListener);
        gridTop.add(lblYear, 0, 0);
        gridBottom.add(lblSum, 0, 0);
        colIndex=1;
        final EstimationCell[] estCells = new EstimationCell[dateEnd-dateStart];
        for(int year=dateStart; year<dateEnd; year++,colIndex++){
            final Label lblYearN = new Label(""+year);
            lblYearN.getStyleClass().add("pojotable-header");
            lblYearN.setAlignment(Pos.CENTER);
            lblYearN.setPrefSize(20, 20);
            lblYearN.setMinSize(20, 20);
            lblYearN.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            lblYearN.widthProperty().addListener(widthListener);
            headerNodes[colIndex] = lblYearN;
            gridTop.add(lblYearN, colIndex, 0);
            estCells[year-dateStart] = new EstimationCell(year-dateStart);
            gridBottom.add(estCells[year-dateStart], colIndex, 0);
        }

        //une ligne par parcelle
        final ParcelleVegetationRepository parcelleRepo = (ParcelleVegetationRepository) session.getRepositoryForClass(ParcelleVegetation.class);
        final SirsStringConverter cvt = new SirsStringConverter();

        int rowIndex = 0;
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

            final PlanifGroup planifGroup = new PlanifGroup();
            for(int year=dateStart; year<dateEnd; year++,colIndex++){
                final ParcelleDateCell parcelleDateCell = new ParcelleDateCell(parcelle, year, year-dateStart, estCells[year-dateStart], planifGroup);
                parcelleDateCell.autoProperty().bind(modeAuto.selectedProperty());
                gridCenter.add(parcelleDateCell, colIndex, rowIndex);
            }

            //on ajoute la colonne 'Mode auto'
            colIndex++;
            if(mode==PLANIFICATION){
                gridCenter.add(modeAuto, colIndex, rowIndex);
            }

            rowIndex++;
        }

        for(int year=dateStart; year<dateEnd; year++,colIndex++){
            estCells[year-dateStart].update();
        }

        //ligne de commentaire
        if(mode==PLANIFICATION){
            gridBottom.add(new Label("* La somme prend en compte le coût de traitements des invasives"), 0, 1, 6, 1);
        }

    }

    private void updateColumnWidth(){
        gridCenter.getColumnConstraints().clear();
        gridBottom.getColumnConstraints().clear();

        for(int i=0;i<headerNodes.length;i++){
            gridCenter.getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE,headerNodes[i].getWidth(),USE_PREF_SIZE,Priority.NEVER,HPos.CENTER,true));
            gridBottom.getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE,headerNodes[i].getWidth(),USE_PREF_SIZE,Priority.NEVER,HPos.CENTER,true));
        }

    }

    private void save(ParcelleVegetation parcelle){
        VegetationSession.INSTANCE.getParcelleRepo().update(parcelle);
    }

    public BooleanProperty editableProperty(){
        return editable;
    }

    /**
     * Classe utilitaire permettant à une cellule de date de la planif de savoir
     * quand la planif est modifiée depuis une autre cellule de date.
     */
    private class PlanifGroup {
        private final BooleanProperty planifChangeProperty = new SimpleBooleanProperty(false);
        public BooleanProperty planifChangeProperty() {return planifChangeProperty;}
    }

    /**
     * Cellule de date.
     * 
     */
    private final class ParcelleDateCell extends CheckBox implements ListChangeListener<Boolean>{

        private final ParcelleVegetation parcelle;
        private final int year;
        private final int index;
        private final EstimationCell estCell;
        private final BooleanProperty autoProperty = new SimpleBooleanProperty();
        private final PlanifGroup planifGroup;

        public ParcelleDateCell(final ParcelleVegetation parcelle, final int year,  
                final int index, final EstimationCell estCell, final PlanifGroup planifGroup) {
            disableProperty().bind(editable.not());
            this.parcelle = parcelle;
            this.year = year;
            this.index = index;
            this.estCell = estCell;
            setPadding(new Insets(10));
            setAlignment(Pos.CENTER);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setStyle(CHECKBOX_NOPADDING);
            this.planifGroup = planifGroup;

            this.parcelle.getPlanifications().addListener(new WeakListChangeListener<>(this));

            setSelected(getVal());
            selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {

                /*
                Si on est en mode auto, il faut regarder si on cherche à
                modifier une planification du passé ou une planification de
                l'année en cours ou d'une année future.

                Si on modifie la planif de l'année en cours ou d'une année
                future, il suffit de recalculer la planification automatique à
                partir de cette année. La cellule en cours écoutant la liste des
                planifications, elle se mettra à jour.

                Si on modifie une valeur du passé, cela doit rester sans effet
                sur la planification.
                */
                if(autoProperty.get() 
                        && newValue
                        && this.index>=LocalDate.now().getYear()-plan.getAnneeDebut()){
                    /*
                    Comme la cellule écoute la liste, le changement d'état sélectionné
                    provoquant la mise à jour de la liste des planifications déclenche
                    à son tour le changement d'état sélectionné et ainsi de suite.

                    Il faut donc veiller, avant de lancer une mise à jour de la liste,
                    que celle-ci n'est pas déjà en cours.
                    */
                    if(!this.planifGroup.planifChangeProperty().get()){
                        /*
                        On bloque la mise à jour de la planif depuis les autres
                        cellules de date de la planif lorque leur propriété de
                        sélection sera modifiée du fait de la modification de la
                        planification.
                        */
                        this.planifGroup.planifChangeProperty().set(true);
                        PluginVegetation.resetAutoPlanif(this.parcelle, this.index);
                        
                        // On sauvegarde le nouvel état de la parcelle avec ses planifications
                        save(parcelle);

                        // On change l'état de planification en cours : FAUX
                        this.planifGroup.planifChangeProperty().set(false);
                    }

                    
                    updateColor();
                    estCell.update();
                }
                else{
                    setVal(newValue);
                }
            });
            updateColor();
        }
        
        public BooleanProperty autoProperty(){return autoProperty;}

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

        private void setVal(final Boolean v){
            if(index<parcelle.getPlanifications().size()){
                final Boolean old = parcelle.getPlanifications().set(index, v);
                if(!Objects.equal(old, v)){
                    /*
                    On ne sauvegarde la parcelle que si un calcul de la planif
                    n'est pas en cours, car en cas de recalcul de la planif, on
                    sauvegardera l'état de la parcelle à la fin du processus.
                    */
                    if(!this.planifGroup.planifChangeProperty().get()){
                        save(parcelle);
                    }
                    updateColor();
                    estCell.update();
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

        @Override
        public void onChanged(Change<? extends Boolean> c) {
            if(getVal()!=isSelected()) {
                setSelected(getVal());
            }
        }

    }

    /**
     * Colonne 'Mode Auto'.
     */
    private final class ParcelleAutoCell extends CheckBox{

        public ParcelleAutoCell(ParcelleVegetation parcelle) {
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            disableProperty().bind(editable.not());
            setSelected(parcelle.getModeAuto());
            selectedProperty().bindBidirectional(parcelle.modeAutoProperty());
            setStyle(AUTO_STYLE);
            setPadding(new Insets(5, 5, 5, 5));

            selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
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

            update();
        }

        private void update(){
            
            final double cout;
            if(mode==PLANIFICATION){
                cout = VegetationSession.estimateCoutPlanification(plan, index, tableParcelles);
            }
            else {
                if(plan!=null){
                    cout = VegetationSession.coutExploitation(plan.getAnneeDebut()+index, tableParcelles);
                }
                else {
                    throw new IllegalStateException("Plan must not be null");
                }
            }
            final NumberFormat numberFormat = new DecimalFormat("0.00");
            label.setText(numberFormat.format(cout));
        }

    }

}
