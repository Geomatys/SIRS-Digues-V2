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
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.DatePickerConverter;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.sis.measure.NumberRange;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.prefs.Preferences;

import static fr.sirs.SIRS.*;

/**
 * Display print configuration for horodatage purpose : synthese boards report.
 *
 * @author Estelle Idee (Geomatys)
 */
public class HorodatageReportPane extends BorderPane {

    private static final String MEMORY_ERROR_MSG = String.format(
            "Impossible d'imprimer les tableaux de synthèse : la mémoire disponible est insuffisante. Vous devez soit :%n"
                    + " - sélectionner moins de prestations,%n"
                    + " - allouer plus de mémoire à l'application."
    );

    // TODO : check ignored fields
    public static final String[] COLUMNS_TO_IGNORE = new String[]{
            AUTHOR_FIELD, VALID_FIELD, FOREIGN_PARENT_ID_FIELD, LONGITUDE_MIN_FIELD,
            LONGITUDE_MAX_FIELD, LATITUDE_MIN_FIELD, LATITUDE_MAX_FIELD,
            DATE_MAJ_FIELD, COMMENTAIRE_FIELD,
            "prDebut", "prFin", "valid", "positionDebut", "positionFin", "epaisseur"};

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
    private GridPane uiGrid;
    @FXML
    private Button uiGenerate;
    @FXML
    private ProgressBar uiProgress;
    @FXML
    private Label uiProgressLabel;
    @FXML
    private CheckBox uiPeriod;
    @FXML
    private CheckBox uiHorodateFilter;

    private final BooleanProperty running = new SimpleBooleanProperty(false);

    @Autowired
    private Session session;

    public HorodatageReportPane() {
        super();
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);

//        uiGenerate.disableProperty().bind(
//                Bindings.or(running, modelProperty.isNull()));
        uiGenerate.disableProperty().bind(running);

        // model edition
//        final FXModeleRapportsPane rapportEditor = new FXModeleRapportsPane();
//        modelProperty.bind(rapportEditor.selectedModelProperty());
//        uiListPane.setCenter(rapportEditor);
//        uiEditorPane.setCenter(rapportEditor.editor);

        // Filter parameters
        uiPeriod.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                final LocalDate date = LocalDate.now();
                if (uiPeriodeDebut.getValue() == null) {
                    uiPeriodeDebut.valueProperty().set(date.minus(10, ChronoUnit.YEARS));
                }
                if (uiPeriodeFin.getValue() == null) {
                    uiPeriodeFin.setValue(date);
                }
                // Keeps the prestations with dates inside the period range
                uiPrestations.setItems(FXCollections.observableArrayList(filterPrestationsByDate(uiPrestations.getItems())));
            } else {
                forceResetListBySelectedSeAndApplyFilters();
            }
        });

        uiPeriodeDebut.disableProperty().bind(uiPeriod.selectedProperty().not());
        uiPeriodeDebut.editableProperty().bind(uiPeriod.selectedProperty());
        uiPeriodeFin.disableProperty().bind(uiPeriod.selectedProperty().not());
        uiPeriodeFin.editableProperty().bind(uiPeriod.selectedProperty());
        DatePickerConverter.register(uiPeriodeDebut);
        DatePickerConverter.register(uiPeriodeFin);
        uiPeriodeDebut.valueProperty().addListener((observable, oldValue, newValue) -> forceResetListBySelectedSeAndApplyFilters());
        uiPeriodeFin.valueProperty().addListener((observable, oldValue, newValue) -> forceResetListBySelectedSeAndApplyFilters());

        uiHorodateFilter.setSelected(true);
        uiHorodateFilter.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                uiPrestations.setItems(FXCollections.observableArrayList(filterPrestationsByNonHorodated(uiPrestations.getItems())));
            } else {
                forceResetListBySelectedSeAndApplyFilters();
            }
        });

        uiSystemEndiguement.valueProperty().addListener(this::systemeEndiguementChange);
        uiPrestations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        uiPrestations.getSelectionModel().getSelectedItems().addListener(this::tronconSelectionChange);
        final SirsStringConverter converter = new SirsStringConverter();
        uiPrestations.setCellFactory(param -> {
            return new ListCell() {
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
            };
        });

        final Previews previewRepository = session.getPreviews();
        SIRS.initCombo(uiSystemEndiguement, SIRS.observableList(previewRepository.getByClass(SystemeEndiguement.class)).sorted(), null);

        // Pour mettre a jour l'etat actif des boutons
