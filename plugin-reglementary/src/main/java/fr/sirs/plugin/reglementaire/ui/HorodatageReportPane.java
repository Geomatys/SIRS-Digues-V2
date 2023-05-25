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
import javafx.beans.value.ObservableValue;
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
import java.util.logging.Level;
import java.util.prefs.Preferences;

/**
 * Display print configuration and generate report for horodatage purpose : prestation synthese table report.
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

    @Autowired
    private Session session;

    public HorodatageReportPane() {
        super();
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);

        uiTitre.setText("Tableau de synthèse prestation pour Registre horodaté");

        // Date filter checkbox
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
            updateListBySelectedSeAndApplyDateFilter();
        });

        DatePickerConverter.register(uiPeriodeDebut);
        DatePickerConverter.register(uiPeriodeFin);
        uiPeriodeDebut.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (uiPeriod.isSelected()) updateListBySelectedSeAndApplyDateFilter();
        });
        uiPeriodeFin.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (uiPeriod.isSelected()) updateListBySelectedSeAndApplyDateFilter();
        });

        uiSystemEndiguement.valueProperty().addListener(this::updatePrestationsList);
        uiPrestations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final SirsStringConverter converter = new SirsStringConverter();
        uiPrestations.setCellFactory(param -> new ListCell() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                String text = converter.toString(item);
                if (item != null) {
                    String id = ((Prestation) item).getTypePrestationId();
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
     * Method to update the list of available prestations for the selected Systeme Endiguement
     * and keeping the previously selected prestations if still available
     */
    private void updateListBySelectedSeAndApplyDateFilter() {
        List<Prestation> selectedPresta = new ArrayList<>(uiPrestations.getSelectionModel().getSelectedItems());
        updatePrestationsList(null, null, this.selectedSE);

        final ObservableList<Prestation> items = uiPrestations.getItems();

        if (items == null || items.isEmpty()) return;

        uiPrestations.getSelectionModel().clearSelection();
        // Keeps previous selection
        selectedPresta.forEach(presta -> uiPrestations.getSelectionModel().select(presta));
    }

    /**
     * Method to update the list of available prestations for the selected Systeme Endiguement
     * and apply date filter if the checkbox has been selected.
     */
    private void updatePrestationsList(ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue) {
        if (newValue == null) {
            uiPrestations.setItems(FXCollections.emptyObservableList());
        } else {
            if (!newValue.equals(this.selectedSE)) {
                this.selectedSE = newValue;
                this.allPrestationsOnSE = getAllPrestationsOnSelectedSE();
            }

            if (this.allPrestationsOnSE == null || this.allPrestationsOnSE.isEmpty()) {
                uiPrestations.setItems(FXCollections.emptyObservableList());
            };

            // copy the list to filter it without modifying the original one.
            List<Prestation> prestations = new ArrayList<>(this.allPrestationsOnSE);
            /*
             * Apply date filter
             */
            if (uiPeriod.isSelected()) {
                filterPrestationsByDate(prestations);
            }
            uiPrestations.setItems(FXCollections.observableArrayList(prestations));
        }
    }

    /**
     * Method to get all the prestations on the selected @{@link SystemeEndiguement} from the @{@link Preview}
     *
     * @return the list of all the prestations on @this.selectedSE
     */
    private List<Prestation> getAllPrestationsOnSelectedSE() {
        if (this.selectedSE == null) return new ArrayList<>();
        final SystemeEndiguementRepository sdRepo   = (SystemeEndiguementRepository) session.getRepositoryForClass(SystemeEndiguement.class);
        final DigueRepository digueRepo             = (DigueRepository) session.getRepositoryForClass(Digue.class);
        final TronconDigueRepository tronconRepo    = Injector.getBean(TronconDigueRepository.class);
        final PrestationRepository prestationRepo   = Injector.getBean(PrestationRepository.class);

        final List<Prestation> prestations          = new ArrayList<>();

        SystemeEndiguement se = null;
        try {
            se = sdRepo.get(this.selectedSE.getElementId());
        } catch (RuntimeException re) {
            SIRS.LOGGER.log(Level.WARNING, "Erreur lors de la récupération du Systeme d'endiguement avec l'id {0}", this.selectedSE.getElementId());
        }
        if (se == null) return prestations;

        final Set<TronconDigue> troncons    = new HashSet<>();
        final List<Digue> digues            = digueRepo.getBySystemeEndiguement(se);
        if (digues.isEmpty()) return prestations;

        for (Digue digue : digues) {
            troncons.addAll(tronconRepo.getByDigue(digue));
        }
        if (troncons.isEmpty()) return prestations;

        for (TronconDigue troncon : troncons) {
            List<Prestation> presta = prestationRepo.getByLinear(troncon);
            presta.forEach(p -> {
                if (p.getRegistreAttribution())
                    prestations.add(p);
            });
        }

        return prestations;
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
            dateRange = NumberRange.create(dateDebut, true, dateFin, true);
        }

        if (dateRange != null) {
            List<Prestation> prestationsToRemove = new ArrayList<>();
            for (Prestation presta : prestations) {
                //on vérifie la date
                final LocalDate objDateDebut    = presta.getDate_debut();
                final LocalDate objDateFin      = presta.getDate_fin();
                final long debut    = objDateDebut == null ? 0 : objDateDebut.atTime(0, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
                final long fin      = objDateFin == null ? Long.MAX_VALUE : objDateFin.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli();
                final NumberRange objDateRange = NumberRange.create(debut, true, fin, true);
                if (!dateRange.intersectsAny(objDateRange)) {
                    prestationsToRemove.add(presta);
                }
            }

            prestations.removeAll(prestationsToRemove);
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
        List<Prestation> nonTimeStampedPrestations = new ArrayList<>();
        prestations.forEach(prestation -> {
            String horodatageStatusId = prestation.getHorodatageStatusId();
            // Keeps all prestations with a status "Non horodaté"
            if ("RefHorodatageStatus:1".equals(horodatageStatusId)) {
                nonTimeStampedPrestations.add(prestation);
            }
        });
        nonTimeStampedPrestations.forEach(presta -> uiPrestations.getSelectionModel().select(presta));
    }


    /**
     * Method to generate the Rapport de synthèse for the selected prestations.
     *
     */
    @FXML
    private void generateReport() throws FileNotFoundException {
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
            InputStream input           = PrinterUtilities.class.getResourceAsStream("/fr/sirs/jrxml/metaTemplatePrestationSyntheseTable.jrxml");
            JasperDesign jasperDesign   = JRXmlLoader.load(input);
            JasperReport jasperReport   = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint     = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            OutputStream outputStream   = new FileOutputStream(output.toFile());

            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            SIRS.openFile(output);

            // Change prestations' horodatage status to "En attente".
            PrestationRepository repo   = Injector.getBean(PrestationRepository.class);
            prestations.forEach(p -> {
                p.setHorodatageStatusId("RefHorodatageStatus:2");
                repo.update(p);
            });

        } catch (FileNotFoundException e) {
            throw e;
        } catch (JRException e) {
            throw new IllegalStateException("Error while creating the synthese prestation report from jrxm file", e);
        }
    }

    /**
     * @return Last chosen path for generation report, or null if we cannot find any.
     */
    private static Path getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(HorodatageReportPane.class);
        final String str        = prefs.get("path", null);
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
        prefs.put("path", path.toAbsolutePath().toString());
    }
}
