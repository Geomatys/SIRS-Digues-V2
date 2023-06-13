
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

import com.giaybac.traprange.PDFTableExtractor;
import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableCell;
import com.giaybac.traprange.entity.TableRow;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.PrestationRepository;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.Role;
import fr.sirs.plugin.reglementaire.FileTreeItem;
import fr.sirs.plugin.reglementaire.PluginReglementary;
import fr.sirs.plugin.reglementaire.PropertiesFileUtilities;
import fr.sirs.plugin.reglementaire.RegistreTheme;
import fr.sirs.ui.Growl;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.nio.IOUtilities;

import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;

import static fr.sirs.plugin.reglementaire.PropertiesFileUtilities.*;

/**
 * Display Registre tree.
 * <ul>
 *     <li>Allows to add folders,</li>
 *     <li><ul>Allows to import timestamped synthese table PDF files :
 *          <li>Input horodatage date</li>
 *          <li>Option to automatically update the table prestations</li>
 *     </ul></li>
 *     <li>Allows to configure the root folder,</li>
 * </ul>
 * Supported external document formats : PDF
 * <p>
 *
 * @author Estelle Idée (Geomatys)
 */
public class DocumentsPane extends GridPane {

    @FXML
    private Button importDocButton;

    @FXML
    private Button deleteDocButton;

    @FXML
    private Button setFolderButton;

    @FXML
    private TreeTableView<File> tree1;

    @FXML
    private Button addExtractionButton;

    @FXML
    private Button addFolderButton;

    @FXML
    private Button hideShowButton;

    @FXML
    private Button hideFileButton;

    protected static final String BUTTON_STYLE = "buttonbar-button";

    private static final Image ADDF_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/add_folder.png"));
    private static final Image ADDD_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/add_doc.png"));
    private static final Image IMP_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/import.png"));
    private static final Image DEL_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/remove.png"));
    private static final Image SET_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/set.png"));
   private static final Image OP_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/ouvrir.png"));
    private static final Image HIDE_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/cocher-decocher.png"));
    private static final Image HI_HISH_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/afficher.png"));
    private static final Image SH_HISH_BUTTON_IMAGE = new Image(RegistreTheme.class.getResourceAsStream("images/masquer.png"));

    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
    public static final String ROOT_FOLDER      = "symadrem.root.folder";

    // SIRS hidden file properties
    public static final String TIMESTAMP_DATE = "timestamp_date";
    public static final String LIBELLE          = "libelle";
    public static final String HIDDEN           = "hidden";

    public static final String SE = "se";
    public static final String TR = "tr";
    public static final String DG = "dg";

    private final FileTreeItem root;

//    private final DynamicDocumentTheme dynDcTheme;

