
package fr.sirs.plugin.reglementaire.ui;

import com.vividsolutions.jts.geom.LineString;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.AUTHOR_FIELD;
import static fr.sirs.SIRS.COMMENTAIRE_FIELD;
import static fr.sirs.SIRS.DATE_MAJ_FIELD;
import static fr.sirs.SIRS.FOREIGN_PARENT_ID_FIELD;
import static fr.sirs.SIRS.LATITUDE_MAX_FIELD;
import static fr.sirs.SIRS.LATITUDE_MIN_FIELD;
import static fr.sirs.SIRS.LONGITUDE_MAX_FIELD;
import static fr.sirs.SIRS.LONGITUDE_MIN_FIELD;
import static fr.sirs.SIRS.VALID_FIELD;
import fr.sirs.Session;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.AbstractTronconDigueRepository;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.EtapeObligationReglementaireRepository;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.component.ObligationReglementaireRepository2;
import fr.sirs.core.component.Previews;
import fr.sirs.core.component.SQLQueryRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.h2.H2Helper;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetPhotographiable;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.PhotoChoiceObligationReglementaire;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RapportModeleObligationReglementaire;
import fr.sirs.core.model.RapportSectionObligationReglementaire;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.core.model.SQLQuery;
import fr.sirs.core.model.SectionTypeObligationReglementaire;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TemplateOdt;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.reglementaire.ODTUtils;
import fr.sirs.theme.ColumnOrder;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsStringConverter;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import net.sf.jooreports.templates.DocumentTemplate;
import net.sf.jooreports.templates.DocumentTemplateFactory;
import org.apache.sis.measure.NumberRange;
import org.apache.sis.storage.DataStoreException;
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
import org.geotoolkit.referencing.LinearReferencing;
import org.geotoolkit.util.FileUtilities;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;

/**
 *
 *
 * @author Johann Sorel (Geomatys)
 */
public class RapportsPane extends BorderPane implements Initializable {

    public static final String[] COLUMNS_TO_IGNORE = new String[] {
        AUTHOR_FIELD, VALID_FIELD, FOREIGN_PARENT_ID_FIELD, LONGITUDE_MIN_FIELD,
        LONGITUDE_MAX_FIELD, LATITUDE_MIN_FIELD, LATITUDE_MAX_FIELD,
        DATE_MAJ_FIELD, COMMENTAIRE_FIELD,
        "prDebut", "prFin", "valid", "positionDebut", "positionFin", "epaisseur"};
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
    @FXML private ProgressIndicator uiProgress;
    @FXML private Label uiProgressLabel;

    private PojoTable uiTable;
    private final BooleanProperty running = new SimpleBooleanProperty(false);
    private Session session;

    public RapportsPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        session = Injector.getSession();
        final Previews previewRepository = session.getPreviews();

