
package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import static fr.sirs.Role.ADMIN;
import static fr.sirs.Role.EXTERNE;
import static fr.sirs.Role.USER;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import javafx.util.Callback;
import jidefx.scene.control.field.NumberField;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.geotoolkit.gui.javafx.util.FXNumberSpinner;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSystemeEndiguementPane extends BorderPane {

    private final Session session = Injector.getSession();
    
    @FXML private FXEditMode uiEditMode;
    @FXML private TextField uiLibelle;
    @FXML private Tab uiTabDigues;
    @FXML private FXDateField uiMaj;
    @FXML private TextField uiClassement;
    @FXML private FXNumberSpinner uiProtection;
    @FXML private FXNumberSpinner uiPopulation;
    @FXML private ComboBox<PreviewLabel> uiDecret;
    @FXML private ComboBox<PreviewLabel> uiTechnique;
    @FXML private HTMLEditor uiComment;
    
    private final DigueTable uiDigueTable = new DigueTable(Digue.class, "Liste des digues");
    
    private final ObjectProperty<SystemeEndiguement> endiguementProp = new SimpleObjectProperty<>();
    
    public FXSystemeEndiguementPane() {
        SIRS.loadFXML(this);
        endiguementProp.addListener(this::changed);
        uiEditMode.setAllowedRoles(ADMIN, USER, EXTERNE);
        uiEditMode.setSaveAction(this::save);
        
        uiMaj.disableProperty().set(true);
        final BooleanBinding binding = uiEditMode.editionState().not();
        uiLibelle.disableProperty().bind(binding);
        uiClassement.disableProperty().bind(binding);
        uiComment.disableProperty().bind(binding);
        uiProtection.disableProperty().bind(binding);
        uiPopulation.disableProperty().bind(binding);
        uiDecret.disableProperty().bind(binding);
        uiTechnique.disableProperty().bind(binding);
        
        uiProtection.numberTypeProperty().set(NumberField.NumberType.Normal);
        uiProtection.minValueProperty().set(0);

        uiPopulation.numberTypeProperty().set(NumberField.NumberType.Integer);
        uiPopulation.minValueProperty().set(0);
        
        uiTabDigues.setContent(uiDigueTable);
        uiDigueTable.editableProperty().bind(uiEditMode.editionState());
    }
    
    private void initCombo(ComboBox comboBox, final ObservableList items, final String currentId) {
        comboBox.setItems(items);
        if (currentId != null && !currentId.isEmpty()) {
            final Iterator<PreviewLabel> it = items.iterator();
            while (it.hasNext()) {
                final PreviewLabel e = it.next();
                if (e.getObjectId().equals(currentId)) {
                    comboBox.getSelectionModel().select(e);
                    break;
                }
            }
        }
        new ComboBoxCompletion(comboBox);
        comboBox.setConverter(new SirsStringConverter());
    }
    
    private void changed(ObservableValue<? extends SystemeEndiguement> observable, SystemeEndiguement oldValue, SystemeEndiguement newValue) {
        uiLibelle.textProperty().unbind();
        if(newValue!=null){
            uiLibelle.textProperty().bindBidirectional(newValue.libelleProperty());
            uiMaj.valueProperty().bindBidirectional(newValue.dateMajProperty());
            uiClassement.textProperty().bindBidirectional(newValue.classementProperty());
            uiProtection.valueProperty().bindBidirectional(newValue.niveauProtectionProperty());
            uiPopulation.valueProperty().bindBidirectional(newValue.populationProtegeeProperty());
            uiComment.setHtmlText(newValue.getCommentaire());
            initCombo(uiDecret, FXCollections.observableArrayList(session.getPreviewLabelRepository().getPreviewLabels(Organisme.class)), newValue.getGestionnaireDecret());
            initCombo(uiTechnique, FXCollections.observableArrayList(session.getPreviewLabelRepository().getPreviewLabels(Organisme.class)), newValue.getGestionnaireTechnique());
            uiDigueTable.updateTable();
        }
    }

    public ObjectProperty<SystemeEndiguement> systemeEndiguementProp() {
        return endiguementProp;
    }

    private void save(){
        endiguementProp.get().setCommentaire(uiComment.getHtmlText());
        endiguementProp.get().setGestionnaireDecret(uiDecret.getValue().getObjectId());
        endiguementProp.get().setGestionnaireTechnique(uiTechnique.getValue().getObjectId());
        endiguementProp.get().setDateMaj(LocalDateTime.now());
        session.getSystemeEndiguementRepository().update(systemeEndiguementProp().get());
    }

    private class DigueTable extends PojoTable {

        public DigueTable(Class pojoClass, String title) {
            super(pojoClass, title);
        }

        @Override
        protected ObservableList<Element> getAllValues() {
            final SystemeEndiguement sd = endiguementProp.get();
            if(sd==null) return FXCollections.emptyObservableList();
            
            final DigueRepository digueRepository = session.getDigueRepository();
            final List<String> digueIds = sd.getDigue();
            final ObservableList<Element> digues = FXCollections.observableArrayList();
            for(String id : digueIds){
                final Digue digue = digueRepository.get(id);
                digues.add(digue);
            }
            return digues;
        }
        
        public void updateTable(){
            final TableView table = getUiTable();
            table.setItems(getAllValues());
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            final DigueRepository digueRepository = session.getDigueRepository();            
            final Element digue = event.getRowValue();
            digueRepository.update((Digue)digue);
        }

        @Override
        protected void deletePojos(Element... pojos) {
            final SystemeEndiguement sd = endiguementProp.get();
            
            for(Element element : pojos){
                final Digue d = (Digue)element;
                sd.getDigue().remove(d.getDocumentId());
            }
            
            updateTable();
        }

        @Override
        protected Object createPojo() {
            
            final ObservableList<Digue> digues = FXCollections.observableList(session.getDigues());
            //on retire les digues que l'on a deja
            digues.removeAll(getAllValues());
            
            final ListView<Digue> lst = new ListView<>(digues);
            lst.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            lst.setCellFactory(new Callback<ListView<Digue>, ListCell<Digue>>() {
                @Override
                public ListCell<Digue> call(ListView<Digue> param) {
                    return new ListCell(){
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(null);
                            if(!empty && item!=null){
                                setText(((Digue)item).getLibelle());
                            }else{                            
                                setText("");
                            }
                        }
                    };
                }
            });
            
            final Dialog dialog = new Dialog();
            final DialogPane pane = new DialogPane();
            pane.setContent(lst);
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.setDialogPane(pane);
            dialog.setTitle("Ajouter une digue");
            
            final Optional opt = dialog.showAndWait();
            if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
                final Digue digue = lst.getSelectionModel().getSelectedItem();
                if(digue!=null){
                    final SystemeEndiguement sd = endiguementProp.get();
                    sd.getDigue().add(digue.getDocumentId());
                    updateTable();
                    return digue;
                }
            }
            return null;
        }
    }
    
}