    public DocumentsPane(final FileTreeItem root) {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        this.root = root;

        getStylesheets().add(SIRS.CSS_PATH);

        addFolderButton.setGraphic(new ImageView(ADDF_BUTTON_IMAGE));
        importDocButton.setGraphic(new ImageView(IMP_BUTTON_IMAGE));
        deleteDocButton.setGraphic(new ImageView(DEL_BUTTON_IMAGE));
        setFolderButton.setGraphic(new ImageView(SET_BUTTON_IMAGE));
        addExtractionButton.setGraphic(new ImageView(ADDD_BUTTON_IMAGE));
        hideFileButton.setGraphic(new ImageView(HIDE_BUTTON_IMAGE));
        if (root.rootShowHiddenFile) {
            hideShowButton.setGraphic(new ImageView(SH_HISH_BUTTON_IMAGE));
        } else {
            hideShowButton.setGraphic(new ImageView(HI_HISH_BUTTON_IMAGE));
        }


        addFolderButton.setTooltip(new Tooltip("Ajouter un dossier"));
        importDocButton.setTooltip(new Tooltip("Importer un fichier"));
        deleteDocButton.setTooltip(new Tooltip("Supprimer un fichier"));
        setFolderButton.setTooltip(new Tooltip("Configurer le dossier racine"));
        addExtractionButton.setTooltip(new Tooltip("Extraire un registre"));
        hideShowButton.setTooltip(new Tooltip("Cacher/Afficher les fichiers cachés"));
        hideFileButton.setTooltip(new Tooltip("Cacher/Afficher le fichier sélectionné"));

        addFolderButton.getStyleClass().add(BUTTON_STYLE);
        importDocButton.getStyleClass().add(BUTTON_STYLE);
        deleteDocButton.getStyleClass().add(BUTTON_STYLE);
        setFolderButton.getStyleClass().add(BUTTON_STYLE);
        addExtractionButton.getStyleClass().add(BUTTON_STYLE);
        hideShowButton.getStyleClass().add(BUTTON_STYLE);
        hideFileButton.getStyleClass().add(BUTTON_STYLE);

        // Name column
        tree1.getColumns().get(0).setEditable(false);
        tree1.getColumns().get(0).setCellValueFactory((Callback) param -> {
            final TreeItem item = ((CellDataFeatures)param).getValue();
            if (item != null) {
                final File f = (File) item.getValue();
                return new SimpleObjectProperty(f);
            }
            return null;
        });
        tree1.getColumns().get(0).setCellFactory((Callback) param -> new FileNameCell());

        // Date column
        tree1.getColumns().get(1).setEditable(false);
        tree1.getColumns().get(1).setCellValueFactory((Callback) param -> {
            final TreeItem item = ((CellDataFeatures)param).getValue();
            if (item != null) {
                final File f = (File) item.getValue();
                synchronized (DATE_FORMATTER) {
                    return new SimpleStringProperty(DATE_FORMATTER.format(new Date(f.lastModified())));
                }
            }
            return null;
        });

        // Timestamp Date column
        tree1.getColumns().get(2).setEditable(false);
        tree1.getColumns().get(2).setCellValueFactory((Callback) param -> {
            final TreeItem item = ((CellDataFeatures)param).getValue();
            if (item != null) {
                final File f = (File) item.getValue();
                synchronized (DATE_FORMATTER) {
                    return new SimpleObjectProperty<>(f);
                }
            }
            return null;
        });
        tree1.getColumns().get(2).setCellFactory((Callback) param -> new TimeStampCell());

        // Size column
        tree1.getColumns().get(3).setEditable(false);
        tree1.getColumns().get(3).setCellValueFactory((Callback) param -> {
            final FileTreeItem f = (FileTreeItem) ((CellDataFeatures)param).getValue();
            if (f != null) {
                return new SimpleStringProperty(f.getSize());
            }
            return null;
        });

        // open column
        tree1.getColumns().get(4).setCellValueFactory((Callback) param -> {
            final FileTreeItem f = (FileTreeItem) ((CellDataFeatures)param).getValue();
            return new SimpleObjectProperty(f);
        });
        tree1.getColumns().get(4).setCellFactory((Callback) param -> new OpenCell());


        tree1.setShowRoot(false);
        tree1.setRoot(root);

        final Preferences prefs = Preferences.userRoot().node(PluginReglementary.NODE_PREFERENCE_NAME);
        final String rootPath   = prefs.get(ROOT_FOLDER, null);

        if (rootPath != null && PropertiesFileUtilities.verifyDatabaseVersion(new File(rootPath))) {
            final File rootDirectory = new File(rootPath);
            root.setValue(rootDirectory);
            root.update(root.rootShowHiddenFile);
            updateDatabaseIdentifier(rootDirectory);

        } else {
            importDocButton.disableProperty().set(true);
            deleteDocButton.disableProperty().set(true);
            addFolderButton.disableProperty().set(true);
        }

        final BooleanBinding guestOrExtern = new BooleanBinding() {

            {
                bind(Injector.getSession().roleBinding());
            }

            @Override
            protected boolean computeValue() {
                final Role userRole = Injector.getSession().roleBinding().get();
                return Role.GUEST.equals(userRole)
                        || Role.EXTERN.equals(userRole);
            }
        };

        setFolderButton.disableProperty().bind(guestOrExtern);
        deleteDocButton.disableProperty().bind(guestOrExtern);
    }

