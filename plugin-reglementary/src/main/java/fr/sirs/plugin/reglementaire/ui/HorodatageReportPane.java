/**
 * This file is part of SIRS-Digues 2.
 * Copyright (C) 2016, FRANCE-DIGUES,
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.DatePickerConverter;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.SirsStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.sis.measure.NumberRange;
import org.apache.sis.util.ArgumentChecks;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Display print configuration and generate report for horodatage purpose : prestation synthese table report.
 *
 * Redmine ticket #7782
 *
 * <ul>
 * <li> Allow to select the file destination folder.</li>
 * <li> Modify the title inside the document.</li>
 * <li> "Periode" checkbox allows to filter the prestations by date.</li>
 * <li> Input dates are used inside the report to add a note for the time period.
 *      User can set the dates even though the checkbox is unselected (client's request).</li>
 * <li> Button to select "non horodatée" prestations in one click.</li>
 * </ul>
 *
 * @author Estelle Idee (Geomatys)
 */
public class HorodatageReportPane extends BorderPane {
    @FXML
    private ComboBox<Preview> uiSystemEndiguement;
    @FXML
    private ListView<Prestation> uiPrestations;
    /**
     *  When the uiPeriod checkbox is unselected, the uiPeriodeDebut and uiPeriodeFin can still be updated.
     *  The values are used in the generated Tableau de synthèse event though the uiPeriod is unselected.
     */
    @FXML
    private DatePicker uiPeriodeFin;
    @FXML
    private DatePicker uiPeriodeDebut;
    @FXML
    private TextField uiTitre;
    @FXML
    private Button uiSelectNonTimeStamped;
    @FXML
    private Button uiGenerate;
    @FXML
    private CheckBox uiPeriod;

    private Preview selectedSE;
    private List<Prestation> allPrestationsOnSE;

    private final String JRXML_PATH = "/fr/sirs/jrxml/metaTemplatePrestationSyntheseTable.jrxml";
    private static final String PATH_KEY = "path";
    private final String refNonTimeStampedStatus;
    private final String refWaitingStatus;

    @Autowired
    private Session session;

