package fr.sirs.digue;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.theme.AbstractPojoTable;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import java.time.LocalDateTime;
import java.util.List;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import jidefx.scene.control.field.LocalDateTimeField;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DigueController extends BorderPane {
    
    private final ObjectProperty<Digue> digueProperty = new SimpleObjectProperty<>();
    private ObservableList<TronconDigue> troncons;
    
    @Autowired
    private Session session;

    @FXML private TextField libelle;
    @FXML private Label id;
    @FXML private FXDateField date_maj;
    @FXML private HTMLEditor uiComment;
    @FXML private ToggleButton uiEdit;
    @FXML private ToggleButton uiConsult;
    @FXML private Button uiSave;

    private final TronconPojoTable table = new TronconPojoTable();

    public DigueController() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        
        //mode edition
        final BooleanBinding editBind = uiEdit.selectedProperty().not();
        uiSave.disableProperty().bind(editBind);
        libelle.disableProperty().bind(editBind);
        uiComment.disableProperty().bind(editBind);
        
        digueProperty.addListener((ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) -> {
            initFields();
        });
        
        final ToggleGroup group = new ToggleGroup();
        uiConsult.setToggleGroup(group);
        uiEdit.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null) group.selectToggle(uiConsult);
        });
    }
    
    public ObjectProperty<Digue> tronconProperty(){
        return digueProperty;
    }
    
    public Digue getDigue(){
        return digueProperty.get();
    }
    
    public void setDigue(Digue digue){
        this.digueProperty.set(digue);
        initFields();
    }
    
    @FXML
    private void save(ActionEvent event){
        digueProperty.get().setCommentaire(uiComment.getHtmlText());
        this.session.update(this.digueProperty.get());
        this.session.update(this.troncons);
    }
    
    private void reloadTroncons(){
        
        final List<TronconDigue> items = session.getTronconDigueByDigue(digueProperty.get());
        this.troncons = FXCollections.observableArrayList();
        items.stream().forEach((item) -> {
            this.troncons.add(item);
        });
    }

    /**
     * 
     */
    public void initFields() {
        this.setCenter(table);
        
        // Binding levee's name.------------------------------------------------
        this.libelle.textProperty().bindBidirectional(digueProperty.get().libelleProperty());
        this.libelle.editableProperty().bindBidirectional(this.uiEdit.selectedProperty());
        
        // Display levee's id.--------------------------------------------------
        this.id.setText(this.digueProperty.get().getId());
        
        // Display levee's update date.-----------------------------------------
        this.date_maj.setValue(this.digueProperty.get().getDateMaj());
        this.date_maj.setDisable(true);
        this.date_maj.valueProperty().bindBidirectional(this.digueProperty.get().dateMajProperty());

        // Binding levee's comment.---------------------------------------------
        this.uiComment.setHtmlText(digueProperty.get().getCommentaire());
        
        table.updateTable();
        
    }
    
    private class TronconPojoTable extends AbstractPojoTable {
    
            public TronconPojoTable() {
            super(TronconDigue.class,"Liste des tronçons");

            final ChangeListener listener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
                updateTable();
            };

            digueProperty.addListener(listener);
        }

        private void updateTable() {
            reloadTroncons();
            if (troncons == null) {
                uiTable.setItems(FXCollections.emptyObservableList());
            } else {
            //JavaFX bug : sortable is not possible on filtered list
                // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
                // https://javafx-jira.kenai.com/browse/RT-32091
                final SortedList sortedList = new SortedList(troncons);
                uiTable.setItems(sortedList);
                sortedList.comparatorProperty().bind(uiTable.comparatorProperty());
            }
        }
    
        @Override
        protected void deletePojos(Element ... pojos) {
            for(Element pojo : pojos){
                session.delete(((TronconDigue) pojo));
            }
            updateTable();
        }

        @Override
        protected void editPojo(Element pojo) {
            final Session session = Injector.getBean(Session.class);
//            final Tab tab = new Tab();
//            final TronconDigueController tronconDigueController = new TronconDigueController();
//            tronconDigueController.setTroncon((TronconDigue) pojo);
//            tab.setContent(tronconDigueController);
//            tab.setText(pojo.getClass().getSimpleName());
//            tab.setOnSelectionChanged(new EventHandler<Event>() {
//                @Override
//                public void handle(Event event) {
//                    if(tab.isSelected()){
//                        session.prepareToPrint(pojo);
//                    }
//                }
//            });
//            session.getFrame().addTab(tab);
            session.getFrame().getDiguesTab().getDiguesController().displayTronconDigue((TronconDigue) pojo);
            session.prepareToPrint(pojo);
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            session.update((TronconDigue) event.getRowValue());
        }

        @Override
        protected void createPojo() {
            TronconDigue troncon = new TronconDigue();
            troncon.setDigueId(digueProperty.get().getId());
            session.add(troncon);
            System.out.println("Id du nouveau tronçon : "+troncon.getId());
            troncons.add(troncon);
            updateTable();
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    
    

    public static class CustomizedFXLocalDateTimeCell<S> extends TableCell<S, Object> {

        private final CustomizedFXDateField field = new CustomizedFXDateField();
        private boolean secondAction;

        public CustomizedFXLocalDateTimeCell() {
            setGraphic(field);
            setAlignment(Pos.CENTER);
            setContentDisplay(ContentDisplay.CENTER);

            this.secondAction = false;

            field.setOnAction((ActionEvent event) -> {
                if (secondAction) {
                    commitEdit(field.getValue());
                } 
                secondAction = !secondAction;
            });
        }

        @Override
        public void startEdit() {
            LocalDateTime time = (LocalDateTime) getItem();
            if (time == null) {
                time = LocalDateTime.now();
            }
            field.setValue(time);
            super.startEdit();
            setText(null);
            setGraphic(field);
            field.requestFocus();
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);
            if (item != null) {
                setText(((LocalDateTime) item).toString());
            }
        }
    }

    public static class CustomizedFXDateField extends LocalDateTimeField {

        private boolean getValueFirstCall = true;

        public CustomizedFXDateField() {

            super("yyyy-MM-dd HH:mm:ss");

            this.setAutoAdvance(false);
            this.setAutoReformat(false);
            this.setAutoSelectAll(true);
            this.setPopupButtonVisible(true);

        }

        /**
         * Overriden to support null values.
         *
         * @return
         */
        @Override
        public LocalDateTime getValue() {
            String text = getText();
            LocalDateTime value = super.getValue();

            if ("-- ::".equals(text)) {
                if (value == null && getValueFirstCall) {
                    super.setValue(LocalDateTime.now());
                } else {
                    super.setValue(null);
                }
            }

            getValueFirstCall = false;

            return super.getValue();
        }

        @Override
        public void replaceText(int start, int end, String text) {
            if (text.length() >= 1) {
                // Change behavior, avoid erasing all text when replacing a full group
                String existingText = getText();
                int newEnd = Math.max(0, Math.min(end, existingText.length()));
                String deletedText = existingText.substring(start, newEnd);
                if (deletedText.length() > text.length()) {
                    end = start + text.length();
                }
            }
            super.replaceText(start, end, text);
        }
    }
}