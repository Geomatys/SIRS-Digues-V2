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
import fr.sirs.core.model.report.ModeleRapport;
import fr.sirs.plugin.reglementaire.FileTreeItem;
import fr.sirs.plugin.reglementaire.RegistreTheme;
import fr.sirs.ui.report.FXModeleRapportsPane;
import fr.sirs.util.DatePickerConverter;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
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
import org.apache.sis.measure.NumberRange;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.File;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.prefs.Preferences;

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

    @Autowired
    private Session session;

    private final SimpleObjectProperty<ModeleRapport> modelProperty = new SimpleObjectProperty<>();

    public ExtractionDocumentsPane(final FileTreeItem root) {
        super();
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        this.root = root;

        uiTitleLabel = new Label("Titre *");
        uiTitleLabel.setTooltip(new Tooltip("Titre en haut de la page de garde"));
        uiTitle = new TextField();
        uiStructureLabel = new Label("Structure *");
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

        final SirsStringConverter converter = new SirsStringConverter();

        // model edition
        final FXModeleRapportsPane rapportEditor = new FXModeleRapportsPane();

        uiGenerateBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            final boolean isValidityDebutNull = uiPeriodeValidityDebut.valueProperty().isNull().get();
            final boolean isValidityFinNull = uiPeriodeValidityFin.valueProperty().isNull().get();
            final boolean isHorodatageDebutNull = uiPeriodeHorodatageDebut.valueProperty().isNull().get();
            final boolean isHorodatageFinNull = uiPeriodeHorodatageFin.valueProperty().isNull().get();
            final boolean isExternalPage = uiIsExternalPage.isSelected();
            return uiDocumentNameField.textProperty().getValue().isEmpty()
                            || (isValidityDebutNull && isValidityFinNull && isHorodatageDebutNull && isHorodatageFinNull)
                            || (isValidityDebutNull != isValidityFinNull
                            || isHorodatageDebutNull != isHorodatageFinNull)
                            || uiSECombo.getValue() == null
                    || (isExternalPage && uiCoverPath.getText().isEmpty())
                    || (!isExternalPage && uiTitle.getText().isEmpty());
                },
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
        final String tmp = uiDocumentNameField.getText();
        if (tmp.isEmpty()) {
            showErrorDialog("Vous devez remplir le nom du fichier");
            return;
        }

        final String docName;
        if (tmp.toLowerCase().endsWith(".pdf")) {
            docName= tmp;
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

        ModeleRapport modele = modelProperty.get();
        if (modele == null) {
            showErrorDialog("Vous devez sélectionner un modèle.");
            return;
        }


        final LocalDate periodeDebut = uiPeriodeValidityDebut.getValue();
        final LocalDate periodeFin = uiPeriodeValidityFin.getValue();
        final NumberRange dateRange;
        if (periodeDebut == null && periodeFin == null) {
            dateRange = null;
        } else {
            final long dateDebut = periodeDebut == null ? 0 : periodeDebut.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            final long dateFin = periodeFin == null ? Long.MAX_VALUE : periodeFin.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli();
            dateRange = NumberRange.create(dateDebut, true, dateFin, true);
        }

//        final boolean onlySE = uiOnlySEBox.isSelected();
//        final File seDir = getOrCreateSE(rootDir, getSelectedSE(), LIBELLE, true, DOCUMENT_FOLDER);
        final Task generator;
//        if (onlySE) {
//            final Path outputDoc = seDir.toPath().resolve(DOCUMENT_FOLDER).resolve(docName);
////            generator = ODTUtils.generateDoc(modele, getTronconList(), outputDoc.toFile(), root.getLibelle(), dateRange);
//        } else {
//            generator = ODTUtils.generateDocsForDigues(docName, onlySE, modele, getTronconList(), seDir, root.getLibelle(), dateRange);
//        }

//        generator.setOnSucceeded(evt -> Platform.runLater(() -> root.update(false)));
//        disableProperty().bind(generator.runningProperty());
//        LoadingPane.showDialog(generator);
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
        final FileChooser fileChooser = new FileChooser();
        final File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            textField.setText(file.getPath());
        }
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

}