        uiTable = new RapportsTable();
        uiTablePane.setCenter(uiTable);
        uiPrDebut = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE,0.0));
        uiPrDebut.setEditable(true);
        uiPrFin = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE,0.0));
        uiPrFin.setEditable(true);
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
                return new ListCell() {
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

        uiGenerate.disableProperty().bind(
                Bindings.or(running, uiTable.getUiTable().getSelectionModel().selectedItemProperty().isNull()));

        // Pour mettre a jour l'etat actif des boutons
        tronconSelectionChange(null);
    }

    private void systemeEndiguementChange(ObservableValue<? extends Preview> observable,
                Preview oldValue, Preview newValue) {
        if(newValue==null){
            uiTroncons.setItems(FXCollections.emptyObservableList());
        }else{
            final SystemeEndiguementRepository sdRepo = (SystemeEndiguementRepository) session.getRepositoryForClass(SystemeEndiguement.class);
            final DigueRepository digueRepo = (DigueRepository) session.getRepositoryForClass(Digue.class);
            final AbstractTronconDigueRepository tronconRepo = (AbstractTronconDigueRepository) session.getRepositoryForClass(TronconDigue.class);
            final SystemeEndiguement sd = sdRepo.get(newValue.getElementId());
            final Set<TronconDigue> troncons = new HashSet<>();
            final List<Digue> digues = digueRepo.getBySystemeEndiguement(sd);
            for(Digue digue : digues){
                troncons.addAll(tronconRepo.getByDigue(digue));
            }
            uiTroncons.setItems(FXCollections.observableArrayList(troncons));
        }
    }

    private void tronconSelectionChange(ListChangeListener.Change<? extends TronconDigue> c){
        final ObservableList<TronconDigue> selectedItems = uiTroncons.getSelectionModel().getSelectedItems();
        if (selectedItems.size() == 1) {
            final TronconDigue troncon = selectedItems.get(0);
            final LineString lineTroncon = LinearReferencing.asLineString(troncon.getGeometry());
            final LinearReferencing.SegmentInfo[] segments = LinearReferencingUtilities.buildSegments(lineTroncon);
            final SystemeReperage sr = Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(troncon.getSystemeRepDefautId());
            final BorneDigueRepository borneRepo = InjectorCore.getBean(BorneDigueRepository.class);

            final float prDebVal = TronconUtils.computePR(segments, sr, lineTroncon.getStartPoint(), borneRepo);
            uiPrDebut.getValueFactory().setValue(new BigDecimal(prDebVal).doubleValue());
            final float prFinVal = TronconUtils.computePR(segments, sr, lineTroncon.getEndPoint(), borneRepo);
            uiPrFin.getValueFactory().setValue(new BigDecimal(prFinVal).doubleValue());

            uiPrDebut.setDisable(false);
            uiPrFin.setDisable(false);
        }else{
            uiPrDebut.getValueFactory().setValue(0d);
            uiPrFin.getValueFactory().setValue(0d);
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

        running.set(true);
        uiProgressLabel.setText("");
        uiProgressLabel.setVisible(true);
        uiProgress.setVisible(true);

        new Thread(){
            @Override
            public void run() {

                try{
                    final long dateDebut = uiPeriodeDebut.getValue().atTime(0,0,0).toInstant(ZoneOffset.UTC).toEpochMilli();
                    final long dateFin = uiPeriodeFin.getValue().atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli();
                    final NumberRange dateRange = NumberRange.create(dateDebut, true, dateFin, true);
                    final Preview type = uiType.valueProperty().get();
                    final Preview sysEndi = uiSystemEndiguement.valueProperty().get();
                    final String titre = uiTitre.getText();
                    final Double prDebut = uiPrFin.getValue();
                    final Double prFin = uiPrFin.getValue();
                    final NumberRange prRange = NumberRange.create(prDebut, true, prFin, true);
                    
                    // on liste tous les elements a générer
                    Platform.runLater(()->uiProgressLabel.setText("Recherche des objets du rapport..."));
                    final ObservableList<TronconDigue> troncons = uiTroncons.getSelectionModel().getSelectedItems();
                    final Map<String,Objet> elements = new LinkedHashMap<>();
                    for(TronconDigue troncon : troncons){
                        if(troncon==null) continue;

                        final List<Objet> objetList = TronconUtils.getObjetList(troncon.getDocumentId());

                        for(Objet obj : objetList){
                            //on verifie la position
                            if(!(prDebut == 0.0 && prFin == 0.0)){
                                if(!prRange.intersectsAny(NumberRange.create(obj.getPrDebut(), true, obj.getPrFin(), true))){
                                    continue;
                                }
                            }
                            //on vérifie la date
                            final LocalDate objDateDebut = obj.getDate_debut();
                            final LocalDate objDateFin = obj.getDate_fin();
                            final long debut = objDateDebut==null ? 0 : objDateDebut.atTime(0,0,0).toInstant(ZoneOffset.UTC).toEpochMilli();
                            final long fin = objDateFin==null ? Long.MAX_VALUE : objDateFin.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli();
                            final NumberRange objDateRange = NumberRange.create(debut, true, fin, true);
                            if(!dateRange.intersectsAny(objDateRange)){
                                continue;
                            }

                            elements.put(obj.getDocumentId(), obj);
                        }
                    }

                    final List parts = new ArrayList();
                    // on crée un document qui contient le titre
                    try{
                        final TextDocument headerDoc = TextDocument.newTextDocument();
                        final Paragraph paragraph = headerDoc.addParagraph(titre);
                        paragraph.setFont(new Font("Serial", StyleTypeDefinitions.FontStyle.BOLD, 20));
                        parts.add(headerDoc);
                    }catch(Exception ex){
                        SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                        GeotkFX.newExceptionDialog("Une erreur est survenue lors de la génération du rapport.", ex).show();
                        return;
                    }

                    // on crée un rapport pour chaque section
                    final File folder = new File(file.getParentFile(),"temp_"+file.getName().split("\\.")[0]);
                    folder.mkdirs();
                    final AtomicInteger inc = new AtomicInteger();
                    try{
                        for(RapportSectionObligationReglementaire section : report.section){
                            Platform.runLater(()->uiProgressLabel.setText("Génération de la section : "+section.getLibelle()));
                            if(SectionTypeObligationReglementaire.TABLE.equals(section.getType())){
                                parts.addAll(generateTable(section, elements, folder, inc));
                            }else if(SectionTypeObligationReglementaire.FICHE.equals(section.getType())){
                                parts.addAll(generateFiches(section, elements, folder, inc));
                            }
                        }

                        // on aggrege le tout
                        Platform.runLater(()->uiProgressLabel.setText("Aggrégation des sections"));
                        final TextDocument aggDoc = TextDocument.newTextDocument();
                        for(int i=0,n=parts.size();i<n;i++){
                            final int I = i;
                            Platform.runLater(()->uiProgressLabel.setText("Aggrégation des sections "+I+"/"+n));
                            ODTUtils.concatenateFile(aggDoc, parts.get(i));
                        }
                        aggDoc.save(file);
                    }catch(Exception ex){
                        SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                        Platform.runLater(()->GeotkFX.newExceptionDialog("Une erreur est survenue lors de la génération du rapport.", ex).show());
                        return;
                    }finally{
                        FileUtilities.deleteDirectory(folder);
                    }

                    if(uiCrerEntreeCalendrier.isSelected()){
                        Platform.runLater(()->uiProgressLabel.setText("Ajout de l'événement dans le calendrier"));
                        //on crée une obligation à la date d'aujourdhui
                        final ObligationReglementaireRepository rep = (ObligationReglementaireRepository)session.getRepositoryForClass(ObligationReglementaire.class);
                        final EtapeObligationReglementaireRepository eorr = Injector.getBean(EtapeObligationReglementaireRepository.class);
                        final ObligationReglementaire obligation = rep.create();
                        final LocalDate date = LocalDate.now();
                        obligation.setAnnee(date.getYear());
                        //obligation.setDateRealisation(date);
                        obligation.setLibelle(titre);
                        if(sysEndi!=null) obligation.setSystemeEndiguementId(sysEndi.getElementId());
                        if(type!=null) obligation.setTypeId(type.getElementId());
                        rep.add(obligation);

                        final EtapeObligationReglementaire etape = eorr.create();
                        etape.setDateRealisation(date);
                        etape.setObligationReglementaireId(obligation.getId());
                        eorr.add(etape);
                    }

                    Platform.runLater(()->uiProgressLabel.setText("Génération terminée"));
                    try {sleep(2000);} catch (InterruptedException ex) {}

                }finally{
                    Platform.runLater(()->{
                        uiProgressLabel.setText("");
                        uiProgressLabel.setVisible(false);
                        uiProgress.setVisible(false);
                        running.set(false);
                    });
                }
            
        }}.start();
        

    }

    private List<TextDocument> generateTable(RapportSectionObligationReglementaire section,
            Map<String,Objet> elements, File tempFolder, AtomicInteger inc) throws Exception{
        final TextDocument doc = TextDocument.newTextDocument();

        //landscape mode: bug
//        final Paragraph para = doc.addParagraph("");
//        final MasterPage masterPage = ODTUtils.createMasterPage(doc, false, 5);
//        doc.addPageBreak(para, masterPage);


        //on recupere les elements qui correspondent a la requete
        final List<Element> validElements = listValidElements(section, elements);
        if(validElements.isEmpty()){
            return Collections.EMPTY_LIST;
        }

        //liste des champs
        final Class pojoClass = validElements.get(0).getClass();
        final LabelMapper labelMapper = LabelMapper.get(pojoClass);
        final Map<String,Printer> cols = new TreeMap<>(ColumnOrder.createComparator(pojoClass.getSimpleName()));

        try {
            //contruction des colonnes editable
            final HashMap<String, PropertyDescriptor> properties = SIRS.listSimpleProperties(pojoClass);
            for(Entry<String,PropertyDescriptor> entry : properties.entrySet()){
                cols.put(entry.getKey(),entry.getValue().getReadMethod()::invoke);
            }

            // On donne toutes les informations de position.
            if (Positionable.class.isAssignableFrom(pojoClass)) {
                final HashMap<String, PropertyDescriptor> positionable = SIRS.listSimpleProperties(Positionable.class);
                positionable.remove("systemeRepId");
                for(String key : positionable.keySet()){
                    cols.remove(key);
                }

                cols.put("borneDebutId", new BorneDebutPrinter());
                cols.put("borneFinId", new BorneFinPrinter());
            }

            // On enlève les propriétés inutiles pour l'utilisateur
            for (final String key : COLUMNS_TO_IGNORE) {
                cols.remove(key);
            }
        } catch (IntrospectionException ex) {
            SIRS.LOGGER.log(Level.WARNING, "property columns cannot be created.", ex);
        }

        //titre
        final String titre = section.getLibelle();
        final Paragraph paragraph = doc.addParagraph(titre);
        paragraph.setFont(new Font("Serial", StyleTypeDefinitions.FontStyle.BOLD, 16));

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
                            obj = cvt.toString(previews.get((String)obj));
                        }catch(DocumentNotFoundException ex){/**pas important*/}
                    }
                    cell.setStringValue(String.valueOf(obj));
                }
                colIndex++;
            }
        }

        return Collections.singletonList(doc);
    }


    private List generateFiches(RapportSectionObligationReglementaire section,
            Map<String,Objet> elements, File tempFolder, AtomicInteger inc) throws Exception{

        final PhotoChoiceObligationReglementaire photoChoice = section.getPhotoChoice();

        final List parts = new ArrayList();

        //titre
        final TextDocument doc = TextDocument.newTextDocument();
        final String titre = section.getLibelle();
        final Paragraph paragraph = doc.addParagraph(titre);
        paragraph.setFont(new Font("Serial", StyleTypeDefinitions.FontStyle.BOLD, 16));
        parts.add(doc);

        //on recupere le template
        final boolean isDefaultTemplate;
        final DocumentTemplate templateDoc;
        final String templateId = section.getTemplateId();
        if(templateId==null || templateId.isEmpty()){
            isDefaultTemplate = true;
            templateDoc = ODTUtils.getDefaultTemplate();
        }else{
            isDefaultTemplate = false;
            final AbstractSIRSRepository<TemplateOdt> repo = session.getRepositoryForClass(TemplateOdt.class);
            final TemplateOdt template = repo.get(templateId);
            if(template==null){
                throw new Exception("Template manquant pour l'identifiant : "+templateId);
            }
            final DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
            templateDoc = documentTemplateFactory.getTemplate(new ByteArrayInputStream(template.getOdt()));
        }


        //on recupere les elements qui correspondent a la requete
        final List<Element> validElements = listValidElements(section, elements);
        if(validElements.isEmpty()){
            return Collections.EMPTY_LIST;
        }

        //on genere une fiche pour chaque objet
        for(Element ele : validElements){
            final File f = new File(tempFolder, section.getLibelle()+"_"+inc.incrementAndGet()+".odt");
            if(isDefaultTemplate){
                ODTUtils.generateReport(templateDoc, ODTUtils.toTemplateMap(ele), f);
            }else{
                ODTUtils.generateReport(templateDoc, ele, f);
            }

            parts.add(f);

            if(ele instanceof ObjetPhotographiable){
                final ObjetPhotographiable photographiable = (ObjetPhotographiable) ele;
                final List<Photo> photos = photographiable.getPhotos();
                if(!photos.isEmpty()){
                    if(PhotoChoiceObligationReglementaire.DERNIERE.equals(photoChoice)){
                        Photo last = null;
                        for(Photo p : photos){
                            final String str = p.getChemin();
                            if(str!=null && !str.isEmpty()){
                                if(last==null || last.getDate()==null || (p.getDate()!=null && last.getDate().isBefore(p.getDate()))){
                                    final Path path = SIRS.getDocumentAbsolutePath(p.getChemin());
                                    if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
                                        last = p;
                                    }
                                }
                            }
                        }
                        if(last!=null){
                            parts.add(SIRS.getDocumentAbsolutePath(last.getChemin()));
                        }
                    }else if(PhotoChoiceObligationReglementaire.TOUTE.equals(photoChoice)){
                        for(Photo p : photos){
                            final String str = p.getChemin();
                            if(str!=null && !str.isEmpty()){
                                final Path path = SIRS.getDocumentAbsolutePath(p.getChemin());
                                if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
                                    parts.add(SIRS.getDocumentAbsolutePath(str));
                                }
                            }
                        }
                    }
                }
            }
        }

        return parts;
    }

    private List<Element> listValidElements(RapportSectionObligationReglementaire section, Map<String,Objet> elements) throws SQLException, DataStoreException{
        final List<Element> validElements = new ArrayList<>();
        final String requeteId = section.getRequeteId();

        if(requeteId==null || requeteId.isEmpty()){
            //pas de filtre
            validElements.addAll(elements.values());
            return validElements;
        }

        //creation de la requete
        final SQLQueryRepository queryRepo = (SQLQueryRepository)Injector.getSession().getRepositoryForClass(SQLQuery.class);
        final SQLQuery sqlQuery = queryRepo.get(requeteId);
        final Query fsquery = org.geotoolkit.data.query.QueryBuilder.language(
                JDBCFeatureStore.CUSTOM_SQL, sqlQuery.getSql(), NamesExt.create("requete"));

        //recupération de la base H2
        final FeatureStore h2Store = (H2FeatureStore) H2Helper.getStore(session.getConnector());
        final FeatureCollection col = h2Store.createSession(false).getFeatureCollection(fsquery);
        final String firstProperty = col.getFeatureType().getDescriptors().iterator().next().getName().tip().toString();

        //on filtre les elements
        try (FeatureIterator iterator = col.iterator()) {
            while(iterator.hasNext()){
                final Feature feature = iterator.next();
                final Object val = feature.getPropertyValue(firstProperty);
                final Objet ele = elements.get(val);
                if(ele!=null) validElements.add(ele);
            }
        }

        return validElements;
    }

    private static interface Printer{

        public Object print(Object candidate) throws Exception;
    }

    private class BorneDebutPrinter implements Printer{

        private final SirsStringConverter cvt = new SirsStringConverter();

        @Override
        public Object print(Object candidate) throws Exception {
            if(!(candidate instanceof Positionable)) return null;

            final Positionable p = (Positionable) candidate;

            final String borneId = p.getBorneDebutId();
            if(borneId==null) return null;

            final boolean borneAval = p.getBorne_debut_aval();
            final double borneDistance = p.getBorne_debut_distance();

            try{
                final Preview preview = Injector.getSession().getPreviews().get(borneId);
                final StringBuilder sb = new StringBuilder(cvt.toString(preview));
                if(borneDistance!=0.0){
                    sb.append( borneAval ? " en aval de " : " en amont de ");
                    sb.append((int)borneDistance);
                    sb.append("m");
                }
                return sb.toString();
            }catch(DocumentNotFoundException ex){
                return null;
            }
        }
    }

    private class BorneFinPrinter implements Printer{

        private final SirsStringConverter cvt = new SirsStringConverter();

        @Override
        public Object print(Object candidate) throws Exception {
            if(!(candidate instanceof Positionable)) return null;

            final Positionable p = (Positionable) candidate;

            final String borneId = p.getBorneFinId();
            if(borneId==null) return null;
            
            final boolean borneAval = p.getBorne_fin_aval();
            final double borneDistance = p.getBorne_fin_distance();

            try{
                final Preview preview = Injector.getSession().getPreviews().get(borneId);
                final StringBuilder sb = new StringBuilder(cvt.toString(preview));
                if(borneDistance!=0.0){
                    sb.append( borneAval ? " en aval de " : " en amont de ");
                    sb.append((int)borneDistance);
                    sb.append("m");
                }
                return sb.toString();
            }catch(DocumentNotFoundException ex){
                return null;
            }
        }
    }

}