//        tronconSelectionChange(null);
    }

    private void forceResetListBySelectedSeAndApplyFilters() {
        uiPrestations.setItems(FXCollections.observableArrayList(getAllPrestationsInSeRegistreAndApplyFilters(uiSystemEndiguement.getValue())));
    }

    private void systemeEndiguementChange(ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue) {
        if (newValue == null) {
            uiPrestations.setItems(FXCollections.emptyObservableList());
        } else {
            final List<Prestation> prestations = getAllPrestationsInSeRegistreAndApplyFilters(newValue);

            uiPrestations.setItems(FXCollections.observableArrayList(prestations));

        }
    }

    private List<Prestation> getAllPrestationsInSeRegistreAndApplyFilters(Preview sePreview) {
        if (sePreview == null) return new ArrayList<>();
        final SystemeEndiguementRepository sdRepo = (SystemeEndiguementRepository) session.getRepositoryForClass(SystemeEndiguement.class);
        final DigueRepository digueRepo = (DigueRepository) session.getRepositoryForClass(Digue.class);
        final TronconDigueRepository tronconRepo = Injector.getBean(TronconDigueRepository.class);
        final PrestationRepository prestationRepo = Injector.getBean(PrestationRepository.class);

        final SystemeEndiguement se = sdRepo.get(sePreview.getElementId());
        final Set<TronconDigue> troncons = new HashSet<>();
        final List<Digue> digues = digueRepo.getBySystemeEndiguement(se);
        for (Digue digue : digues) {
            troncons.addAll(tronconRepo.getByDigue(digue));
        }
        final List<Prestation> prestations = new ArrayList<>();
        for (TronconDigue troncon : troncons) {
            List<Prestation> presta = prestationRepo.getByLinear(troncon);
            presta.forEach(p -> {
                if (p.getRegistreAttribution())
                    prestations.add(p);
            });

        }

        /*
         * Apply filter to keep only the non horodated prestations
         */
        if (uiHorodateFilter.isSelected()) {
            filterPrestationsByNonHorodated(prestations);
        }

        /*
         * Apply date filter
         */
        if (uiPeriod.isSelected()) {
            filterPrestationsByDate(prestations);
        }

        return prestations;
    }

    private List<Prestation> filterPrestationsByNonHorodated(List<Prestation> prestations) {
        List<Prestation> prestationsToRemove = new ArrayList<>();
        prestations.forEach(prestation -> {
            String horodatageStatusId = prestation.getHorodatageStatusId();
            // Remove all prestations with a status null or different from "Non horodaté"
            if (horodatageStatusId == null || !"RefHorodatageStatus:1".equals(horodatageStatusId)) {
                prestationsToRemove.add(prestation);
            }
        });
        prestations.removeAll(prestationsToRemove);
        return prestations;
    }

    private List<Prestation> filterPrestationsByDate(List<Prestation> prestations) {
        if (!uiPeriod.isSelected()) return prestations;

        final LocalDate periodeDebut = uiPeriodeDebut.getValue();
        final LocalDate periodeFin = uiPeriodeFin.getValue();
        final NumberRange dateRange;
        if (periodeDebut == null && periodeFin == null) {
            dateRange = null;
        } else {
            final long dateDebut = periodeDebut == null ? 0 : periodeDebut.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            final long dateFin = periodeFin == null ? Long.MAX_VALUE : periodeFin.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli();
            dateRange = NumberRange.create(dateDebut, true, dateFin, true);
        }

        if (dateRange != null) {
            List<Prestation> prestationsToRemove = new ArrayList<>();
            for (Prestation presta : prestations) {
                //on vérifie la date
                final LocalDate objDateDebut = presta.getDate_debut();
                final LocalDate objDateFin = presta.getDate_fin();
                final long debut = objDateDebut == null ? 0 : objDateDebut.atTime(0, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
                final long fin = objDateFin == null ? Long.MAX_VALUE : objDateFin.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli();
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
     * Method to generate the Rapport de synthèse for the selected prestations.
     *
     */
    @FXML
    private void generateReport() throws FileNotFoundException {
        final ObservableList<Prestation> prestations = uiPrestations.getSelectionModel().getSelectedItems();
        if (prestations.isEmpty()) return;

        /*
        A- Selection of the output file destination folder.
        ======================================================*/

        final FileChooser chooser = new FileChooser();
        final Path previous = getPreviousPath();
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
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(prestations);
        Map<String, Object> parameters = new HashMap();
        parameters.put("CollectionBeanParam", beanColDataSource);
        parameters.put("systemeEndiguement", uiSystemEndiguement.getSelectionModel().getSelectedItem().getLibelle());
        if (uiPeriod.isSelected()) {
            parameters.put("dateDebutPicker", uiPeriodeDebut.getValue());
            parameters.put("dateFinPicker", uiPeriodeFin.getValue());
        }

        try {
            InputStream input = PrinterUtilities.class.getResourceAsStream("/fr/sirs/jrxml/prestation_A4.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(input);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            OutputStream outputStream = new FileOutputStream(output.toFile());
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

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
        final String str = prefs.get("path", null);
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
