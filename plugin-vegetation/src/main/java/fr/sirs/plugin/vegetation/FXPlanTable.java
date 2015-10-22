
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.ZoneVegetation;
import static fr.sirs.plugin.vegetation.FXPlanTable.Mode.EXPLOITATION;
import static fr.sirs.plugin.vegetation.FXPlanTable.Mode.PLANIFICATION;
import static fr.sirs.plugin.vegetation.VegetationSession.NON_PLANIFIE_FUTUR;
import static fr.sirs.plugin.vegetation.VegetationSession.NON_PLANIFIE_NON_TRAITE;
import static fr.sirs.plugin.vegetation.VegetationSession.PLANIFIE_FUTUR;
import static fr.sirs.plugin.vegetation.VegetationSession.PLANIFIE_NON_TRAITE;
import static fr.sirs.plugin.vegetation.VegetationSession.estimatedPlanificationCost;
import static fr.sirs.plugin.vegetation.VegetationSession.exploitationCost;
import static fr.sirs.plugin.vegetation.VegetationSession.getParcelleEtat;
import static fr.sirs.plugin.vegetation.VegetationSession.setCheckBoxColor;
import fr.sirs.util.SirsStringConverter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.elasticsearch.common.base.Objects;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanTable extends BorderPane{

    public enum Mode{PLANIFICATION, EXPLOITATION};

    private static final String AUTO_STYLE = "-fx-border-color: lightgray; -fx-border-insets: 0; -fx-border-width: 0 0 0 3; -fx-label-padding: 0;";
    public static final String CHECKBOX_NO_LABEL_PADDING = "-fx-label-padding: 0;";

    private final PlanVegetation plan;
    private final List<ParcelleVegetation> tableParcelles = new ArrayList<>();
    private final Mode mode;
    
    private final Session session = Injector.getSession();
    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    private final GridPane gridTop = new GridPane();
    private final GridPane gridCenter = new GridPane();
    private final GridPane gridBottom = new GridPane();
    private Region[] headerNodes;

    public FXPlanTable(final PlanVegetation plan, final TronconDigue troncon, final Mode mode, final List<String> filteredStates, final int filterIndex){
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
        final CostGroup costGroup = new CostGroup();
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
            final CostCell costCell = new CostCell(year-dateStart);
            costGroup.addCell(costCell);
            gridBottom.add(costCell, colIndex, 0);
        }

        //une ligne par parcelle
        final ParcelleVegetationRepository parcelleRepo = (ParcelleVegetationRepository) session.getRepositoryForClass(ParcelleVegetation.class);
        final SirsStringConverter cvt = new SirsStringConverter();

        int rowIndex = 0;
        final List<ParcelleVegetation> planifParcelle = parcelleRepo.getByPlanId(plan.getDocumentId());
        for(final ParcelleVegetation parcelle : planifParcelle){

            // Clause de filtrage : on saute la ligne si une condition de filtrage est vérifiée pour l'année de filtrage
            if(filteredStates!=null && !filteredStates.isEmpty() && filterIndex>=0 && filterIndex<parcelle.getPlanifications().size()){
                final String parcelleEtat = getParcelleEtat(parcelle, parcelle.getPlanifications().get(filterIndex), filterIndex+plan.getAnneeDebut());
                final String internalEtat = NON_PLANIFIE_FUTUR.equals(parcelleEtat) ? NON_PLANIFIE_NON_TRAITE : (PLANIFIE_FUTUR.equals(parcelleEtat) ? PLANIFIE_NON_TRAITE : parcelleEtat);
                if(filteredStates.contains(internalEtat)) continue;
            }

            gridCenter.getRowConstraints().add(new RowConstraints(30, 30, 30, Priority.NEVER, VPos.CENTER, true));
            
            //on vérifie que la parcelle fait partie du troncon
            if(troncon!=null && !Objects.equal(parcelle.getForeignParentId(), troncon.getDocumentId())){
                continue;
            }
            
            // On ajoute la parcelle aux parcelles contenues dans le tableau (on en aura besoin pour les calculs des coûts).
            tableParcelles.add(parcelle);

            colIndex=0;

            /*==================================================================
            Colonne d'information et du libellé de la parcelle.
            */
            final Button toParcelle = new Button(null, new ImageView(SIRS.ICON_EDIT_BLACK));
            toParcelle.setOnAction(e -> Injector.getSession().showEditionTab(parcelle));
            toParcelle.setBackground(Background.EMPTY);
            toParcelle.setBorder(Border.EMPTY);
            toParcelle.setPadding(Insets.EMPTY);
            final Button infoButton = new Button(null, new ImageView(SIRS.ICON_INFO_CIRCLE_BLACK_16));
            infoButton.setOnAction(e -> new InfoStage(parcelle).showAndWait());
            infoButton.setBackground(Background.EMPTY);
            infoButton.setBorder(Border.EMPTY);
            infoButton.setPadding(Insets.EMPTY);
            final HBox infoParcelleBox = new HBox(toParcelle, infoButton);
            infoParcelleBox.setAlignment(Pos.CENTER);
            infoParcelleBox.setSpacing(.5);

            gridCenter.add(new Label(cvt.toString(parcelle), infoParcelleBox), colIndex, rowIndex);

            /*==================================================================
            Colonnes des planifications.
            */
            colIndex++;
            /*
            Groupement des planifications d'une parcelle de manière à ce que les
            cellules de dates coordonnent leurs changements.
            */
            final PlanifGroup planifGroup = new PlanifGroup();
            for(int year=dateStart; year<dateEnd; year++,colIndex++){
                final ParcelleDateCell parcelleDateCell = new ParcelleDateCell(parcelle, year, costGroup, planifGroup);
                gridCenter.add(parcelleDateCell, colIndex, rowIndex);
            }

            /*==================================================================
            En mode planification on ajoute la colonne de controle du mode auto.
            */
            if(mode==PLANIFICATION){
                colIndex++;
                final CheckBox modeAuto = new AutoModeCell(parcelle);
                gridCenter.add(modeAuto, colIndex, rowIndex);
            }

            rowIndex++;
        }

        costGroup.update();

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
     * Classe utilitaire permettant de recalculer l'ensemble des cellules de coût.
     */
    private class CostGroup {
        private final List<CostCell> estimationCells = new ArrayList<>();
        public void addCell(final CostCell cell){
            estimationCells.add(cell);
        }
        public void update(){
            for(final CostCell cell : estimationCells){
                cell.update();
            }
        }
    }

    /**
     * Cellule de date.
     *
     * Affiche l'état de planification d'une parcelle pour une année donnée.
     *
     * En mode d'exploitation, la couleur de la cellule indique la cohérence des
     * traitements d'exploitation avec les planifications.
     * 
     */
    private final class ParcelleDateCell extends CheckBox implements ListChangeListener<Boolean>{

        /**
         * Parcelle.
         */
        private final ParcelleVegetation parcelle;
        private final int year;
        private final int index;

        /**
         * Référence vers la cellule d'affichage de coût.
         *
         * Cette référence existe afin que les changements intervenus dans la
         * cellule courante puissent être répercutés sur la cellule de coût de
         * l'année correspondante.
         */
        private final CostGroup estGroup;

        /**
         * Le planifGroup sert à la cellule dans le cadre de la planification.
         *
         * Ce composant avertit la cellule que la planification a été modifiée
         * manuellement depuis une cellule de date du groupe.
         *
         * Cela permet de savoir qu'une mise à jour des planifications
         * automatiques est en cours et, tant que cette dernière n'est pas
         * achevée, de ne pas lancer de nouvelle mise à jour en cascade de cette
         * liste.
         */
        private final PlanifGroup planifGroup;

        /**
         * Écouteur des changements de l'état de la planification.
         *
         * L'état de la planification peut intervenir par sélection/désélection
         * directe de la boîte à cocher, mais elle peut également intervenir
         * automatiquement car la boîte à cocher écoute également la liste des
         * planifications de la parcelle pour se mettre à jour lorsqu'elle
         * change.
         */
        private final ChangeListener<Boolean> selectChangeListener;

        public ParcelleDateCell(final ParcelleVegetation parcelle, final int year,
                final CostGroup estCell, final PlanifGroup planifGroup) {
            disableProperty().bind(editable.not().or(new SimpleObjectProperty<>(mode).isEqualTo(EXPLOITATION)));
            this.parcelle = parcelle;
            this.year = year;
            this.index = year - plan.getAnneeDebut();
            this.estGroup = estCell;
            setPadding(new Insets(10));
            setAlignment(Pos.CENTER);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setStyle(CHECKBOX_NO_LABEL_PADDING);
            this.planifGroup = planifGroup;

            this.parcelle.getPlanifications().addListener(new WeakListChangeListener<>(this));

            setSelected(getVal());

            /*
            En planification on écoute les modification de sélection.
            Il ne faut pas l'activer en exploitation sinon cela provoque des
            conflits entre les deux panneaux.

            !!! SEUL LE PANNEAU DE PLANIFICATION DOIT AVOIR LA POSSIBILITÉ DE
            MODIFIER LES PLANIFS TOUT EN LES ÉCOUTANT !!!
            */
            if(mode==PLANIFICATION){
                selectChangeListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {

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

                    D'autre part, la planif est recalculée uniquement si on coche la case
                    à cocher, mais non si on la décoche.
                    */
                    if(this.parcelle.getModeAuto()
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
                        
                        this.estGroup.update();
                    }
                    else{
                        setVal(newValue);
                    }
                };
            }
            else {
                selectChangeListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> updateColor();
                updateColor();
            }

            selectedProperty().addListener(selectChangeListener);
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

        private void setVal(final Boolean v){
            if(index<parcelle.getPlanifications().size()){
                final Boolean old = parcelle.getPlanifications().get(index);
                if(!Objects.equal(old, v)){
                    parcelle.getPlanifications().set(index, v);
                    /*
                    On ne sauvegarde la parcelle que si un calcul de la planif
                    n'est pas en cours, car en cas de recalcul de la planif, on
                    sauvegardera l'état de la parcelle à la fin du processus.
                    */
                    if(!this.planifGroup.planifChangeProperty().get()){
                        save(parcelle);
                    }
                }
                updateColor();
                estGroup.update();
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
            if(mode==EXPLOITATION){
                setCheckBoxColor(this, getParcelleEtat(parcelle, getVal(), year));
            }
        }

        @Override
        public void onChanged(Change<? extends Boolean> c) {
            /*
            Si la liste est vide, on ne fait rien, car c'est qu'on est en train 
            de supprimer son contenu pour le réinitialiser depuis le méthode de 
            calcul des planifications automatiques.
            */
            if(!parcelle.getPlanifications().isEmpty()){
                final boolean val = getVal();
                if(val!=isSelected()) {
                    setSelected(val);
                }
            }
        }
    }

    /**
     * Cellule de contrôle du mode automatique d'une parcelle.
     */
    private final class AutoModeCell extends CheckBox{

        public AutoModeCell(ParcelleVegetation parcelle) {
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
     * Cellule d'affichage des coûts.
     *
     * Cette cellule dispose d'une méthode update() permettant de calculer les
     * coûts d'une année de planification.
     *
     * Le calcul est différent selon que l'affichage est en mode planification
     * ou exploitation.
     */
    private final class CostCell extends BorderPane{

        private final int index;
        private final Label label = new Label();

        public CostCell(final int index) {
            this.index = index;
            label.getStyleClass().add("pojotable-header");
            setCenter(label);

            update();
        }

        private void update(){

            final double cout;
            if(mode==PLANIFICATION){
                cout = estimatedPlanificationCost(plan, index, tableParcelles);
            }
            else {
                if(plan!=null){
                    cout = exploitationCost(plan.getAnneeDebut()+index, tableParcelles);
                }
                else {
                    throw new IllegalStateException("Plan must not be null");
                }
            }
            final NumberFormat numberFormat = new DecimalFormat("0.00");
            label.setText(numberFormat.format(cout));
        }

    }

    /**
     * Legend display.
     */
    private static class InfoStage extends Stage {

        private InfoStage(final ParcelleVegetation parcelle){
            final GridPane grid = new GridPane();
            grid.setPadding(new Insets(10, 10, 10, 10));
            grid.setHgap(10);
            grid.setVgap(10);

            final SirsStringConverter cvt = new SirsStringConverter();
            final Label lgdTitle = new Label("Peuplement détaillé de la parcelle "+cvt.toString(parcelle)+" :");
            lgdTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");
            grid.add(lgdTitle, 0, 0);

            final List<? extends ZoneVegetation> zones = AbstractZoneVegetationRepository.getAllZoneVegetationByParcelleId(parcelle.getId(), Injector.getSession());

            int i=1;
            for(final ZoneVegetation zone : zones){
                final StringBuilder label = new StringBuilder(cvt.toString(zone));
                if(zone instanceof PeuplementVegetation){
                    final String typeId = ((PeuplementVegetation)zone).getTypeVegetationId();
                    if(typeId!=null){
                        try{
                            final Preview preview = Injector.getSession().getPreviews().get(typeId);
                            final String typeLabel = cvt.toString(preview);
                            if(typeLabel!=null && !typeLabel.isEmpty()){
                                label.append(" (").append(typeLabel).append(") ");
                            }
                        }
                        catch(Exception e){
                            SIRS.LOGGER.log(Level.WARNING, e.getMessage());
                        }
                    }
                }
                else if(zone instanceof InvasiveVegetation){
                    final String typeId = ((InvasiveVegetation)zone).getTypeVegetationId();
                    if(typeId!=null){
                        try{
                            final Preview preview = Injector.getSession().getPreviews().get(typeId);
                            final String typeLabel = cvt.toString(preview);
                            if(typeLabel!=null && !typeLabel.isEmpty()){
                                label.append(" (").append(typeLabel).append(") ");
                            }
                        }
                        catch(Exception e){
                            SIRS.LOGGER.log(Level.WARNING, e.getMessage());
                        }
                    }
                }
                grid.add(new Label(label.toString()), 0, i);
                i++;
            }

            final Button ok = new Button("Fermer");
            ok.setOnAction(ev -> hide());
            grid.add(ok, 0, i);
            GridPane.setHalignment(ok, HPos.CENTER);

            setScene(new Scene(grid));
            initModality(Modality.APPLICATION_MODAL);
        }

    }

}
