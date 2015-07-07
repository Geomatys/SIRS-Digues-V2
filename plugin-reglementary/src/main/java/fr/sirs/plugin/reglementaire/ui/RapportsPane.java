
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsStringConverter;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

/**
 * 
 *
 * @author Johann Sorel (Geomatys)
 */
public class RapportsPane extends BorderPane implements Initializable {

    @FXML private ComboBox<Preview> uiSystemEndiguement;
    @FXML private ListView<TronconDigue> uiTroncons;
    private Spinner<Double> uiPrDebut;
    @FXML private DatePicker uiPeriodeFin;
    private Spinner<Double> uiPrFin;
    @FXML private DatePicker uiPeriodeDebut;
    @FXML private CheckBox uiCrerEntreeCalendrier;
    @FXML private TextField uiTitre;
    @FXML private BorderPane uiTablePane;
    @FXML private GridPane uiGrid;
    @FXML private ComboBox<Preview> uiType;

    private PojoTable uiTable;

    public RapportsPane() {
        SIRS.loadFXML(this, RapportsPane.class);
        Injector.injectDependencies(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final Session session = Injector.getSession();
        final Previews previewRepository = session.getPreviews();

        uiTable = new RapportsTable();
        uiTablePane.setCenter(uiTable);
        uiPrDebut = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE,0.0));
        uiPrFin = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE,0.0));
        uiGrid.add(uiPrDebut, 1, 3);
        uiGrid.add(uiPrFin, 3, 3);

        final LocalDate date = LocalDate.now();
        uiPeriodeDebut.valueProperty().set(date);
        uiPeriodeFin.valueProperty().set(date);

        uiSystemEndiguement.setEditable(false);
        uiSystemEndiguement.valueProperty().addListener(this::systemeEndiguementChange);
        uiTroncons.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiTroncons.getSelectionModel().getSelectedItems().addListener(this::tronconSelectionChange);
        final SirsStringConverter converter = new SirsStringConverter();
        uiTroncons.setCellFactory(new Callback<ListView<TronconDigue>, ListCell<TronconDigue>>() {
            @Override
            public ListCell<TronconDigue> call(ListView<TronconDigue> param) {
                return new ListCell(){
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(converter.toString(item));
                    }
                };
            }
        });

        uiSystemEndiguement.setConverter(new SirsStringConverter());
        uiSystemEndiguement.setItems(FXCollections.observableArrayList(
            previewRepository.getByClass(SystemeEndiguement.class)));
        if(uiSystemEndiguement.getItems()!=null){
            uiSystemEndiguement.getSelectionModel().select(0);
        }

        SIRS.initCombo(uiType, FXCollections.observableArrayList(
            previewRepository.getByClass(RefTypeObligationReglementaire.class)), null);
        if(uiType.getItems()!=null){
            uiType.getSelectionModel().select(0);
        }
        uiType.setEditable(false);

    }

    private void systemeEndiguementChange(ObservableValue<? extends Preview> observable,
                Preview oldValue, Preview newValue) {
        if(newValue==null){
            uiTroncons.setItems(FXCollections.emptyObservableList());
        }else{
            final Session session = Injector.getSession();
            final SystemeEndiguementRepository sdRepo = (SystemeEndiguementRepository) session.getRepositoryForClass(SystemeEndiguement.class);
            final DigueRepository digueRepo = (DigueRepository) session.getRepositoryForClass(Digue.class);
            final TronconDigueRepository tronconRepo = (TronconDigueRepository) session.getRepositoryForClass(TronconDigue.class);
            final SystemeEndiguement sd = sdRepo.get(newValue.getElementId());

            final Set<TronconDigue> troncons = new HashSet<>();
            for(Digue digue : digueRepo.get(sd.getDigueIds())){
                troncons.addAll(tronconRepo.getByDigue(digue));
            }
            uiTroncons.setItems(FXCollections.observableArrayList(troncons));
        }
    }

    private void tronconSelectionChange(ListChangeListener.Change<? extends TronconDigue> c){
        final Session session = Injector.getSession();

        final ObservableList<TronconDigue> selectedItems = uiTroncons.getSelectionModel().getSelectedItems();
        if(selectedItems.size()==1){
            uiPrDebut.setDisable(false);
            uiPrFin.setDisable(false);
        }else{
            uiPrDebut.setDisable(true);
            uiPrFin.setDisable(true);
        }
    }

    @FXML
    private void generateReport(ActionEvent event) {
        final Session session = Injector.getSession();

        final LocalDate dateDebut = uiPeriodeDebut.getValue();
        final LocalDate dateFin = uiPeriodeFin.getValue();
        final Preview type = uiType.valueProperty().get();
        final Preview sysEndi = uiSystemEndiguement.valueProperty().get();
        final String titre = uiTitre.getText();
//        final ObservableList<Digue> selectedItems = uiTroncons.getSelectionModel().getSelectedItems();
//        if(selectedItems.size()!=1){
//
//        }

        if(uiCrerEntreeCalendrier.isSelected()){
            //on crée une obligation à la date d'aujourdhui
            final ObligationReglementaireRepository rep = (ObligationReglementaireRepository)session.getRepositoryForClass(ObligationReglementaire.class);
            final ObligationReglementaire obligation = rep.create();
            final LocalDate date = LocalDate.now();
            obligation.setAnnee(date.getYear());
            obligation.setDateRealisation(date);
            obligation.setLibelle(titre);
            if(sysEndi!=null) obligation.setSystemeEndiguementId(sysEndi.getElementId());
            if(type!=null) obligation.setTypeId(type.getElementId());
            rep.add(obligation);
        }

    }

}
