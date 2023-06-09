/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.*;
import fr.sirs.plugin.reglementaire.FileTreeItem;
import fr.sirs.plugin.reglementaire.PDFUtils;
import fr.sirs.plugin.reglementaire.RegistreTheme;
import fr.sirs.ui.LoadingPane;
import fr.sirs.ui.report.FXModeleRapportsPane;
import fr.sirs.util.DatePickerConverter;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.SirsStringConverter;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.sis.measure.NumberRange;
import org.apache.sis.util.ArgumentChecks;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static fr.sirs.PropertiesFileUtilities.*;
import static fr.sirs.plugin.reglementaire.ui.DocumentsPane.*;

/**
 * Panneau de gestion de création de documents d'extraction des données de registre.
 *
 * @author Estelle Idée (Geomatys)
 */
public class ExtractionDocumentsPane extends BorderPane {

    @FXML private TextField uiDocumentNameField;
    @FXML private DatePicker uiPeriodeValidityFin;
    @FXML private DatePicker uiPeriodeValidityDebut;
    @FXML private DatePicker uiPeriodeHorodatageFin;
    @FXML private DatePicker uiPeriodeHorodatageDebut;
    @FXML private ComboBox<Preview> uiSECombo;
    @FXML private GridPane uiCoverGridpane;
    @FXML private CheckBox uiIsExternalPage;
    @FXML private Label uiCoverPathLabel;
    @FXML private TextField uiCoverPath;
    @FXML private Button chooseCoverFileButton;
    @FXML private Label uiTitleLabel;
    @FXML private TextField uiTitle;
    @FXML private Label uiStructureLabel;
    @FXML private TextField uiStructure;
    @FXML private TextField uiConclusionPath;
    @FXML private Button chooseConclusionFileButton;

    @FXML private Button uiGenerateBtn;
    private final FileTreeItem root;
    private Preview selectedSe;
    private List<Prestation> allPrestationsOnSelectedSe;
    private final FileChooser fileChooser;
    /**
     * Supported formats for cover and conclusion pages
     */
    private final List<String> supportedFormat = Arrays.asList("*.pdf");

    private final String JRXML_PATH = "/fr/sirs/jrxml/metaTemplateHorodatageCoverPage.jrxml";

    @Autowired
    private Session session;

    public ExtractionDocumentsPane(final FileTreeItem root) {
        super();
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        this.root = root;

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported formats for cover and conclusion pages", supportedFormat));
        uiTitleLabel = new Label("Titre *");
        uiTitleLabel.setTooltip(new Tooltip("Titre en haut de la page de garde"));
        uiTitle = new TextField("Registre");
        uiStructureLabel = new Label("Structure");
        uiStructureLabel.setTooltip(new Tooltip("Nom de la structure"));
        uiStructure = new TextField();

        uiGenerateBtn.setTooltip(new Tooltip("Générer le document dynamique"));
        DatePickerConverter.register(uiPeriodeValidityDebut);
        DatePickerConverter.register(uiPeriodeValidityFin);
        uiPeriodeValidityDebut.valueProperty().addListener((observable, oldValue, newValue) ->
                uiPeriodeValidityFin.setDayCellFactory(RegistreTheme.getUiPeriodFinDayCellFactory(uiPeriodeValidityDebut))
        );

        uiPeriodeValidityFin.valueProperty().addListener((observable, oldValue, newValue) ->
                uiPeriodeValidityDebut.setDayCellFactory(RegistreTheme.getUiPeriodDebutDayCellFactory(uiPeriodeValidityFin))
        );
        uiPeriodeHorodatageDebut.valueProperty().addListener((observable, oldValue, newValue) ->
                uiPeriodeHorodatageFin.setDayCellFactory(RegistreTheme.getUiPeriodFinDayCellFactory(uiPeriodeHorodatageDebut))
        );

        uiPeriodeHorodatageFin.valueProperty().addListener((observable, oldValue, newValue) ->
                uiPeriodeHorodatageDebut.setDayCellFactory(RegistreTheme.getUiPeriodDebutDayCellFactory(uiPeriodeHorodatageFin))
        );

        SIRS.initCombo(uiSECombo, FXCollections.observableList(session.getPreviews().getByClass(SystemeEndiguement.class)), null);

        uiSECombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            this.selectedSe = newValue;
            this.allPrestationsOnSelectedSe = getAllPrestationsInSelectedSeRegistre();
        });

        final SirsStringConverter converter = new SirsStringConverter();

        // model edition
        final FXModeleRapportsPane rapportEditor = new FXModeleRapportsPane();

        uiGenerateBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            final boolean isValidityDebutNull = uiPeriodeValidityDebut.valueProperty().isNull().get();
            final boolean isValidityFinNull = uiPeriodeValidityFin.valueProperty().isNull().get();
            final boolean isHorodatageDebutNull = uiPeriodeHorodatageDebut.valueProperty().isNull().get();
            final boolean isHorodatageFinNull = uiPeriodeHorodatageFin.valueProperty().isNull().get();
            final boolean isExternalPage = uiIsExternalPage.isSelected();
            return disableProperty().get()
                    || uiDocumentNameField.textProperty().getValue().trim().isEmpty()
                            || (isValidityDebutNull && isValidityFinNull && isHorodatageDebutNull && isHorodatageFinNull)
