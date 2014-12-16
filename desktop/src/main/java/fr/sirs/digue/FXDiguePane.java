package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import static fr.sirs.Session.Role.ADMIN;
import static fr.sirs.Session.Role.EXTERNE;
import static fr.sirs.Session.Role.USER;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.FXElementPane;
import java.time.LocalDateTime;
import java.util.List;
import javafx.beans.property.BooleanProperty;
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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import jidefx.scene.control.field.LocalDateTimeField;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDiguePane extends BorderPane implements FXElementPane {
    
    private final ObjectProperty<Digue> digueProperty = new SimpleObjectProperty<>();
    private ObservableList<TronconDigue> troncons;
    
    @Autowired private Session session;

    @FXML private TextField libelle;
    @FXML private Label id;
    @FXML private FXDateField date_maj;
    @FXML private HTMLEditor uiComment;
    
    @FXML private FXEditMode uiMode;

    private final TronconPojoTable table = new TronconPojoTable();

    public FXDiguePane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        
        //mode edition
        uiMode.setAllowedRoles(ADMIN, USER, EXTERNE);
        uiMode.setSaveAction(this::save);
        final BooleanProperty editBind = uiMode.editionState();
        libelle.editableProperty().bind(editBind);
        uiComment.disableProperty().bind(editBind.not());
        table.editableProperty().bind(editBind);
        
        digueProperty.addListener((ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) -> {
            initFields();
        });
    }
    
    public ObjectProperty<Digue> tronconProperty(){
        return digueProperty;
    }
    
    public Digue getDigue(){
        return digueProperty.get();
    }
    
    @Override
    public void setElement(Element digue){
        this.digueProperty.set((Digue) digue);
        initFields();
    }
    
    private void save(){
        digueProperty.get().setCommentaire(uiComment.getHtmlText());
        session.update(this.digueProperty.get());
        session.update(this.troncons);
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
//        final BooleanProperty editBind = uiMode.editionState();
        
        // Binding levee's name.------------------------------------------------
        this.libelle.textProperty().bindBidirectional(digueProperty.get().libelleProperty());
//        this.libelle.editableProperty().bindBidirectional(editBind);
        
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
    
    private class TronconPojoTable extends PojoTable {
    
        public TronconPojoTable() {
            super(TronconDigue.class, "Liste des tronçons", true);

            final ChangeListener listener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
                updateTable();
            };

            digueProperty.addListener(listener);
        }

        private void updateTable() {
            reloadTroncons();
            if (troncons == null) {
                setTableItems(FXCollections::emptyObservableList);
            } else {
                //JavaFX bug : sortable is not possible on filtered list
                // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
                // https://javafx-jira.kenai.com/browse/RT-32091
                final SortedList sortedList = new SortedList(troncons);
                setTableItems(()->sortedList);
                sortedList.comparatorProperty().bind(getUiTable().comparatorProperty());
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
        protected void editPojo(Object pojo) {
            session.getFrame().getDiguesTab().getDiguesController().displayTronconDigue((TronconDigue) pojo);
            session.prepareToPrint(pojo);
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            session.update((TronconDigue) event.getRowValue());
        }

        @Override
        protected Object createPojo() {
            TronconDigue troncon = new TronconDigue();
            troncon.setDigueId(digueProperty.get().getId());
            session.add(troncon);
            System.out.println("Id du nouveau tronçon : "+troncon.getId());
            troncons.add(troncon);
            updateTable();
            return troncon;
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