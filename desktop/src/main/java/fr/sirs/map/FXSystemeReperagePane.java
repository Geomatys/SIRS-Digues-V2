
package fr.sirs.map;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SimpleButtonColumn;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.ReferenceTableCell;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.gui.javafx.util.FXTableCell;
import org.geotoolkit.gui.javafx.util.FXTableView;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.util.StringUtilities;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSystemeReperagePane extends BorderPane {

    public static enum Mode{
        PICK_TRONCON,
        EDIT_BORNE,
        CREATE_BORNE,
        NONE
    };

    @FXML private TextField uiTronconLabel;
    @FXML private ToggleButton uiPickTroncon;
    @FXML private ChoiceBox<SystemeReperage> uiSrComboBox;
    @FXML private CheckBox uiDefaultSRCheckBox;
    @FXML private Button uiAddSr;
    @FXML private Button uiDeleteSR;
    @FXML private FXTableView<SystemeReperageBorne> uiBorneTable;
    @FXML private Button uiAddBorne;
    @FXML private ToggleButton uiCreateBorne;
    @FXML private Button uiProject;
    @FXML private Label typeNameLabel;

    private final ObjectProperty<TronconDigue> tronconProp = new SimpleObjectProperty<>();
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.NONE);
    private final Session session;
    private final FXMap map;

    /** A flag to indicate that selected {@link TronconDigue} must be saved. */
    private final SimpleBooleanProperty saveTD = new SimpleBooleanProperty(false);
    /** A flag to indicate that selected {@link SystemeReperage} must be saved. */
    private final SimpleBooleanProperty saveSR = new SimpleBooleanProperty(false);

    public FXSystemeReperagePane(FXMap map, final String typeName) {
        SIRS.loadFXML(this);

        typeNameLabel.setText(StringUtilities.firstToUpper(typeName)+ " :");
        this.map = map;
        session = Injector.getSession();

        uiPickTroncon.setGraphic(new ImageView(SIRS.ICON_CROSSHAIR_BLACK));
        uiAddSr.setGraphic(new ImageView(SIRS.ICON_ADD_BLACK));
        uiDeleteSR.setGraphic(new ImageView(GeotkFX.ICON_DELETE));
        uiProject.setDisable(true);

        //on active le choix du sr si un troncon est sélectionné
        final BooleanBinding srEditBinding = tronconProp.isNull();
        uiSrComboBox.disableProperty().bind(srEditBinding);
        uiSrComboBox.setConverter(new SirsStringConverter());
        uiAddSr.disableProperty().bind(srEditBinding);

        //on active la table et bouton de creation si un sr est sélectionné
        final BooleanBinding borneEditBinding = uiSrComboBox.valueProperty().isNull();
        uiBorneTable.disableProperty().bind(borneEditBinding);
        uiAddBorne.disableProperty().bind(borneEditBinding);
        uiCreateBorne.disableProperty().bind(borneEditBinding);

        //on active le calcule de PR uniquement si 2 bornes sont sélectionnées
        uiBorneTable.setEditable(true);
        uiBorneTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiBorneTable.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<SystemeReperageBorne>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends SystemeReperageBorne> c) {
                final int size = uiBorneTable.getSelectionModel().getSelectedItems().size();
                uiProject.setDisable(size<1);
            }
        });

        uiPickTroncon.setOnAction(this::startPickTroncon);
        uiAddBorne.setOnAction(this::startAddBorne);
        uiCreateBorne.setOnAction(this::startCreateBorne);
        uiAddSr.setOnAction(this::createSystemeReperage);
        uiDeleteSR.setOnAction(this::deleteSystemeReperage);
        uiProject.setOnAction(this::projectPoints);

        // Affichage du libellé du tronçon
        uiTronconLabel.textProperty().bind(Bindings.createStringBinding(()->tronconProp.get()==null?"":tronconProp.get().getLibelle(),tronconProp));

        //etat des boutons sélectionné
        final ToggleGroup group = new ToggleGroup();
        uiPickTroncon.setToggleGroup(group);
        uiCreateBorne.setToggleGroup(group);

        mode.addListener(new ChangeListener<Mode>() {
            @Override
            public void changed(ObservableValue<? extends Mode> observable, Mode oldValue, Mode newValue) {
                if(newValue==Mode.CREATE_BORNE){
                    group.selectToggle(uiCreateBorne);
                }else if(newValue==Mode.PICK_TRONCON){
                    group.selectToggle(uiPickTroncon);
                }else{
                    group.selectToggle(null);
                }
            }
        });


        //colonne de la table
        final TableColumn<SystemeReperageBorne,SystemeReperageBorne> deleteCol = new DeleteColumn();
        final TableColumn<SystemeReperageBorne,SystemeReperageBorne> nameCol = new NameColumn();
        final TableColumn<SystemeReperageBorne,Number> prCol = new PRColumn();

        uiBorneTable.getColumns().add(deleteCol);
        uiBorneTable.getColumns().add(nameCol);
        uiBorneTable.getColumns().add(prCol);

        // Initialize event listeners
        tronconProp.addListener(this::tronconChanged);
        uiSrComboBox.valueProperty().addListener(this::updateBorneTable);
        uiSrComboBox.valueProperty().addListener(this::updateDefaultSRCheckBox);
        uiDefaultSRCheckBox.selectedProperty().addListener(this::updateTonconDefaultSR);

    }

    public void reset(){
        mode.set(Mode.PICK_TRONCON);
        systemeReperageProperty().set(null);
        tronconProperty().set(null);
    }

    public void selectSRB(SystemeReperageBorne srb){
        final int index = uiBorneTable.getItems().indexOf(srb);
        if(index>=0){
            uiBorneTable.getSelectionModel().clearAndSelect(index);
        }else{
            uiBorneTable.getSelectionModel().clearSelection();
        }
    }

    public ReadOnlyObjectProperty<Mode> modeProperty(){
        return mode;
    }

    public ObjectProperty<TronconDigue> tronconProperty(){
        return tronconProp;
    }

    public ObjectProperty<SystemeReperage> systemeReperageProperty(){
        return uiSrComboBox.valueProperty();
    }

    public ObservableList<SystemeReperageBorne> borneProperties(){
        return uiBorneTable.getSelectionModel().getSelectedItems();
    }

    public void save() {
        save(uiSrComboBox.getValue(), tronconProp.getValue());
    }

    private void save(final SystemeReperage sr, final TronconDigue td) {
        final boolean mustSaveTd = saveTD.get();
        final boolean mustSaveSr = saveSR.get();

        if (mustSaveTd || mustSaveTd) {
            saveTD.set(false);
            saveSR.set(false);

            TaskManager.INSTANCE.submit("Sauvegarde...", () -> {
                if (td != null && mustSaveTd) {
                    ((AbstractSIRSRepository) session.getRepositoryForClass(td.getClass())).update(td);
                }

                if (sr != null && mustSaveSr) {
                    if (td != null) {
                        ((SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class)).update(sr, td);
                    } else {
                        ((SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class)).update(sr);
                    }
                }
            });
        }
    }

    private void startPickTroncon(ActionEvent evt){
        mode.set(Mode.PICK_TRONCON);
    }

    /*
     * BORNE UTILITIES
     */

    /**
     *
     * @return A list view of all bornes bound to currently selected troncon, or
     * null if no troncon is selected.
     */
    private ListView<BorneDigue> buildBorneList(final Set<String> toExclude) {
        final TronconDigue troncon = tronconProperty().get();
        if (troncon == null) return null;

        final AbstractSIRSRepository<BorneDigue> repo = session.getRepositoryForClass(BorneDigue.class);
        final ObservableList<String> borneIds;
        if (toExclude != null && !toExclude.isEmpty()) {
            borneIds = FXCollections.observableArrayList(troncon.getBorneIds());
            borneIds.removeIf((borneId) -> toExclude.contains(borneId));
        } else {
            borneIds = troncon.getBorneIds();
        }
        final List<BorneDigue> bornes = repo.get(borneIds);
        bornes.sort((BorneDigue b1, BorneDigue b2) -> {
            if (b1.getLibelle() == null) {
                return 1;
            }
            return b1.getLibelle().compareToIgnoreCase(b2.getLibelle());
        });

        final ListView<BorneDigue> bornesView = new ListView<>();
        bornesView.setItems(FXCollections.observableArrayList(bornes));
        bornesView.setCellFactory(TextFieldListCell.forListView(new SirsStringConverter()));
        bornesView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        return bornesView;
    }

    private void startAddBorne(ActionEvent evt){
        final TronconDigue troncon = tronconProperty().get();
        final SystemeReperage csr = systemeReperageProperty().get();
        if(csr==null || troncon==null) return;

        // Do not show bornes already present in selected SR.
        final HashSet<String> borneIdsAlreadyInSR = new HashSet<>();
        for (final SystemeReperageBorne srb : csr.systemeReperageBornes) {
            borneIdsAlreadyInSR.add(srb.getBorneId());
        }
        final ListView<BorneDigue> bornesView = buildBorneList(borneIdsAlreadyInSR);

        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.setContent(bornesView);
        pane.getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
        dialog.setDialogPane(pane);
        final Object res = dialog.showAndWait().get();

        if(ButtonType.OK.equals(res)){
            final ObservableList<BorneDigue> selectedItems = bornesView.getSelectionModel().getSelectedItems();
            for(BorneDigue bd : selectedItems){
                createBorne(bd);
            }
        }

    }

    private void startCreateBorne(ActionEvent evt){
        if(mode.get().equals(Mode.CREATE_BORNE)){
            //on retourne on mode edition
            mode.set(Mode.EDIT_BORNE);
        }else{
            mode.set(Mode.CREATE_BORNE);
        }
    }

    private void projectPoints(ActionEvent evt){
        final TronconDigue troncon = tronconProp.get();
        final SystemeReperage sr = systemeReperageProperty().get();
        if(troncon==null || sr==null) return;

        final LineString linear = LinearReferencingUtilities.asLineString(troncon.getGeometry());
        final LinearReferencingUtilities.SegmentInfo[] segments = LinearReferencingUtilities.buildSegments(linear);
        final AbstractSIRSRepository<BorneDigue> repo = session.getRepositoryForClass(BorneDigue.class);

        final ObservableList<SystemeReperageBorne> lst = uiBorneTable.getSelectionModel().getSelectedItems();

        for(SystemeReperageBorne srb : lst){
            final String borneId = srb.getBorneId();
            final BorneDigue borne = repo.get(borneId);
            final Point point = borne.getGeometry();

            final LinearReferencingUtilities.ProjectedPoint proj = LinearReferencingUtilities.projectReference(segments, point);
            point.getCoordinate().setCoordinate(proj.projected);

            repo.update(borne);
        }

        uiBorneTable.getSelectionModel().clearSelection();
        map.getCanvas().repaint();
    }

    private void createSystemeReperage(ActionEvent evt){
        final TronconDigue troncon = tronconProperty().get();
        if(troncon==null) return;

        final TextInputDialog dialog = new TextInputDialog("Nom du SR");
        dialog.getEditor().setPromptText("nom du système de repèrage");
        dialog.setTitle("Nouveau système de repèrage");
        dialog.setHeaderText("Nom du nouveau système de repèrage");

        final Optional<String> opt = dialog.showAndWait();
        if(!opt.isPresent() || opt.get().isEmpty()) return;


        final String srName = opt.get();
        final SystemeReperage sr = session.getRepositoryForClass(SystemeReperage.class).create();
        sr.setLibelle(srName);
        sr.setLinearId(troncon.getDocumentId());
        ((SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class)).add(sr, troncon);

        //maj de la liste des SR
        tronconChanged(tronconProperty(), troncon, troncon);

        //selection du SR
        uiSrComboBox.getSelectionModel().clearAndSelect(uiSrComboBox.getItems().indexOf(sr));
    }

    public void createBorne(Point geom){
        final TextInputDialog dialog = new TextInputDialog("");
        dialog.getEditor().setPromptText("borne ...");
        dialog.setTitle("Nouvelle borne");
        dialog.setGraphic(null);
        dialog.setHeaderText("Libellé de la nouvelle borne");

        final Optional<String> opt = dialog.showAndWait();
        if(!opt.isPresent() || opt.get().isEmpty()) return;

        //creation de la borne
        final String borneLbl = opt.get();
        final BorneDigue borne = session.getRepositoryForClass(BorneDigue.class).create();
        borne.setLibelle(borneLbl);
        borne.setGeometry(geom);
        session.getRepositoryForClass(BorneDigue.class).add(borne);
        TronconDigue tr = tronconProp.get();
        if (tr != null) {
            tr.getBorneIds().add(borne.getId());
        }
        
        createBorne(borne);
    }

    public void createBorne(BorneDigue borne) {
        final SystemeReperage sr = systemeReperageProperty().get();

        //on vérifie que la borne n'est pas deja dans la liste
        for(SystemeReperageBorne srb : sr.getSystemeReperageBornes()){
            if(borne.getDocumentId().equals(srb.borneIdProperty().get())){
                //la borne fait deja partie de ce SR
                return;
            }
        }

        //reference dans le SR
        final SystemeReperageBorne srb = Injector.getSession().getElementCreator().createElement(SystemeReperageBorne.class);
        srb.borneIdProperty().set(borne.getDocumentId());
        srb.valeurPRProperty().set(0);

        //sauvegarde du SR
        saveSR.set(sr.systemeReperageBornes.add(srb));
    }

    /**
     * Open a {@link ListView} to allow user to select one or more {@link BorneDigue}
     * to delete.
     *
     * Note : Once suppression is confirmed, we're forced to check all {@link SystemeReperage}
     * defined on the currently edited {@link TronconDigue}, and update them if
     * they use chosen bornes.
     * @param e Event fired when deletion button has been fired.
     */
    @FXML
    private void deleteBornes(ActionEvent e) {
        final ListView<BorneDigue> borneList = buildBorneList(null);
        if (borneList == null) return;

        final Stage stage = new Stage();
        stage.setTitle("Sélectionnez les bornes à supprimer");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(this.getScene().getWindow());

        final Separator blankSpace = new Separator();
        blankSpace.setVisible(false);

        final Button cancelButton = new Button("Annuler");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> stage.hide());
        final Button deleteButton = new Button("Supprimer");
        deleteButton.disableProperty().bind(borneList.getSelectionModel().selectedItemProperty().isNull());

        final HBox buttonBar = new HBox(10, blankSpace, cancelButton, deleteButton);
        buttonBar.setPadding(new Insets(5));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        final VBox content = new VBox(borneList, buttonBar);

        stage.setScene(new Scene(content));

        deleteButton.setOnAction(event -> {
            final BorneDigue[] selectedItems = borneList.getSelectionModel().getSelectedItems().toArray(new BorneDigue[0]);
            if (checkBorneSuppression(selectedItems)) {
                final TaskManager.MockTask deletor = new TaskManager.MockTask("Suppression de bornes", () -> {
                    InjectorCore.getBean(BorneDigueRepository.class).remove(selectedItems);
                });

                deletor.setOnSucceeded(evt -> Platform.runLater(() -> borneList.getItems().removeAll(selectedItems)));
                deletor.setOnFailed(evt -> Platform.runLater(() -> GeotkFX.newExceptionDialog("Une erreur est survenue lors de la suppression des bornes.", deletor.getException()).show()));
                content.disableProperty().bind(deletor.runningProperty());

                TaskManager.INSTANCE.submit(deletor);
            }
        });

        stage.show();
    }

    /**
     * Detect if any available SR would be emptied if input {@link BorneDigue}s
     * were deleted from database. If it's the case, we ask user to confirm his
     * will to remove them.
     * @param bornes Bornes to delete.
     * @return True if we can proceed to borne deletion, false if not.
     */
    public boolean checkBorneSuppression(final BorneDigue... bornes) {
        final HashSet<String> borneIds = new HashSet<>();
        for (final BorneDigue bd : bornes) {
            borneIds.add(bd.getId());
        }

        // Find all Sr which would be emptied by suppression of input bornes.
        FilteredList<SystemeReperage> emptiedSrs = uiSrComboBox.getItems().filtered(sr
                -> sr.systemeReperageBornes.filtered(srb -> !borneIds.contains(srb.getBorneId())).isEmpty()
        );

        if (emptiedSrs.isEmpty()) {
            return true;
        }

        final StringBuilder msg = new StringBuilder("La suppression des bornes séléctionnées va entièrement vider les systèmes de repérage suivants :");
        for (final SystemeReperage sr : emptiedSrs) {
            msg.append(System.lineSeparator()).append(uiSrComboBox.getConverter().toString(sr));
        }
        msg.append(System.lineSeparator()).append("Voulez-vous tout-de-même supprimer ces bornes ?");

        final Alert alert = new Alert(Alert.AlertType.WARNING, msg.toString(), ButtonType.NO, ButtonType.YES);
        alert.setResizable(true);
        return ButtonType.YES.equals(alert.showAndWait().orElse(ButtonType.NO));
    }

    /*
     * SR UTILITIES
     */

    /**
     * Delete the {@link SystemeReperage} selected in {@link #ui
     * @param evt
     */
    private void deleteSystemeReperage(ActionEvent evt) {
        final TronconDigue troncon = tronconProperty().get();
        if(troncon==null) return;

        SystemeReperage toDelete = uiSrComboBox.getValue();
        if (toDelete == null || toDelete.getId() == null) {
            return;
        }

        // We cannot delete default SR, because all PRs on the troncon are based on it.
        if (toDelete.getId().equals(troncon.getSystemeRepDefautId())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Impossible de supprimer le système de repérage par défaut.\n "
                    + "Avant de pouvoir supprimer le système courant, vous devez en sélectionner un autre comme système par défaut du tronçon.", ButtonType.OK);
            // Forced to do that because of linux bug.
            alert.setResizable(true);
            alert.setWidth(400);
            alert.setHeight(300);
            alert.showAndWait();

        } else {
            /*
             Before deleting SR, we propose user to choose another SR to update
             position of objects defined on SR to delete.
             */
            final SystemeReperage alternative;
            if (uiSrComboBox.getItems().size() > 1) {

                Alert alert = new Alert(Alert.AlertType.WARNING, "Les positions linéaires des structures définies sur ce système de repérage seront invalidées.\n"
                        + "Voulez-vous définir un SR pour mettre à jour la position linéaire des objets affectés ?"
                        + toDelete.getLibelle() + " ?", ButtonType.NO, ButtonType.YES);
                // Forced to do that because of linux bug.
                alert.setResizable(true);
                alert.setWidth(400);
                alert.setHeight(300);
                ButtonType response = alert.showAndWait().orElse(null);
                // User choose to use an alternative SR.
                if (ButtonType.YES.equals(response)) {
                    ObservableList<SystemeReperage> otherSRs = FXCollections.observableArrayList(uiSrComboBox.getItems());
                    otherSRs.remove(toDelete);
                    final ComboBox<SystemeReperage> chooser = new ComboBox();
                    SIRS.initCombo(chooser, otherSRs, otherSRs.get(0));
                    alert = new Alert(Alert.AlertType.NONE, null, ButtonType.CANCEL, ButtonType.YES);
                    alert.getDialogPane().setContent(chooser);
                    // Forced to do that because of linux bug.
                    alert.setResizable(true);
                    alert.setWidth(400);
                    alert.setHeight(300);
                    response = alert.showAndWait().orElse(ButtonType.CANCEL);
                    if (ButtonType.YES.equals(response)) {
                        alternative = chooser.getValue();
                    } else {
                        // User cancelled dialog.
                        return;
                    }
                } else if (ButtonType.NO.equals(response)) {
                    alternative = null;
                } else {
                    // User cancelled dialog.
                    return;
                }
            } else {
                alternative = null;
            }

            // Current selected SR will be removed, we don't need to update it.
            saveSR.set(false);
            InjectorCore.getBean(SystemeReperageRepository.class).remove(toDelete, troncon, alternative);
            uiSrComboBox.getItems().remove(toDelete);
            if (alternative != null) {
                uiSrComboBox.getSelectionModel().select(alternative);
            }
        }
    }

    private void tronconChanged(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
        if (oldValue != null) {
            save(uiSrComboBox.getValue(), oldValue);
        }

        if(newValue==null) {
            uiSrComboBox.setItems(FXCollections.emptyObservableList());
        } else {
            mode.set(Mode.NONE);
            final List<SystemeReperage> srs = ((SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class)).getByLinear(newValue);
            uiSrComboBox.setItems(FXCollections.observableArrayList(srs));

            final String defaultSRID = newValue.getSystemeRepDefautId();
            if (defaultSRID != null) {
                for (final SystemeReperage sr : srs) {
                    if (defaultSRID.equals(sr.getId())) {
                        uiSrComboBox.getSelectionModel().select(sr);
                        break;
                    }
                }
            }

            // In case default SR change from another panel
            newValue.systemeRepDefautIdProperty().addListener((ObservableValue<? extends String> srObservable, String oldSR, String newSR) -> {
                uiDefaultSRCheckBox.setSelected(newSR == null? false : newSR.equals(uiSrComboBox.getValue() == null? null : uiSrComboBox.getValue().getId()));
            });
        }
    }

    private void updateDefaultSRCheckBox(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue) {
        if (newValue != null && tronconProp.get() != null &&
                newValue.getId().equals(tronconProp.get().getSystemeRepDefautId())) {
            uiDefaultSRCheckBox.setSelected(true);
        } else {
            uiDefaultSRCheckBox.setSelected(false);
        }
    }

    private void updateTonconDefaultSR(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        TronconDigue td = tronconProp.get();
        final SystemeReperage selectedSR = uiSrComboBox.getSelectionModel().getSelectedItem();
        final String srid = selectedSR == null? null : selectedSR.getId();
        if (td != null && srid != null) {
            if (Boolean.TRUE.equals(newValue) && !srid.equals(td.getSystemeRepDefautId())) {
                td.setSystemeRepDefautId(srid);
                saveTD.set(true);
            } else if (Boolean.FALSE.equals(newValue) && srid.equals(td.getSystemeRepDefautId())) {
                td.setSystemeRepDefautId(null);
                saveTD.set(true);
            }
        }
    }

    /*
     * TABLE UTILITIES
     */

    public void sortBorneTable(){
        final TronconDigue troncon = tronconProp.get();
        if(troncon==null) return;

        final List lst = uiBorneTable.getItems();
        final LineString linear = LinearReferencingUtilities.asLineString(troncon.getGeometry());
        lst.sort(new SRBComparator(linear));
    }

    private void updateBorneTable(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue) {
        if (oldValue != null) {
            save(oldValue, null);
        }

        if (newValue == null) {
            uiBorneTable.setItems(FXCollections.emptyObservableList());
        } else {
            final Mode current = mode.get();
            if (current.equals(Mode.CREATE_BORNE) || current.equals(Mode.EDIT_BORNE)) {
                //do nothing
            } else {
                mode.set(Mode.EDIT_BORNE);
            }
            uiBorneTable.setItems(newValue.getSystemeReperageBornes());
            sortBorneTable();
        }
    }

    public class DeleteColumn extends SimpleButtonColumn<SystemeReperageBorne, SystemeReperageBorne> {

        public DeleteColumn() {
            super(GeotkFX.ICON_UNLINK,
                    (TableColumn.CellDataFeatures<SystemeReperageBorne, SystemeReperageBorne> param) -> new SimpleObjectProperty<>(param.getValue()),
                    (SystemeReperageBorne t) -> true,
                    new Function<SystemeReperageBorne, SystemeReperageBorne>() {

                        public SystemeReperageBorne apply(SystemeReperageBorne t) {
                            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression (seule l'association entre la borne et le système de repérage est brisée) ?",
                                    ButtonType.NO, ButtonType.YES);
                            alert.setResizable(true);
                            final ButtonType res = alert.showAndWait().get();
                            if (ButtonType.YES == res) {
                                saveSR.set(systemeReperageProperty().get().getSystemeReperageBornes().remove(t));
                            }
                            return null;
                        }
                    },
                    "Enlever du système de repérage"
            );
        }
    }

    public class NameColumn extends TableColumn<SystemeReperageBorne,SystemeReperageBorne>{

        public NameColumn() {
            super("Nom");
            setSortable(false);

            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SystemeReperageBorne, SystemeReperageBorne>, ObservableValue<SystemeReperageBorne>>() {
                @Override
                public ObservableValue<SystemeReperageBorne> call(TableColumn.CellDataFeatures<SystemeReperageBorne, SystemeReperageBorne> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });

            setCellFactory((TableColumn<SystemeReperageBorne, SystemeReperageBorne> param) -> {
                final FXTableCell<SystemeReperageBorne, SystemeReperageBorne> tableCell = new FXTableCell<SystemeReperageBorne, SystemeReperageBorne>() {

                    @Override
                    protected void updateItem(SystemeReperageBorne item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setGraphic(new ImageView(ReferenceTableCell.ICON_LINK));
                            setText(new SirsStringConverter().toString(item));
                        }
                    }

                };
                tableCell.setEditable(false);
                return tableCell;
            });
        }
    }

    public class PRColumn extends TableColumn<SystemeReperageBorne, Number>{

        public PRColumn() {
            super("PR");
            setSortable(false);
            setEditable(true);

            setCellValueFactory(new Callback<CellDataFeatures<SystemeReperageBorne, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(CellDataFeatures<SystemeReperageBorne, Number> param) {
                    return (ObservableValue)param.getValue().valeurPRProperty();
                }
            });

            setCellFactory(new Callback<TableColumn<SystemeReperageBorne, Number>, TableCell<SystemeReperageBorne, Number>>() {
                @Override
                public TableCell<SystemeReperageBorne, Number> call(TableColumn<SystemeReperageBorne, Number> param) {
                    return new FXNumberCell<SystemeReperageBorne>(Double.class);
                }
            });

            addEventHandler(TableColumn.editCommitEvent(), (TableColumn.CellEditEvent<SystemeReperageBorne, Object> event) -> {
                final SystemeReperageBorne srb = event.getRowValue();
                srb.setValeurPR(((Number)event.getNewValue()).floatValue());
                saveSR.set(true);
            });
        }
    }


}
