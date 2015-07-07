
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.h2.H2Helper;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RapportModeleObligationReglementaire;
import fr.sirs.core.model.RapportSectionObligationReglementaire;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.core.model.SQLQuery;
import fr.sirs.core.model.SectionType;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.reglementaire.ODTUtils;
import fr.sirs.theme.ColumnOrder;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsStringConverter;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.sis.measure.NumberRange;
import org.ektorp.DocumentNotFoundException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.db.h2.H2FeatureStore;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.NamesExt;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.util.FileUtilities;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;

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
    @FXML private Button uiGenerate;

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
        uiPeriodeDebut.valueProperty().set(date.minus(10, ChronoUnit.YEARS));
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

        uiGenerate.disableProperty().bind(uiTable.getUiTable().getSelectionModel().selectedItemProperty().isNull());

        // Pour mettre a jour l'etat actif des boutons
        tronconSelectionChange(null);
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

        final RapportModeleObligationReglementaire report = (RapportModeleObligationReglementaire)
                uiTable.getUiTable().getSelectionModel().getSelectedItem();
        
        final FileChooser chooser = new FileChooser();
        final File file = chooser.showSaveDialog(null);
        if(file==null) return;

        final Session session = Injector.getSession();

        final LocalDate dateDebut = uiPeriodeDebut.getValue();
        final LocalDate dateFin = uiPeriodeFin.getValue();
        final Preview type = uiType.valueProperty().get();
        final Preview sysEndi = uiSystemEndiguement.valueProperty().get();
        final String titre = uiTitre.getText();
        final Double prDebut = uiPrFin.getValue();
        final Double prFin = uiPrFin.getValue();
        final NumberRange range = NumberRange.create(prDebut, true, prFin, true);

        // on liste tous les elements a générer
        final ObservableList<TronconDigue> troncons = uiTroncons.getSelectionModel().getSelectedItems();
        final Map<String,Objet> elements = new LinkedHashMap<>();
        for(TronconDigue troncon : troncons){
            if(troncon==null) continue;
            if(prDebut == 0.0 && prFin == 0.0){
                for(Objet obj : TronconUtils.getObjetList(troncon.getDocumentId())){
                    elements.put(obj.getDocumentId(), obj);
                }
            }else{
                //on verifie la position
                for(Objet obj : TronconUtils.getObjetList(troncon.getDocumentId())){
                    if(range.intersectsAny(NumberRange.create(obj.getPrDebut(), true, obj.getPrFin(), true))){
                        elements.put(obj.getDocumentId(), obj);
                    }
                }
            }
        }

        // on crée un rapport pour chaque section
        final File folder = new File(file.getParentFile(),"temp_"+file.getName().split("\\.")[0]);
        folder.mkdirs();
        final List<File> files = new ArrayList<>();
        final AtomicInteger inc = new AtomicInteger();
        try{
            for(RapportSectionObligationReglementaire section : report.section){
                if(SectionType.TABLE.equals(section.getType())){
                    files.addAll(generateTable(section, elements, folder, inc));
                }else if(SectionType.FORM.equals(section.getType())){
                    
                }
                
            }

            // on aggrege le tout
            ODTUtils.concatenateFiles(file, files.toArray(new File[0]));
        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            GeotkFX.newExceptionDialog("Une erreur est survenue lors de la génération du rapport.", ex).show();
            return;
        }finally{
            FileUtilities.deleteDirectory(folder);
        }

        if(uiCrerEntreeCalendrier.isSelected()){
            //on crée une obligation à la date d'aujourdhui
            final ObligationReglementaireRepository rep = (ObligationReglementaireRepository)session.getRepositoryForClass(ObligationReglementaire.class);
            final ObligationReglementaire obligation = rep.create();
            final LocalDateTime date = LocalDateTime.now();
            obligation.setAnnee(date.getYear());
            obligation.setDateRealisation(date);
            obligation.setLibelle(titre);
            if(sysEndi!=null) obligation.setSystemeEndiguementId(sysEndi.getElementId());
            if(type!=null) obligation.setTypeId(type.getElementId());
            rep.add(obligation);
        }

    }

    private List<File> generateTable(RapportSectionObligationReglementaire section, 
            Map<String,Objet> elements, File tempFolder, AtomicInteger inc) throws Exception{
        final TextDocument doc = TextDocument.newTextDocument();

        final File docFile = new File(tempFolder, inc.incrementAndGet()+".odt");

        final String titre = section.getLibelle();
        final String requeteId = section.getRequeteId();

        //creation de la requete
        final Session session = Injector.getSession();
        final SQLQuery sqlQuery = session.getSqlQueryRepository().get(requeteId);        
        final Query fsquery = org.geotoolkit.data.query.QueryBuilder.language(
                JDBCFeatureStore.CUSTOM_SQL, sqlQuery.getSql(), NamesExt.create("requete"));
         
        //recupération de la base H2
        final FeatureStore h2Store = (H2FeatureStore) H2Helper.getStore(session.getConnector());
        final FeatureCollection col = h2Store.createSession(false).getFeatureCollection(fsquery);
        final String firstProperty = col.getFeatureType().getDescriptors().iterator().next().getName().tip().toString();

        //on filtre les elements
        final List<Element> validElements = new ArrayList<>();
        try (FeatureIterator iterator = col.iterator()) {
            while(iterator.hasNext()){
                final Feature feature = iterator.next();
                final Object val = feature.getPropertyValue(firstProperty);
                final Objet ele = elements.get(val);
                if(ele!=null) validElements.add(ele);
            }
        }
        if(validElements.isEmpty()){
            return Collections.EMPTY_LIST;
        }

        //liste des champs
        final Class pojoClass = validElements.get(0).getClass();
        final LabelMapper labelMapper = new LabelMapper(pojoClass);
        final Map<String,Printer> cols = new TreeMap<>(ColumnOrder.createComparator(pojoClass.getSimpleName()));

        try {
            //contruction des colonnes editable
            final HashMap<String, PropertyDescriptor> properties = SIRS.listSimpleProperties(pojoClass);
            // On enlève les propriétés inutiles pour l'utilisateur
            for (final String key : PojoTable.COLUMNS_TO_IGNORE) {
                properties.remove(key);
            }
            for(Entry<String,PropertyDescriptor> entry : properties.entrySet()){
                cols.put(entry.getKey(),entry.getValue().getReadMethod()::invoke);
            }

            // On donne toutes les informations de position.
            if (Positionable.class.isAssignableFrom(pojoClass)) {
                final HashMap<String, PropertyDescriptor> positionable = SIRS.listSimpleProperties(Positionable.class);
                for(Entry<String,PropertyDescriptor> entry : positionable.entrySet()){
                    cols.put(entry.getKey(),entry.getValue().getReadMethod()::invoke);
                }
            }

        } catch (IntrospectionException ex) {
            SIRS.LOGGER.log(Level.WARNING, "property columns cannot be created.", ex);
        }


        //titre
        doc.addParagraph(titre);

        //table
        final Table table = doc.addTable(validElements.size(), cols.size());
        //header
         int colIndex = 0;
        for(Entry<String,Printer> entry : cols.entrySet()){
            final Cell cell = table.getCellByPosition(colIndex, 0);
            cell.setStringValue(labelMapper.mapPropertyName(entry.getKey()));
            colIndex++;
        }
        //cells
        final Previews previews = session.getPreviews();
        final SirsStringConverter cvt = new SirsStringConverter();
        for(int i=0,n=validElements.size();i<n;i++){
            final Element element = validElements.get(i);
            colIndex = 0;
            for(Entry<String,Printer> entry : cols.entrySet()){
                Object obj = entry.getValue().print(element);
                if(obj!=null){
                    final Cell cell = table.getCellByPosition(colIndex, i+1);
                    if(obj instanceof String){
                        try{
                            obj = cvt.toString(previews.get(titre));
                        }catch(DocumentNotFoundException ex){/**pas important*/}

                    }
                    cell.setStringValue(String.valueOf(obj));
                }
                colIndex++;
            }
        }

        doc.save(docFile);
        return Collections.singletonList(docFile);
    }

    private static interface Printer{

        public Object print(Object candidate) throws Exception;
    }

}
