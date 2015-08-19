
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.ParamCoutTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.TraitementZoneVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDate;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.FXNumberCell2;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanVegetationPane extends BorderPane {
    
    @FXML private TextField uiPlanName;
    @FXML private TextField uiDesignation;
    @FXML private Spinner uiPlanDebut;
    @FXML private Spinner uiPlanFin;
    @FXML private VBox uiVBox;
    
    private TableView<TraitementSummary> uiTraitementsTable;
    
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
                        } else {
                            try{
                                super.setText(converter.toString(previews.get(item)));
                            } catch (Exception e){
                                super.setText(item);
                            }
                        }
                        super.setGraphic(null);
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
                        } else {
                            try{
                                super.setText(converter.toString(previews.get(item)));
                            } catch (Exception e){
                                super.setText(item);
                            }
                        }
                        super.setGraphic(null);
                    }
                };
            };

    private final PlanVegetation plan;

    public FXPlanVegetationPane(PlanVegetation plan) {
        SIRS.loadFXML(this, FXPlanVegetationPane.class);
        this.plan = plan;
        
        uiDesignation.textProperty().bindBidirectional(plan.designationProperty());
        uiPlanName.textProperty().bindBidirectional(plan.libelleProperty());
        uiPlanDebut.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, LocalDate.now().getYear()));
        uiPlanDebut.setEditable(true);
        uiPlanDebut.getValueFactory().valueProperty().bindBidirectional(plan.anneDebutProperty());
        uiPlanFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, LocalDate.now().getYear()+10));
        uiPlanFin.setEditable(true);
        uiPlanFin.getValueFactory().valueProperty().bindBidirectional(plan.anneFinProperty());
        
        uiSave.setOnAction((ActionEvent event) -> {
            planRepo.update(FXPlanVegetationPane.this.plan);
        });
        


        ////////////////////////////////////////////////////////////////////////
        // Construction des résumés des traitements planifiés sur les zones.
        ////////////////////////////////////////////////////////////////////////

        /*
        On commence par parcourir les zones de végétation de toutes les
        parcelles du plan.

        Pour chaque zone, on récupère le traitement qui lui est associé.

        |-> S'il n'y a pas de traitement associé, ou si le traitement est hors
            gestion, on passe à la zone suivante.

        |-> Avec ce traitement, s'il exite et doit être pris en compte dans la
            gestion, on construit deux "résumés de traitement", un pour le
            traitement ponctuel et un pour le traitemnet non ponctuel,
            mémorisant le type de la zone de végétation, le type de traitement,
            le type de sous-traitement et un booleen indiquant la ponctualité
            du traitement. En plus, les traitements non ponctuels mémorisent la
            fréquence de traitement.
            [Note: on a besoin du booleen indiquant la ponctualité du traitement
            car la nullité de la fréquence n'est pas suffisante (on pourrait
            avoir une fréquence nulle pour un traitement non ponctuel si celle-
            -ci n'avait pas été initializée). On serait alors contraint de faire
            une requête sur la liste de référence du type de traitement afin de
            vérifier s'il est ponctuel ou non.]
            On vérifie ensuite que les résumés de traitement ne sont pas déjà
            dans la liste des traitements avant de les y ajouter.

            À l'issue de cette étape on a donc une liste des traitements
            recensés dans le plan de gestion.
        */
        final ObservableList<TraitementSummary> traitements = FXCollections.observableArrayList();
        for(final ParcelleVegetation parcelle : parcelleRepo.getByPlan(plan)){
            ObservableList<? extends ZoneVegetation> allZoneVegetationByParcelleId = AbstractZoneVegetationRepository.getAllZoneVegetationByParcelleId(parcelle.getId(), session);
            for(final ZoneVegetation zone : allZoneVegetationByParcelleId){
                final TraitementZoneVegetation traitement = zone.getTraitement();
                if(traitement!=null && !traitement.getHorsGestion()){
                    final TraitementSummary summaryNonPonctuel = new TraitementSummary(zone.getClass(), traitement.getTypeTraitementId(), traitement.getSousTypeTraitementId(), traitement.getFrequenceId(), false);
                    final TraitementSummary summaryPonctuel = new TraitementSummary(zone.getClass(), traitement.getTypeTraitementPonctuelId(), traitement.getSousTypeTraitementPonctuelId(), null, true);
                    if(traitement.getTypeTraitementId()!=null && !traitements.contains(summaryNonPonctuel)) traitements.add(summaryNonPonctuel);
                    if(traitement.getTypeTraitementPonctuelId()!=null && !traitements.contains(summaryPonctuel)) traitements.add(summaryPonctuel);
                }
            }
        }

        uiTraitementsTable = new TableView<>(traitements);

        final TableColumn<TraitementSummary, Class<? extends ZoneVegetation>> vegetationColumn = new TableColumn<>("Type de zone");
        vegetationColumn.setCellValueFactory((TableColumn.CellDataFeatures<TraitementSummary, Class<? extends ZoneVegetation>> param) -> param.getValue().typeVegetationClass);
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
        typeTraitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<TraitementSummary, String> param) -> param.getValue().typeTraitementId);
        typeTraitementColumn.setCellFactory(fromIdCellFactory);
        final TableColumn<TraitementSummary, String> typeSousTraitementColumn = new TableColumn<>("Sous-type de traitement");
        typeSousTraitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<TraitementSummary, String> param) -> param.getValue().typeSousTraitementId);
        typeSousTraitementColumn.setCellFactory(fromIdCellFactory);
        final TableColumn<TraitementSummary, String> frequenceTraitementColumn = new TableColumn<>("Fréquence de traitement");
        frequenceTraitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<TraitementSummary, String> param) -> {
            final TraitementSummary sum = param.getValue();
            if(sum.ponctuel.get())
                return new SimpleStringProperty("Ponctuel");
            else
                return sum.typeFrequenceId;
                });
        frequenceTraitementColumn.setCellFactory(fromIdCellFactory);

        uiTraitementsTable.getColumns().addAll(vegetationColumn, typeTraitementColumn, typeSousTraitementColumn, frequenceTraitementColumn);

     

        ////////////////////////////////////////////////////////////////////////
        // Construction des paramètes de coûts.
        ////////////////////////////////////////////////////////////////////////
        uiCoutTable = new TableView<>(FXCollections.observableList(plan.paramCout));
        uiCoutTable.setEditable(true);
        final TableColumn<ParamCoutTraitementVegetation, String> traitementColumn = new TableColumn<>("Type de traitement");
        traitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<ParamCoutTraitementVegetation, String> param) -> param.getValue().typeProperty());
        traitementColumn.setCellFactory(fromIdCellFactory2);
        traitementColumn.setEditable(false);
        final TableColumn<ParamCoutTraitementVegetation, String> sousTraitementColumn = new TableColumn<>("Sous-type de traitement");
        sousTraitementColumn.setCellValueFactory((TableColumn.CellDataFeatures<ParamCoutTraitementVegetation, String> param) -> param.getValue().sousTypeProperty());
        sousTraitementColumn.setCellFactory(fromIdCellFactory2);
        sousTraitementColumn.setEditable(false);
        final TableColumn<ParamCoutTraitementVegetation, Number> coutColumn = new TableColumn<>("Coût");
        coutColumn.setCellValueFactory((TableColumn.CellDataFeatures<ParamCoutTraitementVegetation, Number> param) -> param.getValue().coutProperty());
        coutColumn.setCellFactory((TableColumn<ParamCoutTraitementVegetation, Number> param) -> new FXNumberCell2.FXDoubleCell<>(0.));
        uiCoutTable.getColumns().addAll(traitementColumn, sousTraitementColumn, coutColumn);



        uiVBox.getChildren().addAll(uiCoutTable, uiTraitementsTable);

    }



    private static class TraitementSummary {
        private final ObjectProperty<Class<? extends ZoneVegetation>> typeVegetationClass = new SimpleObjectProperty<>();
        private final StringProperty typeTraitementId = new SimpleStringProperty();
        private final StringProperty typeSousTraitementId = new SimpleStringProperty();
        private final StringProperty typeFrequenceId = new SimpleStringProperty();
        private final BooleanProperty ponctuel = new SimpleBooleanProperty();

        public TraitementSummary(final Class<? extends ZoneVegetation> typeVegetationClass,
                final String typeTraitementId, final String typeSousTraitementId,
                final String typeFrequenceId, final boolean ponctuel){
            this.typeVegetationClass.set(typeVegetationClass);
            this.typeTraitementId.set(typeTraitementId);
            this.typeSousTraitementId.set(typeSousTraitementId);
            this.typeFrequenceId.set(typeFrequenceId);
            this.ponctuel.set(ponctuel);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.typeVegetationClass);
            hash = 83 * hash + Objects.hashCode(this.typeTraitementId);
            hash = 83 * hash + Objects.hashCode(this.typeSousTraitementId);
            hash = 83 * hash + Objects.hashCode(this.typeFrequenceId);
            hash = 83 * hash + Objects.hashCode(this.ponctuel);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TraitementSummary other = (TraitementSummary) obj;
            if (!Objects.equals(this.typeVegetationClass, other.typeVegetationClass)) {
                return false;
            }
            if (!Objects.equals(this.typeTraitementId, other.typeTraitementId)) {
                return false;
            }
            if (!Objects.equals(this.typeSousTraitementId, other.typeSousTraitementId)) {
                return false;
            }
            if (!Objects.equals(this.typeFrequenceId, other.typeFrequenceId)) {
                return false;
            }
            if (!Objects.equals(this.ponctuel, other.ponctuel)) {
                return false;
            }
            return true;
        }

        public boolean equalsTraitementSummary(TraitementSummary obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            if (!Objects.equals(this.typeTraitementId, obj.typeTraitementId)) {
                return false;
            }
            if (!Objects.equals(this.typeSousTraitementId, obj.typeSousTraitementId)) {
                return false;
            }
            return true;
        }

    }

}