    public HorodatageReportPane(final String refNonTimeStampedStatus, final String refWaitingStatus) {
        super();
        ArgumentChecks.ensureNonNull("refNonTimeStampedStatus", refNonTimeStampedStatus);
        ArgumentChecks.ensureNonNull("refWaitingStatus", refWaitingStatus);

        SIRS.loadFXML(this);
        Injector.injectDependencies(this);

        uiTitre.setText("Tableau de synthèse prestation pour Registre horodaté");

        this.refNonTimeStampedStatus = refNonTimeStampedStatus;
        this.refWaitingStatus = refWaitingStatus;

        // Date filter checkbox
        // When the the checkbox is unselected, the uiPeriodeDebut and uiPeriodeFin can still be updated.
        // The values are used in the generated Tableau de synthèse event though the uiPeriod is unselected.
        // When the checkbox is unselected, the uiPeriodeDebut and uiPeriodeFin can still be updated.
        // The values are used in the generated Tableau de synthèse event though the uiPeriod is unselected.
        uiPeriod.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (newValue) {
                final LocalDate date = LocalDate.now();
                if (uiPeriodeDebut.getValue() == null) {
                    uiPeriodeDebut.valueProperty().set(date.minus(10, ChronoUnit.YEARS));
                }
                if (uiPeriodeFin.getValue() == null) {
                    uiPeriodeFin.setValue(date);
                }
            }
            updatePrestationsAndKeepSelection();
        });

        DatePickerConverter.register(uiPeriodeDebut);
        DatePickerConverter.register(uiPeriodeFin);
        uiPeriodeDebut.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (uiPeriod.isSelected()) updatePrestationsAndKeepSelection();
        });
        uiPeriodeFin.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (uiPeriod.isSelected()) updatePrestationsAndKeepSelection();
        });

        uiSystemEndiguement.valueProperty().addListener((obs, o, n) -> updatePrestationsList(n));
        uiPrestations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiPrestations.setCellFactory(param -> new ListCell<Prestation>() {
            final SirsStringConverter converter = new SirsStringConverter();
            @Override
            protected void updateItem(Prestation item, boolean empty) {
                super.updateItem(item, empty);
                String text = converter.toString(item);
                if (item != null) {
                    String id = item.getTypePrestationId();
                    if (id != null) {
                        RefPrestationRepository refPrestationRepo = Injector.getBean(RefPrestationRepository.class);
                        text = text + " / " + refPrestationRepo.get(id).getLibelle();
                    }
                }
                setText(text);
            }
        });

        final Previews previewRepository = session.getPreviews();
        SIRS.initCombo(uiSystemEndiguement, SIRS.observableList(previewRepository.getByClass(SystemeEndiguement.class)).sorted(), null);

        uiSelectNonTimeStamped.setTooltip(new Tooltip("Sélectionne toutes les prestations avec le status \"non horodatée\" de la liste."));
    }

    /**
     * Method to update the list of available prestations for the selected Systeme Endiguement @this.selectedSE after a date filter modification :
     * <ul>
     *     <li>Selection/unselection of the Period checkbox @uiPeriod</li>
     *     <li>Modification of the start date @uiPeriodeDebut</li>
     *     <li>Modification of the end date @uiPeriodeFin</li>
     * </ul>
     * Keeps the previously selected prestations if still available.
     */
    private void updatePrestationsAndKeepSelection() {
        List<Prestation> selectedPresta = new ArrayList<>(uiPrestations.getSelectionModel().getSelectedItems());
        updatePrestationsList(this.selectedSE);

        final ObservableList<Prestation> items = uiPrestations.getItems();

        if (items == null || items.isEmpty()) return;

        uiPrestations.getSelectionModel().clearSelection();
        // Keeps previous selection
        selectedPresta.forEach(presta -> uiPrestations.getSelectionModel().select(presta));
    }

    /**
     * Method to update the list of available @{@link Prestation} for the input @{@link SystemeEndiguement}
     * and apply date filter if the checkbox has been selected.
     *
     * <p>If the input @{@link SystemeEndiguement} has changed since the last selection, then the @{@link Prestation} are collected from the data base.<br>
     *  Otherwise the prestations are recovered from the class variable @allPrestationsOnSE.</p>
     *
     * @param newValue the @{@link SystemeEndiguement} for which to collect the available @{@link Prestation} and apply date filter if necessary.
     */
    private void updatePrestationsList(Preview newValue) {
        if (newValue == null) {
            uiPrestations.setItems(FXCollections.emptyObservableList());
        } else {
            // the @SystemeEndiguement has changed, the class variables are updated with the new values.
            if (!newValue.equals(this.selectedSE)) {
                this.selectedSE = newValue;
                this.allPrestationsOnSE = getAllPrestationsOnSelectedSE();
            }

            if (this.allPrestationsOnSE == null || this.allPrestationsOnSE.isEmpty()) {
                uiPrestations.setItems(FXCollections.emptyObservableList());
                return;
            }

            // copy the list to filter it without modifying the original one.
            List<Prestation> prestations = new ArrayList<>(this.allPrestationsOnSE);
            /*
             * Apply date filter
             */
            if (uiPeriod.isSelected()) {
                prestations = filterPrestationsByDate(prestations);
            }
            uiPrestations.setItems(FXCollections.observableArrayList(prestations));
        }
    }

    /**
     * Method to get all the prestations on the selected @{@link SystemeEndiguement} from the @{@link Preview}
     *
     * @return The list of all the @{@link Prestation} available on @this.selectedSE. <br>
     * <ul>
     *     <li>Null if no @{@link Digue} on the SE;</li>
     *     <li>Empty ArrayList if no @{@link TronconDigue} or no @{@link Prestation}.</li>
     * </ul>
     */
    private List<Prestation> getAllPrestationsOnSelectedSE() {
        if (this.selectedSE == null) return new ArrayList<>();
        final SystemeEndiguementRepository sdRepo   = (SystemeEndiguementRepository) session.getRepositoryForClass(SystemeEndiguement.class);
        final DigueRepository digueRepo             = (DigueRepository) session.getRepositoryForClass(Digue.class);
        final TronconDigueRepository tronconRepo    = Injector.getBean(TronconDigueRepository.class);
        final PrestationRepository prestationRepo   = Injector.getBean(PrestationRepository.class);

        SystemeEndiguement se = null;
        try {
            se = sdRepo.get(this.selectedSE.getElementId());
        } catch (RuntimeException re) {
            SIRS.LOGGER.log(Level.WARNING, "Erreur lors de la récupération du Systeme d'endiguement avec l'id {0}", this.selectedSE.getElementId());
        }
        if (se == null) return null;

        final List<Digue> digues = digueRepo.getBySystemeEndiguement(se);
        if (digues.isEmpty()) return null;

        return digues.stream().flatMap(digue -> tronconRepo.getByDigue(digue).stream())
                .flatMap(troncon -> {
                    List<Prestation> presta = prestationRepo.getByLinear(troncon);
                    return presta.stream().filter(Prestation::getRegistreAttribution);
                })
                .collect(Collectors.toList());
    }

    /**
     * Filter the list of the prestations according to the date pickers.
     *
     * @param prestations the list of the prestations to filter.
     * @return the list of the prestations after date filtering.
     */
    private List<Prestation> filterPrestationsByDate(List<Prestation> prestations) {
        ArgumentChecks.ensureNonNull("prestations", prestations);
        if (prestations.isEmpty() || !uiPeriod.isSelected()) return prestations;

        final LocalDate periodeDebut    = uiPeriodeDebut.getValue();
        final LocalDate periodeFin      = uiPeriodeFin.getValue();
        final NumberRange dateRange;
        if (periodeDebut == null && periodeFin == null) {
            dateRange = null;
        } else {
            final long dateDebut    = periodeDebut == null ? 0 : periodeDebut.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            final long dateFin      = periodeFin == null ? Long.MAX_VALUE : periodeFin.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli();
            dateRange               = NumberRange.create(dateDebut, true, dateFin, true);
        }

        if (dateRange != null) {
            AvecBornesTemporelles.IntersectDateRange dateRangePredicate = new AvecBornesTemporelles.IntersectDateRange(dateRange);
            return prestations.stream().filter(dateRangePredicate).collect(Collectors.toList());
        }
        return prestations;
    }

    /**
     * Method to select all the prestations available in the @uiPrestations.
     *
     */
    @FXML
    private void selectAll() {
        uiPrestations.getSelectionModel().selectAll();
    }

    /**
     * Method to select all "non horodatée" prestations available in the @uiPrestations.
     *
     */
    @FXML
    private void selectNonTimeStamped() {
        List<Prestation> prestations = uiPrestations.getItems();
        if (prestations.isEmpty()) return;
        // Keeps all prestations with a status "Non horodaté"
        prestations.stream()
                .filter(prestation -> this.refNonTimeStampedStatus.equals(prestation.getHorodatageStatusId()))
                .forEach(presta -> uiPrestations.getSelectionModel().select(presta));
    }


    /**
     * <p>Method to generate the Rapport de synthèse for the selected prestations.</p>
     * <ul>
     *     <li>Let the user choose the destination folder and output file's name</li>
     *     <li>Open the created PDF</li>
     *     <li>Uses the JRXML "/fr/sirs/jrxml/metaTemplatePrestationSyntheseTable.jrxml"</li>
     * </ul>
     *
     */
    @FXML
    private void generateReport() {
        List<Prestation> prestations = FXCollections.observableArrayList(uiPrestations.getSelectionModel().getSelectedItems());
        if (prestations.isEmpty()) return;

        // sort prestations by date_fin if available, by date_debut otherwise.
        prestations.sort((p1, p2) -> {
            LocalDate dateFin1  = p1.getDate_fin();
            LocalDate dateFin2  = p2.getDate_fin();
            LocalDate date1     = dateFin1 != null ? dateFin1 : p1.getDate_debut();
            LocalDate date2     = dateFin2 != null ? dateFin2 : p2.getDate_debut();
            return date1.compareTo(date2);
        });


        /*
        A- Selection of the output file destination folder.
        ======================================================*/

        final FileChooser chooser   = new FileChooser();
        final Path previous         = getPreviousPath();
        if (previous != null) {
            chooser.setInitialDirectory(previous.toFile());
            chooser.setInitialFileName(".pdf");
        }
        final File file = chooser.showSaveDialog(null);
        if (file == null) return;

        final Path output = file.toPath();
        setPreviousPath(output.getParent());

        /*
        A- Creation of the PDF from the jasper template.
        ======================================================*/
        JRBeanCollectionDataSource beanColDataSource    = new JRBeanCollectionDataSource(prestations);
        Map<String, Object> parameters                  = new HashMap();

        // set report parameters
        parameters.put("title", uiTitre.getText());
        parameters.put("collectionBeanParam", beanColDataSource);
        parameters.put("systemeEndiguement", this.selectedSE.getLibelle());
        parameters.put("dateDebutPicker", uiPeriodeDebut.getValue());
        parameters.put("dateFinPicker", uiPeriodeFin.getValue());

        try {
            InputStream input           = PrinterUtilities.class.getResourceAsStream(JRXML_PATH);
            JasperDesign jasperDesign   = JRXmlLoader.load(input);
            JasperReport jasperReport   = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint     = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            OutputStream outputStream   = new FileOutputStream(file);

            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            SIRS.openFile(output);

            // Change prestations' horodatage status to "En attente".
            PrestationRepository repo   = Injector.getBean(PrestationRepository.class);
            prestations.forEach(p -> {
                p.setHorodatageStatusId(this.refWaitingStatus);
                repo.update(p);
            });

        } catch (FileNotFoundException e) {
            throw new IllegalStateException("The jrxml file was not found at \"/fr/sirs/jrxml/metaTemplatePrestationSyntheseTable.jrxml\"", e);
        } catch (JRException e) {
            throw new IllegalStateException("Error while creating the synthese prestation report from jrxml file", e);
        }
    }

    /**
     * @return Last chosen path for generation report, or null if we cannot find any.
     */
    private static Path getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(HorodatageReportPane.class);
        final String str        = prefs.get(PATH_KEY, null);
        if (str != null) {
            final Path file = Paths.get(str);
            if (Files.isDirectory(file)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Set value to be retrieved by {@link #getPreviousPath() }.
     *
     * @param path To put as previously chosen path. Should be a directory.
     */
    private static void setPreviousPath(final Path path) {
        final Preferences prefs = Preferences.userNodeForPackage(HorodatageReportPane.class);
        prefs.put(PATH_KEY, path.toAbsolutePath().toString());
    }
}