    /**
     * Method used by action on button importDocButton.
     * <p>
     * Allows to import the timestamped PDF files, or other type of files.
     * <p>
     * User must first select a directory in the tree.
     * <p>
     * For timestamped "Tableau de synthèse", the user has the option to automatically update the Prestations' horodatage attributes with the input date and file.
     * @param event
     * @throws IOException if error when copying the files to the selected directory.
     */
    @FXML
    public void createAndShowImportDialog(ActionEvent event) throws IOException {
        final File directory = getSelectedFile();
        if (directory == null || !directory.isDirectory()) {
            showInformationDialog("Veuillez sélectionner un dossier dans la liste des dossiers et fichiers");
            return;
        }
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final ImportPane ipane = new ImportPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        pane.lookupButton(ButtonType.OK).disableProperty()
                        .bind(Bindings.createBooleanBinding(
                                () -> ipane.isSyntheseTable.isSelected() &&
                                        ipane.horodatageDate.getValue() == null,
                                ipane.isSyntheseTable.selectedProperty(),
                                ipane.horodatageDate.valueProperty()
                        ));
        ipane.horodatageDate.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !ipane.isSyntheseTable.isSelected() ,
                ipane.isSyntheseTable.selectedProperty()
        ));
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Import de document");
        showImportDialog(dialog, directory);
    }

    /**
     * Show import dialog and deal with error : <br>
     * If @isSyntheseTable is selected, then the document must be a PDF -> show error message and reshow import dialog.
     * @param dialog the @{@link Dialog} to show.
     * @param directory the directory where to save the file.
     * @throws IOException if error while copying the file.
     */
    private void showImportDialog(final Dialog dialog, final File directory) throws IOException {
        final Optional opt = dialog.showAndWait();
        if (opt.isPresent() && ButtonType.OK.equals(opt.get())) {
            final ImportPane ipane = (ImportPane) dialog.getDialogPane().getContent();
            // Check if it is a Tableau de synthèse. If so, the timestamp date must be set.
            final boolean isSyntheseTable   = ipane.isSyntheseTable.isSelected();
            final LocalDate timeStampDate   = ipane.horodatageDate.getValue();
            final File f                    = new File(ipane.fileField.getText());
            final String fName              = f.getName();

            final File newFile              = new File(directory, f.getName());

            if (newFile.exists()) {
                showWarningDialog("Il existe déjà un fichier du même nom dans le répertoire sélectionné : \n\n" + newFile.getPath(), null, 600, 175);
                showImportDialog(dialog, directory);
                return;
            }

            if (isSyntheseTable) {
                if (fName == null || !fName.endsWith(".pdf")) {
                    showWarningDialog("Le tableau de synthèse doit être au format pdf.");
                    showImportDialog(dialog, directory);
                    return;
                }


                final Optional confirmOpt = showConfirmationDialog("Souhaitez-vous mettre automatiquement à jour les prestations du tableau?" +
                        "\n\nLes éléments suivants seront mis à jour pour chaque prestation :" +
                        "\n\n    - la date d'horodatage;" +
                        "\n    - le statut d'horodatage en " + RegistreTheme.refTimeStampedStatus + ";" +
                        "\n    - le lien vers le Tableau de synthèse : " + newFile + "." +
                        "\n\nSélectionner 'Annuler' pour annuler l'importation du fichier.", null, 600, 300, true);

                boolean updatePrestationsDate = false;
                if (confirmOpt.isPresent()) {
                    if (ButtonType.YES.equals(confirmOpt.get())) {
                        updatePrestationsDate = true;
                    } else if (ButtonType.NO.equals(confirmOpt.get())) {
                        // do nothing
                    } else if (ButtonType.CANCEL.equals(confirmOpt.get())) {
                        // cancel the file import process.
                        return;
                    }
                }

                if (!extractElementsInFile(f, timeStampDate, updatePrestationsDate)) return;
                setProperty(newFile, TIMESTAMP_DATE, timeStampDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            Files.copy(f.toPath(), newFile.toPath());

            // refresh tree
            update();
        }
    }


    /**
     * Extract text information from pdf file and update @{@link fr.sirs.core.model.Prestation}.
     *
     * @param pdf_filename pdf file to read.
     * @param timeStampDate timestamp date entered by user and used to update Prestations' horodatageDate if necessary.
     * @param updatePrestationsDate whether to automatically update the Prestations' horodatageDate.
     * @return true if the document has been successfully extracted.<p>
     * false otherwise.
     */
    private boolean extractElementsInFile(final File pdf_filename, final LocalDate timeStampDate, final boolean updatePrestationsDate) {
        ArgumentChecks.ensureNonNull("pdf_filename", pdf_filename);

        final PDFTableExtractor extractor = (new PDFTableExtractor())
                .setSource(pdf_filename);

        final List<Integer[]> exceptLines = new ArrayList<>();
        // two first lines of the doc : title and @SystemeEndiguement libelle
        exceptLines.add(new Integer[]{0,0});
        exceptLines.add(new Integer[]{1,0});

        final List<Integer> lastPageExceptLines = new ArrayList<>();
        // exclude last line of the last page -> corresponds to "Période : xx/xx/xxxx - xx/xx/xxxx"
        lastPageExceptLines.add(-1);

        //except lines
        final List<Integer> exceptLineIdxes = new ArrayList<>();
        final Multimap<Integer, Integer> exceptLineInPages = LinkedListMultimap.create();
        for (Integer[] exceptLine : exceptLines) {
            if (exceptLine.length == 1) {
                exceptLineIdxes.add(exceptLine[0]);
            } else if (exceptLine.length == 2) {
                final int lineIdx = exceptLine[0];
                final int pageIdx = exceptLine[1];
                exceptLineInPages.put(pageIdx, lineIdx);
            }
        }
        if (!exceptLineIdxes.isEmpty()) {
            extractor.exceptLine(Ints.toArray(exceptLineIdxes));
        }
        if (!exceptLineInPages.isEmpty()) {
            for (int pageIdx : exceptLineInPages.keySet()) {
                extractor.exceptLine(pageIdx, Ints.toArray(exceptLineInPages.get(pageIdx)));
            }
        }

        //except lines in last page
        if (lastPageExceptLines != null) {
            extractor.exceptLineInLastPage(lastPageExceptLines);
        }
        //begin parsing pdf file
        final List<Table> tables = extractor.extract();

        // Check the first line corresponds to the Tableau de synthèse header.
        // If not -> the document is not a Tableau de synthèse.
         List<TableCell> firstLineCells = new ArrayList<>();
         boolean isSyntheseDoc = true;
        try {
            final String[] headers = "Désignation;Libellé;Tronçon;Type de prestation;Date de début;Date de fin;Intervenant;Nom auteur;Commentaire;Identifiant SIRS".split(";");
            firstLineCells = tables.get(0).getRows().get(0).getCells();
            if (headers.length == firstLineCells.size()) {
                for (int i = 0; i < headers.length; i++) {
                    if (!headers[i].equals(firstLineCells.get(i).getContent())) {
                        isSyntheseDoc = false;
                        break;
                    }
                }
            } else {
                isSyntheseDoc = false;
            }
        } catch (RuntimeException re) {
            isSyntheseDoc = false;
        } finally {
            if (!isSyntheseDoc) {
                showErrorDialog("Le document n'est pas reconnu comme un tableau de synthèse", "Document non reconnu", 0, 0);
                return false;
            }
        }



        // Removes the header row of each page's table.
        tables.forEach(table -> table.getRows().remove(0));

        final int columnsNo = firstLineCells.size();

        final Map<String, String> prestationsIdsAndLibelleWithError = new HashMap<>();
        for (Table table: tables) {
            for (TableRow row : table.getRows()) {
                // Check that the last column exists and is not empty.
                // If not -> no identifiant SIRS, which means that the row is part of the previous row.
                boolean oldRow = false;
                String idSirs = "";
                try {
                    idSirs = row.getCells().get(columnsNo - 1).getContent();
                    if (idSirs.isEmpty()) {
                        oldRow = true;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    oldRow = true;
                } finally {
                    if (oldRow) continue;
                    final PrestationRepository prestationRepo = Injector.getBean(PrestationRepository.class);
                    final Prestation prestation;
                    try {
                        prestation = prestationRepo.get(idSirs);
                    } catch (RuntimeException re) {
                        SIRS.LOGGER.warning("Error while retrieving the Prestation with id " + idSirs);
                        String libelle = row.getCells().get(1).getContent();
                        if ("-".equals(libelle.trim())) {
                            libelle = "libellé non renseigné";
                        }
                        prestationsIdsAndLibelleWithError.put(idSirs, libelle);
                        continue;
                    }
                    if (prestation == null) continue;

                    if (updatePrestationsDate) {
                        prestation.setHorodatageStatusId(RegistreTheme.refTimeStampedStatus);
                        prestation.setHorodatageDate(timeStampDate);
                        prestationRepo.update(prestation);
                    }
                }
            }
        }

        if (prestationsIdsAndLibelleWithError.isEmpty()) return true;

        final StringBuilder text = new StringBuilder();
        prestationsIdsAndLibelleWithError.forEach((id, libelle) -> text.append("\n  -  ").append(libelle).append(" / ").append(id));
        showWarningDialog("Erreur lors de la récupération de " + prestationsIdsAndLibelleWithError.size() + " prestations : \n" + text, null, 600, 400);

        return true;
    }


    @FXML
    public void showRemoveDialog(ActionEvent event) throws IOException {
        final Optional opt      = showConfirmationDialog("Détruire le fichier/dossier dans le système de fichier ?", "Détruire document", 0, 0, false);

        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            final File f = getSelectedFile();
            if (f != null) {
                if (f.isDirectory()) {
                    IOUtilities.deleteRecursively(f.toPath());
                    removeProperty(f, TIMESTAMP_DATE);
                } else {
                    f.delete();
                }
                removeProperties(f);

                // refresh tree
                update();
            } else {
                showErrorDialog("Vous devez sélectionner un dossier.");
            }
        }
    }

    @FXML
    public void setMainFolder(ActionEvent event) {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final MainFolderPane ipane = new MainFolderPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Emplacement du dossier racine");

        final Optional opt = dialog.showAndWait();
        if (opt.isPresent() && ButtonType.OK.equals(opt.get())) {
            File f = new File(ipane.rootFolderField.getText());
            if (f.isDirectory() && verifyDatabaseVersion(f)) {
                String rootPath = f.getPath();

                final Preferences prefs = Preferences.userRoot().node(PluginReglementary.NODE_PREFERENCE_NAME);
                prefs.put(ROOT_FOLDER, rootPath);
                importDocButton.disableProperty().set(false);
                deleteDocButton.disableProperty().unbind();
                deleteDocButton.disableProperty().set(false);
                addFolderButton.disableProperty().set(false);
                addExtractionButton.disableProperty().set(false);
                // refresh tree
                final File rootDirectory = new File(rootPath);
                updateFileSystem(rootDirectory);
                root.setValue(rootDirectory);
                root.update(root.rootShowHiddenFile);
                updateDatabaseIdentifier(rootDirectory);
            }
        }
    }

    @FXML
    public void showAddFolderDialog(ActionEvent event) {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final NewFolderPane ipane = new NewFolderPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Création de dossier");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            String folderName  = ipane.folderNameField.getText();
            final File rootDir = root.getValue();
            switch (ipane.locCombo.getValue()) {
                case NewFolderPane.IN_CURRENT_FOLDER:
                    addToSelectedFolder(folderName);
                    break;
                case NewFolderPane.IN_SE_FOLDER:
                    addToModelFolder(rootDir, folderName, SE);
                    update();
                    break;
            }
        }
    }

    /**
     * Method used when action on button @addExtractionButton.
     * Opens the Extraction tab.
     * @param event
     */
    @FXML
    public void openExtractionTab(ActionEvent event) {
        final TabPane result = (TabPane) this.getParent().getParent();
        final Tab extraction = result.getTabs().filtered(tab -> RegistreTheme.EXTRACTION_TAB.equals(tab.getText())).stream().findFirst().orElseGet(null);
        if (extraction == null) {
            showErrorDialog("Erreur lors de l'ouverture de l'onglet l'extraction des registres.");
        }
        result.getSelectionModel().select(extraction);
    }

    @FXML
    public void hideFiles(ActionEvent event) {
        FileTreeItem item = (FileTreeItem) tree1.getSelectionModel().getSelectedItem();
        item.hidden.setValue(!item.hidden.getValue());
        update();
    }

    @FXML
    public void hideShowFiles(ActionEvent event) {
        root.rootShowHiddenFile = !root.rootShowHiddenFile;
        if (root.rootShowHiddenFile) {
            hideShowButton.setGraphic(new ImageView(SH_HISH_BUTTON_IMAGE));
        } else {
            hideShowButton.setGraphic(new ImageView(HI_HISH_BUTTON_IMAGE));
        }
        update();
    }

    private File getSelectedFile() {
        TreeItem<File> item = tree1.getSelectionModel().getSelectedItem();
        if (item != null) {
            return item.getValue();
        }
        return null;
    }

    private void update() {
        root.update(root.rootShowHiddenFile);
    }

    private void addToSelectedFolder(final String folderName) {
        File directory = getSelectedFile();
        if (directory != null && directory.isDirectory()) {
            final File newDir = new File(directory, folderName);
            newDir.mkdir();
            update();
        } else {
            showErrorDialog("Vous devez sélectionner un dossier.");
        }
    }


    private void addToModelFolder(final File rootDir, final String folderName, final String model) {
        for (File f : rootDir.listFiles()) {
            if (f.isDirectory()) {
                if (getIsModelFolder(f, model)) {
                    final File newDir = new File(f, folderName);
                    newDir.mkdir();
                    update();
                } else {
                    addToModelFolder(f, folderName, model);
                }
            }
        }
    }

    private static class OpenCell extends TreeTableCell {

        private final Button button = new Button();


        public OpenCell() {
            setGraphic(button);
            button.setGraphic(new ImageView(OP_BUTTON_IMAGE));
            button.getStyleClass().add(BUTTON_STYLE);
            button.disableProperty().bind(editingProperty());
            button.setOnAction(this::handle);

        }

        public void handle(ActionEvent event) {
            final FileTreeItem item = (FileTreeItem) getItem();
            if (item != null && item.getValue() != null) {
                File file = item.getValue();

                SIRS.openFile(file).setOnSucceeded(evt -> {
                    if (!Boolean.TRUE.equals(evt.getSource().getValue())) {
                        Platform.runLater(() -> {
                            new Growl(Growl.Type.WARNING, "Impossible de trouver un programme pour ouvrir le document.").showAndFade();
                        });
                    }
                });
            }
        }



        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            final FileTreeItem ft = (FileTreeItem) item;
            if (ft != null) {
                final File f          = ft.getValue();
                if (f != null && !f.isDirectory()) {
                    button.setTooltip(new Tooltip("Ouvrir le fichier"));
                    button.setVisible(true);
                } else {
                    button.setVisible(false);
                }
            } else {
                button.setVisible(false);
            }
        }
    }

    private static class FileNameCell extends TreeTableCell {

        private final Label label = new Label();

        public FileNameCell() {
            setGraphic(label);
        }

        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            File f = (File) item;
            label.opacityProperty().unbind();
            if (f != null) {
                final String name;
                if (getIsModelFolder(f)) {
                    name = getProperty(f, LIBELLE);
                } else {
                    name = f.getName();
                }
                label.setText(name);
                FileTreeItem fti = (FileTreeItem) getTreeTableRow().getTreeItem();
                if (fti != null) {
                    label.opacityProperty().bind(Bindings.when(fti.hidden).then(0.5).otherwise(1.0));
                }
            } else {
               label.setText("");
            }
        }
    }

    private static class TimeStampCell extends TreeTableCell {

        private final Label label = new Label();

        public TimeStampCell() {
            setGraphic(label);
        }

        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            File f = (File) item;
            label.opacityProperty().unbind();
            if (f != null) {
                final String name = getProperty(f, TIMESTAMP_DATE);
                label.setText(name);
                FileTreeItem fti = (FileTreeItem) getTreeTableRow().getTreeItem();
                if (fti != null) {
                    label.opacityProperty().bind(Bindings.when(fti.hidden).then(0.5).otherwise(1.0));
                }
            } else {
                label.setText("");
            }
        }
    }
}