//                            || (isValidityDebutNull != isValidityFinNull
//                            || isHorodatageDebutNull != isHorodatageFinNull)
                            || uiSECombo.getValue() == null
                    || (isExternalPage && uiCoverPath.getText().trim().isEmpty())
                    || (!isExternalPage && uiTitle.getText().trim().isEmpty());
                },
                disableProperty(),
                uiPeriodeValidityDebut.valueProperty(),
                uiPeriodeValidityFin.valueProperty(),
                uiPeriodeHorodatageDebut.valueProperty(),
                uiPeriodeHorodatageFin.valueProperty(),
                uiDocumentNameField.textProperty(),
                uiSECombo.valueProperty(),
                uiIsExternalPage.selectedProperty(),
                uiCoverPath.textProperty(),
                uiTitle.textProperty()
        ));

        uiIsExternalPage.setSelected(true);

        // Hide or show rows depending on cover page type : external or automatically created from title and structure field.
        uiIsExternalPage.selectedProperty().addListener((obs, oldValue, newValue) -> {
            final ObservableList<RowConstraints> rowConstraints = uiCoverGridpane.getRowConstraints();
            for (int i = 1; i < rowConstraints.size(); i++) {
                rowConstraints.remove(i);
            }
            if (newValue) {
                uiCoverGridpane.getChildren().removeAll(uiTitleLabel, uiTitle, uiStructureLabel, uiStructure);
                uiCoverGridpane.addRow(1, uiCoverPathLabel, uiCoverPath, chooseCoverFileButton);
            } else {
                uiCoverGridpane.getChildren().removeAll(uiCoverPathLabel, uiCoverPath, chooseCoverFileButton);
                uiCoverGridpane.addRow(1, uiTitleLabel, uiTitle);
                uiCoverGridpane.addRow(2, uiStructureLabel, uiStructure);
            }
        });
    }



    @FXML
    private void generateDocument(ActionEvent event) {
        /*
         * INPUT FIELDS VERIFICATIONS
         */
        final String tmp = uiDocumentNameField.getText().trim();
        if (tmp.isEmpty()) {
            showErrorDialog("Vous devez remplir le nom du fichier");
            return;
        }
        if (uiPeriodeValidityDebut.getValue() == null && uiPeriodeValidityFin.getValue() == null
                && uiPeriodeHorodatageDebut.getValue() == null && uiPeriodeHorodatageFin.getValue() == null) {
            showErrorDialog("Vous devez renseigner au moins une date");
            return;
        }
        final String coverPath = uiCoverPath.getText().trim();
        final File coverFile;
        String title = uiTitle.getText().trim();
        if (uiIsExternalPage.isSelected()) {
            if (coverPath == null || coverPath.isEmpty()) {
                showErrorDialog("Veuillez sélectionner un fichier pour la page de garde.");
                return;
            }
            if (!coverPath.endsWith(".pdf")) {
                showErrorDialog("Le format du fichier de la page de garde n'est pas supporté.\n\n" +
                        "Formats supportés :\n" +
                        "- pdf", null, 0, 175);
                return;
            }
            coverFile = new File(coverPath);
            if (!coverFile.exists()) {
                showErrorDialog("Le fichier est introuvable : \n" + coverFile.getPath(), null, 0, 175);
                return;
            }
        } else {
            // TODO create pdf for cover page from field

            if (title.isEmpty()) {
                showErrorDialog("Veuillez renseigner le titre du document");
                return;
            }
            coverFile = null;
        }
        final String conclusionPath = uiConclusionPath.getText();
        final File conclusionFile;
        if (conclusionPath != null) {
            if (!conclusionPath.endsWith(".pdf")){
                showErrorDialog("Le format du fichier de la page de conclusion n'est pas supporté.\n\n" +
                        "Formats supportés :\n" +
                        "- pdf", null, 0, 175);
                return;
            }
            conclusionFile = new File(coverPath);
            if (!conclusionFile.exists()) {
                showErrorDialog("Le fichier est introuvable : \n" + conclusionFile.getPath(), null, 0, 175);
                return;
            }
        } else {
            conclusionFile = null;
        }

        if (this.allPrestationsOnSelectedSe == null || this.allPrestationsOnSelectedSe.isEmpty()) {
            showErrorDialog("Aucune prestation disponible sur le système d'endiguement.");
            return;
        }

        /*
         * OUTPUT PDF CREATION
         */

        final String docName;
        if (tmp.toLowerCase().endsWith(".pdf")) {
            docName = tmp;
        } else {
            docName = tmp + ".pdf";
        }
        final Preferences prefs = Preferences.userRoot().node("ReglementaryPlugin");
        String rootPath = prefs.get(ROOT_FOLDER, null);

        if (rootPath == null || rootPath.isEmpty()) {
            rootPath = setMainFolder();
        }

        final File rootDir = new File (rootPath);
        root.setValue(rootDir);

        final List<Prestation> prestations = filterPrestationsByDateFilters(new ArrayList<>(this.allPrestationsOnSelectedSe));
        if (prestations.isEmpty()) {
            showErrorDialog("Aucune prestation disponible sur le système d'endiguement aux périodes renseignées.");
            return;
        }

        List<File> filesToMerge = new ArrayList<>();

        // Add cover page to final PDF.
        if (coverFile == null) {
            try {
                final File coverPageTmpPath = createCoverPageAndAddToList(title);
                filesToMerge.add(coverPageTmpPath);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("The jrxml file was not found at " + JRXML_PATH, e);
            } catch (JRException e) {
                throw new IllegalStateException("Error while creating the cover page from jrxml file " + JRXML_PATH, e);
            } catch (IOException e) {
                throw new IllegalStateException("Error while creating temporary file for cover page.", e);
            }
        } else {
            filesToMerge.add(coverFile);
        }


        // Add conclusion page to final PDF.
        if (conclusionFile != null) filesToMerge.add(conclusionFile);

        String output = rootPath + "/" + docName;

        final Task generator = PDFUtils.mergeFiles(filesToMerge, output, false, true, true);
        generator.setOnSucceeded(evt -> Platform.runLater(() -> {
            root.update(false);
            SIRS.openFile(new File(output));
        }));

        disableProperty().bind(generator.runningProperty());
        LoadingPane.showDialog(generator);
    }

    /**
     * Create a standard cover page from user's inputs as a PDF file.
     * @param title title from @uiTitle.
     * @return the created temporary PDF file.
     * @throws IOException if error when creating the temporary file.
     * @throws JRException if error when creating the PDF file from the JRXML template.
     */
    private File createCoverPageAndAddToList(final String title) throws IOException, JRException {
        Map<String, Object> parameters = new HashMap();

        // set report parameters
        parameters.put("title", title);
        parameters.put("systemeEndiguement", this.selectedSe.getLibelle());
        parameters.put("dateDebutPicker", uiPeriodeValidityDebut.getValue());
        parameters.put("dateFinPicker", uiPeriodeValidityFin.getValue());
        parameters.put("structure", this.uiStructure.getText());

        Path coverPageTmpPath = Files.createTempFile("namefile", ".pdf");
        InputStream input = PrinterUtilities.class.getResourceAsStream(JRXML_PATH);
        JasperDesign jasperDesign = JRXmlLoader.load(input);
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
        OutputStream outputStream = new FileOutputStream(coverPageTmpPath.toFile());

        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        SIRS.openFile(coverPageTmpPath);
        return coverPageTmpPath.toFile();
    }

    /**
     * Event when button chooseCoverFileButton is clicked.
     * @param event
     */
    @FXML
    public void chooseCoverFile(ActionEvent event) {
        chooseCoverFile(uiCoverPath);
    }

    /**
     * Event when button chooseCoverFileButton is clicked.
     * @param event
     */
    @FXML
    public void chooseConclusionFile(ActionEvent event) {
        chooseCoverFile(uiConclusionPath);
    }

    /**
     * Event when button chooseCoverFileButton is clicked.
     */
    @FXML
    public void chooseCoverFile(TextField textField) {

        final Window owner = getParent().getScene().getWindow();
        final File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            textField.setText(file.getPath());
            if (!file.exists()) {
                showErrorDialog("Le fichier est introuvable : \n" + file.getPath());
                chooseCoverFile(textField);
                return;
            }
            if (!file.isFile()) {
                showErrorDialog("Veuillez sélectionner un fichier");
                chooseCoverFile(textField);
                return;
            }
        }
        fileChooser.setInitialDirectory(file.getParentFile());
    }

    public String setMainFolder() {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final MainFolderPane ipane = new MainFolderPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Emplacement du dossier racine");

        String rootPath = null;
        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            File f = new File(ipane.rootFolderField.getText());
            if (f.isDirectory() && verifyDatabaseVersion(f)) {
                rootPath = f.getPath();

                final Preferences prefs = Preferences.userRoot().node("ReglementaryPlugin");
                prefs.put(ROOT_FOLDER, rootPath);
                updateDatabaseIdentifier(new File(rootPath));
            }
        }
        return rootPath;
    }

