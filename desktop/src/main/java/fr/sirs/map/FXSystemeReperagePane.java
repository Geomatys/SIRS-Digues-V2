
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.SirsTableCell;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import jidefx.scene.control.field.NumberField;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.internal.GeotkFX;

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
    @FXML private Button uiAddSr;
    @FXML private TableView<SystemeReperageBorne> uiBorneTable;
    @FXML private ToggleButton uiCreateBorne;
    @FXML private Button uiCalculatePr;

    private final ObjectProperty<TronconDigue> tronconProp = new SimpleObjectProperty<>();
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.NONE);
    private final Session session;
    
    public FXSystemeReperagePane() {
        SIRS.loadFXML(this);
        session = Injector.getSession();
        
        uiPickTroncon.setGraphic(new ImageView(SIRS.ICON_CROSSHAIR_BLACK));
        uiAddSr.setGraphic(new ImageView(SIRS.ICON_ADD_BLACK));
        uiCalculatePr.setDisable(true);
        
        //on active le choix du sr si un troncon est sélectionné
        final BooleanBinding srEditBinding = tronconProp.isNull();
        uiSrComboBox.disableProperty().bind(srEditBinding);
        uiSrComboBox.setConverter(new SirsStringConverter());
        uiSrComboBox.valueProperty().addListener(this::updateBorneList);
        uiAddSr.disableProperty().bind(srEditBinding);
        
        //on active la table et bouton de creation si un sr est sélectionné
        final BooleanBinding borneEditBinding = uiSrComboBox.valueProperty().isNull();
        uiBorneTable.disableProperty().bind(borneEditBinding);
        uiCreateBorne.disableProperty().bind(borneEditBinding);
        
        //on active le calcule de PR uniquement si 2 bornes sont sélectionnées
        uiBorneTable.setEditable(true);
        uiBorneTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiBorneTable.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<SystemeReperageBorne>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends SystemeReperageBorne> c) {
                final int size = uiBorneTable.getSelectionModel().getSelectedItems().size();
                uiCalculatePr.setDisable(size!=2);
            }
        });
        
        
        uiPickTroncon.setOnAction(this::startPickTroncon);
        uiCreateBorne.setOnAction(this::startCreateBorne);
        uiAddSr.setOnAction(this::createSystemeReperage);
        
        //liste des SR sur changement de troncon
        tronconProp.addListener(this::updateSrList);
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
        final TableColumn<SystemeReperageBorne,Object> prCol = new PRColumn();
        
        uiBorneTable.getColumns().add(deleteCol);
        uiBorneTable.getColumns().add(nameCol);
        uiBorneTable.getColumns().add(prCol);
                
    }

    public ObjectProperty<Mode> modeProperty(){
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
    
    public void save(){
        final TronconDigue troncon = tronconProperty().get();
        if(troncon!=null){
            final Session session = Injector.getSession();
            session.getTronconDigueRepository().update(troncon);
        }
    }
    
    private void startPickTroncon(ActionEvent evt){
        mode.set(Mode.PICK_TRONCON);
    }
    
    private void startCreateBorne(ActionEvent evt){
        mode.set(Mode.CREATE_BORNE);
    }
    
    private void createSystemeReperage(ActionEvent evt){
        final TronconDigue troncon = tronconProperty().get();
        if(troncon==null) return;
        
        final TextInputDialog dialog = new TextInputDialog("Nom du SR");
        dialog.getEditor().setPromptText("nom du système de repèrage");
        dialog.setTitle("Nouveau système de repèrage");
        dialog.setGraphic(null);
        dialog.setHeaderText("Nom du nouveau système de repèrage");
        
        final Optional<String> opt = dialog.showAndWait();
        if(!opt.isPresent() || opt.get().isEmpty()) return;
        
        
        final String srName = opt.get();
        final SystemeReperage sr = session.getSystemeReperageRepository().create();
        sr.setLibelle(srName);
        sr.setTronconId(troncon.getDocumentId());
        session.getSystemeReperageRepository().add(sr);
        
        //maj de la liste des SR
        updateSrList(null, null, null);
        
        //selection du SR
        uiSrComboBox.getSelectionModel().select(sr);
        
    }
    
    private void updateSrList(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue){
        final TronconDigue troncon = tronconProperty().get();
        if(troncon==null){
            uiSrComboBox.setItems(null);
        }else{
            modeProperty().set(Mode.NONE);
            final List<SystemeReperage> srs = session.getSystemeReperageRepository().getByTroncon(troncon);
            uiSrComboBox.setItems(FXCollections.observableArrayList(srs));
        }
    }
    
    private void updateBorneList(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue){
        final SystemeReperage sr = uiSrComboBox.getValue();
        if(sr==null){
            uiBorneTable.setItems(FXCollections.emptyObservableList());
        }else{
            modeProperty().set(Mode.EDIT_BORNE);
            final ObservableList bornes = FXCollections.observableList(sr.getSystemereperageborneId());
            uiBorneTable.setItems(bornes);
        }
    }
    
    public class DeleteColumn extends TableColumn<SystemeReperageBorne,SystemeReperageBorne>{

        public DeleteColumn() {
            super();            
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DELETE));
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SystemeReperageBorne, SystemeReperageBorne>, ObservableValue<SystemeReperageBorne>>() {
                @Override
                public ObservableValue<SystemeReperageBorne> call(TableColumn.CellDataFeatures<SystemeReperageBorne, SystemeReperageBorne> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            setCellFactory((TableColumn<SystemeReperageBorne, SystemeReperageBorne> param) -> new ButtonTableCell<>(
                    false,new ImageView(GeotkFX.ICON_DELETE), (SystemeReperageBorne t) -> true, new Function<SystemeReperageBorne, SystemeReperageBorne>() {
                @Override
                public SystemeReperageBorne apply(SystemeReperageBorne t) {
                    final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?", 
                            ButtonType.NO, ButtonType.YES).showAndWait().get();
                    if(ButtonType.YES == res){
                        final SystemeReperage sr = systemeReperageProperty().get();
                        sr.getSystemereperageborneId().remove(t);
                    }
                    return null;
                }
            }));
        }  
    }
    
    public class NameColumn extends TableColumn<SystemeReperageBorne,SystemeReperageBorne>{

        public NameColumn() {
            super("Nom");
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SystemeReperageBorne, SystemeReperageBorne>, ObservableValue<SystemeReperageBorne>>() {
                @Override
                public ObservableValue<SystemeReperageBorne> call(TableColumn.CellDataFeatures<SystemeReperageBorne, SystemeReperageBorne> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            
            setCellFactory(new Callback<TableColumn<SystemeReperageBorne, SystemeReperageBorne>, TableCell<SystemeReperageBorne, SystemeReperageBorne>>() {
                @Override
                public TableCell<SystemeReperageBorne, SystemeReperageBorne> call(TableColumn<SystemeReperageBorne, SystemeReperageBorne> param) {
                    return new SirsTableCell();
                }
            });
        }
    }
    
    public class PRColumn extends TableColumn<SystemeReperageBorne,Object>{

        public PRColumn() {
            super("PR");
            
            setEditable(true);
            
            setCellValueFactory(new Callback<CellDataFeatures<SystemeReperageBorne, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(CellDataFeatures<SystemeReperageBorne, Object> param) {
                    return (ObservableValue)param.getValue().valeurPRProperty();
                }
            });
            
            setCellFactory(new Callback<TableColumn<SystemeReperageBorne, Object>, TableCell<SystemeReperageBorne, Object>>() {
                @Override
                public TableCell<SystemeReperageBorne, Object> call(TableColumn<SystemeReperageBorne, Object> param) {
                    return new FXNumberCell<SystemeReperageBorne>(NumberField.NumberType.Normal);
                }
            });
            
        }
    }
    
    
}
