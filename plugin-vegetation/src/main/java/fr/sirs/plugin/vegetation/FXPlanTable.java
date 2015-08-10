
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.PlanifParcelleVegetation;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanTable extends BorderPane{

    private static final String AUTO_STYLE = "-fx-border-color: lightgray;-fx-border-insets: 0;-fx-border-width: 0 0 0 3;\n";

    private final PlanVegetation plan;
    private final boolean exploitation;
    private final List<EstimationCell> esimations = new ArrayList<>();
    private final Session session = Injector.getSession();
    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    public FXPlanTable(PlanVegetation plan, boolean exploitation){
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
        int dateStart = 2009;
        int dateEnd = 2019;

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
        final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo = session.getRepositoryForClass(ParcelleVegetation.class);
        final SirsStringConverter cvt = new SirsStringConverter();
        int rowIndex = 0;
        int colIndex = 0;
        final List<PlanifParcelleVegetation> planifParcelle = plan.getPlanifParcelle();
        for(PlanifParcelleVegetation state : planifParcelle){
            gridCenter.getRowConstraints().add(new RowConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, VPos.CENTER, true));
            final ParcelleVegetation parcelle = parcelleRepo.get(state.getParcelleId());
            colIndex=0;
            gridCenter.add(new Label(cvt.toString(parcelle)), colIndex, rowIndex);
            colIndex++;

            for(int year=dateStart;year<dateEnd;year++,colIndex++){
                gridCenter.add(new ParcelleDateCell(parcelle,state,year), colIndex, rowIndex);
            }

            //on ajoute la colonne 'Mode auto'
            colIndex++;
            if(!exploitation){
                gridCenter.add(new ParcelleAutoCell(parcelle,state), colIndex, rowIndex);
            }

            rowIndex++;
        }

        //on bind la taille des cellules
        gridCenter.add(fake0, 0, rowIndex);
        lblYear.prefWidthProperty().bind(fake0.widthProperty());
        lblSum .prefWidthProperty().bind(fake0.widthProperty());


        //ligne d'estimation
        gridTop.add(lblYear, 0, 0);
        gridBottom.add(lblSum, 0, 0);
        colIndex=1;
        for(int year=dateStart;year<dateEnd;year++,colIndex++){
            final Label lblYearN = new Label(""+year);
            lblYearN.getStyleClass().add("pojotable-header");
            gridTop.add(lblYearN, colIndex, 0);
            gridBottom.add(new EstimationCell(year), colIndex, 0);
        }

        //ligne de commentaire
        if(!exploitation){
            gridBottom.add(new Label("* La somme prend en compte le coût de traitements des invasives"), 0, 1, 6, 1);
        }

    }

    public BooleanProperty editableProperty(){
        return editable;
    }

    public PlanVegetation getPlanVegetation() {
        return plan;
    }


    private final class ParcelleDateCell extends BorderPane{

        private final ParcelleVegetation parcelle;
        private final PlanifParcelleVegetation state;
        private final int year;
        private final CheckBox checkBox = new CheckBox();

        public ParcelleDateCell(ParcelleVegetation parcelle, PlanifParcelleVegetation state, int year) {
            checkBox.disableProperty().bind(editable.not());
            this.parcelle = parcelle;
            this.state = state;
            this.year = year;
            setCenter(checkBox);
            setPadding(new Insets(10, 10, 10, 10));
        }

    }

    private final class ParcelleAutoCell extends BorderPane{

        private final ParcelleVegetation parcelle;
        private final PlanifParcelleVegetation state;
        private final CheckBox checkBox = new CheckBox();

        public ParcelleAutoCell(ParcelleVegetation parcelle, PlanifParcelleVegetation state) {
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            checkBox.disableProperty().bind(editable.not());
            setStyle(AUTO_STYLE);
            this.parcelle = parcelle;
            this.state = state;
            setCenter(checkBox);
            setPadding(new Insets(5, 5, 5, 5));
        }

    }

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