//    private SystemeEndiguement getSelectedSE() {
//        final Preview newValue = uiSECombo.getSelectionModel().getSelectedItem();
//        if (newValue != null) {
//            return session.getRepositoryForClass(SystemeEndiguement.class).get(newValue.getElementId());
//        } else {
//            return null;
//        }
//    }

    /**
     * Method to get all the prestations on the selected @{@link SystemeEndiguement} from the @{@link Preview}
     *
     * @return The list of all the @{@link Prestation} available on @this.selectedSE. <br>
     * <ul>
     *     <li>Null if no @{@link Digue} on the SE;</li>
     *     <li>Empty ArrayList if no @{@link TronconDigue} or no @{@link Prestation}.</li>
     * </ul>
     */
    private List<Prestation> getAllPrestationsInSelectedSeRegistre() {
        if (this.selectedSe == null) return new ArrayList<>();
        return HorodatageReportPane.getAllPrestationsInSeRegistre(this.selectedSe, this.session);
    }

    private List<Prestation> filterPrestationsByDateFilters(List<Prestation> prestations) {
        ArgumentChecks.ensureNonNull("prestations", prestations);
        prestations = filterPrestationsByDateFilter(prestations, true);
        return filterPrestationsByDateFilter(prestations, false);
    }

    /**
     * Filter a @{@link List} of @{@link Prestation} by Validity dates or by Horodatage date.
     *
     * @param prestations the list of prestations to filter.
     * @param isValidityDate indicate whether the filter is using the validity dates or the horodatage dates.
     * @return the list of prestations after filtering.
     */
    private List<Prestation> filterPrestationsByDateFilter(List<Prestation> prestations, final boolean isValidityDate) {
        ArgumentChecks.ensureNonNull("prestations", prestations);

        final LocalDate periodeDebut = isValidityDate ? uiPeriodeValidityDebut.getValue() : uiPeriodeHorodatageDebut.getValue();
        final LocalDate periodeFin = isValidityDate ? uiPeriodeValidityFin.getValue() : uiPeriodeHorodatageFin.getValue();

        if (periodeDebut == null && periodeFin == null) {
            return prestations;
        }

        NumberRange dateRange;
        if (isValidityDate) {
            final long dateDebut = periodeDebut == null ? 0 : periodeDebut.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            final long dateFin = periodeFin == null ? Long.MAX_VALUE : periodeFin.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli();
            dateRange = NumberRange.create(dateDebut, true, dateFin, true);

            AvecBornesTemporelles.IntersectDateRange dateRangePredicate = new AvecBornesTemporelles.IntersectDateRange(dateRange);
            return prestations.stream().filter(dateRangePredicate).collect(Collectors.toList());

        } else {
            return prestations.stream().filter(p -> {
                final LocalDate horodatageDate = p.getHorodatageDate();
                if (horodatageDate == null) return false;

                return (periodeDebut == null || horodatageDate.isAfter(periodeDebut)) && (periodeFin == null || horodatageDate.isBefore(periodeFin));
            }).collect(Collectors.toList());
        }
    }
}
