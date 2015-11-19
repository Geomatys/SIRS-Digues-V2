package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.report.ModeleRapport;
import fr.sirs.plugin.document.FileTreeItem;
import fr.sirs.plugin.document.ODTUtils;
import static fr.sirs.plugin.document.PropertiesFileUtilities.*;
import static fr.sirs.plugin.document.ui.DocumentsPane.ROOT_FOLDER;
import fr.sirs.ui.report.FXModeleRapportsPane;
import fr.sirs.util.SirsStringConverter;
import java.io.File;
import java.nio.file.Path;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Panneau de gestion de création de documents dynamiques.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class DynamicDocumentsPane extends BorderPane {

    @FXML private CheckBox uiSelectAllTronconBox;

    @FXML private CheckBox uiOnlySEBox;

    @FXML private ComboBox<Preview> uiSECombo;

    @FXML private ListView<TronconDigue> uiTronconsList;

    @FXML private BorderPane uiListPane;

    @FXML private BorderPane uiModelPane;

    @FXML private Button uiGenerateBtn;

    @FXML private Label uiTronconLabel;

    @FXML private TextField uiDocumentNameField;

    private final FileTreeItem root;

    @Autowired
    private Session session;

    private final SimpleObjectProperty<ModeleRapport> modelProperty = new SimpleObjectProperty<>();

    public DynamicDocumentsPane(final FileTreeItem root) {
        super();
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        this.root = root;

        uiGenerateBtn.setTooltip(new Tooltip("Générer le document dynamique"));

        SIRS.initCombo(uiSECombo, FXCollections.observableList(session.getPreviews().getByClass(SystemeEndiguement.class)), null);

        // Gestion de la liste de système d'endiguements et de tronçons associés
        uiSECombo.valueProperty().addListener(this::systemeEndiguementChange);
        uiTronconsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final SirsStringConverter converter = new SirsStringConverter();
        uiTronconsList.setCellFactory(new Callback<ListView<TronconDigue>, ListCell<TronconDigue>>() {
            @Override
            public ListCell<TronconDigue> call(ListView<TronconDigue> param) {
                return new ListCell<TronconDigue>() {
                    @Override
                    protected void updateItem(TronconDigue item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(converter.toString(item));
                        }
                    }
                };
            }
        });

        uiOnlySEBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            uiTronconLabel.setVisible(!newValue);
            uiSelectAllTronconBox.setVisible(!newValue);
            uiTronconsList.setVisible(!newValue);
        });

        uiSelectAllTronconBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                uiTronconsList.getSelectionModel().selectAll();
            } else {
                uiTronconsList.getSelectionModel().clearSelection();
            }
        });

        // model edition
        final FXModeleRapportsPane rapportEditor = new FXModeleRapportsPane();
        modelProperty.bind(rapportEditor.selectedModelProperty());
        uiListPane.setCenter(rapportEditor);
        uiModelPane.setCenter(rapportEditor.editor);
        uiGenerateBtn.disableProperty().bind(modelProperty.isNull());
    }

    @FXML
    private void generateDocument(ActionEvent event) {
        final String tmp = uiDocumentNameField.getText();
        if (tmp.isEmpty()) {
            showErrorDialog("Vous devez remplir le nom du fichier");
            return;
        }

        final String docName;
        if (!tmp.toLowerCase().endsWith(".odt")) {
            docName = tmp + ".odt";
        } else {
            docName= tmp;
        }
        final Preferences prefs = Preferences.userRoot().node("DocumentPlugin");
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

        final boolean onlySE = uiOnlySEBox.isSelected();
        final File seDir = getOrCreateSE(rootDir, getSelectedSE());
        final Task generator;
        if (onlySE) {
            final Path outputDoc = seDir.toPath().resolve(DocumentsPane.DOCUMENT_FOLDER).resolve(docName);
            generator = ODTUtils.generateDoc(modele, getTronconList(), outputDoc.toFile(), root);
        } else {
            generator = ODTUtils.generateDocsForDigues(docName, onlySE, modele, getTronconList(), seDir, root);
        }

        disableProperty().bind(generator.runningProperty());
        LoadingPane.showDialog(generator);
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

                final Preferences prefs = Preferences.userRoot().node("DocumentPlugin");
                prefs.put(ROOT_FOLDER, rootPath);
                updateDatabaseIdentifier(new File(rootPath));
            }
        }
        return rootPath;
    }

    /**
     * Rafraîchit la liste des tronçons associés au système d'endiguement choisi.
     *
     * @param observable système d'endiguement
     * @param oldValue ancien système
     * @param newValue nouveau système
     */
    private void systemeEndiguementChange(ObservableValue<? extends Preview> observable,
                                          Preview oldValue, Preview newValue) {
        if(newValue==null){
            uiTronconsList.setItems(FXCollections.emptyObservableList());
        }else{
            final SystemeEndiguementRepository sdRepo = (SystemeEndiguementRepository) Injector.getSession().getRepositoryForClass(SystemeEndiguement.class);
            final DigueRepository digueRepo = (DigueRepository) Injector.getSession().getRepositoryForClass(Digue.class);
            final TronconDigueRepository tronconRepo = (TronconDigueRepository) Injector.getSession().getRepositoryForClass(TronconDigue.class);
            final SystemeEndiguement sd = sdRepo.get(newValue.getElementId());
            final Set<TronconDigue> troncons = new HashSet<>();
            final List<Digue> digues = digueRepo.getBySystemeEndiguement(sd);
            for(Digue digue : digues){
                troncons.addAll(tronconRepo.getByDigue(digue));
            }
            uiTronconsList.setItems(FXCollections.observableArrayList(troncons));
        }
    }

    private SystemeEndiguement getSelectedSE() {
        final Preview newValue = uiSECombo.getSelectionModel().getSelectedItem();
        if (newValue != null) {
            return session.getRepositoryForClass(SystemeEndiguement.class).get(newValue.getElementId());
        } else {
            return null;
        }
    }

    private Collection<TronconDigue> getTronconList() {
        if (uiOnlySEBox.isSelected()) {
            final SystemeEndiguement sd              = getSelectedSE();
            if (sd == null) {
                return Collections.EMPTY_LIST;
            }
            final DigueRepository digueRepo          = (DigueRepository) session.getRepositoryForClass(Digue.class);
            final TronconDigueRepository tronconRepo = (TronconDigueRepository) session.getRepositoryForClass(TronconDigue.class);
            final Set<TronconDigue> troncons         = new HashSet<>();
            final List<Digue> digues                 = digueRepo.getBySystemeEndiguement(sd);
            for(Digue digue : digues){
                troncons.addAll(tronconRepo.getByDigue(digue));
            }
            return troncons;

        } else {
            return  uiTronconsList.getSelectionModel().getSelectedItems();
        }
    }

}
