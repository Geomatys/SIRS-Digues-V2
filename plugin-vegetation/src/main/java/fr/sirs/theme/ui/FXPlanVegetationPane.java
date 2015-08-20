
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
import java.util.ArrayList;
import java.util.List;
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
    private final ObservableList<TraitementSummary> traitements = FXCollections.observableArrayList();

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

        initTraitements(null);
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
        uiCoutTable = new TableView<>((ObservableList) plan.paramCout);
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
        uiVBox.getChildren().addAll(uiCoutsTableHeader, uiCoutTable, uiTraitementsTableHeader, uiTraitementsTable);
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
        final List<TraitementSummary> traitementsAvecCout = new ArrayList<>();
        final List<ParamCoutTraitementVegetation> coutAvecTraitement = new ArrayList<>();
        for(final ParamCoutTraitementVegetation param : plan.getParamCout()){
            final TraitementSummary paramTraitementStub = TraitementSummary.toSummary(param);
            for(final TraitementSummary traitement : traitements){
                if(paramTraitementStub.equalsTraitementSummary(traitement)){
                    traitementsAvecCout.add(traitement);
                    coutAvecTraitement.add(param);
                }
            }
        }

        /*
        On retranche des traitements, ceux qui ont un coût déterminé afin d'avoir une liste des traitements sans coût.
        */
        final List<TraitementSummary> traitementsSansCout = new ArrayList<>(traitements);
        traitementsSansCout.removeAll(traitementsAvecCout);
        
        /*
        On retranche de la liste des coûts ceux qui n'ont pas de traitement associé.
        */
        final List<ParamCoutTraitementVegetation> coutSansTraitement = new ArrayList<>(plan.getParamCout());
        coutSansTraitement.removeAll(coutAvecTraitement);

        // On commence par retrancher de la liste des paramètres ceux qui ne servent plus à rien
        plan.getParamCout().removeAll(coutSansTraitement);

        // Puis on crée des paramètres pour les traitements qui n'ont pas de coût
        for(final TraitementSummary traitement : traitementsSansCout){
            final ParamCoutTraitementVegetation param = session.getElementCreator().createElement(ParamCoutTraitementVegetation.class);
            param.getId();// On affecte un Id à l'élément imbriqué
            param.setType(traitement.typeTraitementId.get());
            param.setSousType(traitement.typeSousTraitementId.get());
            plan.getParamCout().add(param);
        }

        planRepo.update(plan);
    }

    /**
     * Méthode d'initialisation des traitements.
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
        traitements.remove(0, traitements.size());
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
    }

    /**
     * Utility class to represent different kinds of treatments.
     */
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

            if (!Objects.equals(this.typeTraitementId.get(), obj.typeTraitementId.get())) {
                return false;
            }
            if (!Objects.equals(this.typeSousTraitementId.get(), obj.typeSousTraitementId.get())) {
                return false;
            }
            return true;
        }

        /**
         * Construit une ébauche de TraitementSummary à l'aide des informations
         * présentens dans le ParamCoutTraitementVegetation donné en paramètres.
         *
         * Cette opération est réalisée à des fins de simple comparaison de manière
         * à évaluer si un ParamCoutTraitementVegetation prend en charge un
         * Traitement summary (c'est-à-dire correspond à son type et sous-type de
         * traitement).
         *
         * @param param
         * @return
         */
        private static TraitementSummary toSummary(final ParamCoutTraitementVegetation param){
            return new TraitementSummary(null, param.getType(), param.getSousType(), null, true);
        }
    }

}
