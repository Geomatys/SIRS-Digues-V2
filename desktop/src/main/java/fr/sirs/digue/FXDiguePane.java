package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import static fr.sirs.Role.ADMIN;
import static fr.sirs.Role.EXTERNE;
import static fr.sirs.Role.USER;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.AbstractFXElementPane;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.web.HTMLEditor;
import jidefx.scene.control.field.LocalDateTimeField;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDiguePane extends AbstractFXElementPane<Digue> {
    
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
        
        elementProperty.addListener((ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) -> {
            initFields();
        });
    }
    
    public ObjectProperty<Digue> tronconProperty(){
        return elementProperty;
    }
    
    public Digue getDigue(){
        return elementProperty.get();
    }
    
    private void save(){
        elementProperty.get().setCommentaire(uiComment.getHtmlText());
        session.update(this.elementProperty.get());
        session.update(this.troncons);
    }
    
    private void reloadTroncons(){
        
        final List<TronconDigue> items = session.getTronconDigueByDigue(elementProperty.get());
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
        this.libelle.textProperty().bindBidirectional(elementProperty.get().libelleProperty());
//        this.libelle.editableProperty().bindBidirectional(editBind);
        
        // Display levee's id.--------------------------------------------------
        this.id.setText(this.elementProperty.get().getId());
        
        // Display levee's update date.-----------------------------------------
        this.date_maj.setValue(this.elementProperty.get().getDateMaj());
        this.date_maj.setDisable(true);
        this.date_maj.valueProperty().bindBidirectional(this.elementProperty.get().dateMajProperty());

        // Binding levee's comment.---------------------------------------------
        this.uiComment.setHtmlText(elementProperty.get().getCommentaire());
        
        table.updateTable();
    }

    @Override
    public void preSave() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private class TronconPojoTable extends PojoTable {
    
        public TronconPojoTable() {
            super(TronconDigue.class, "Liste des tronçons");

            final ChangeListener listener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
                updateTable();
            };

            elementProperty.addListener(listener);
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
            troncon.setDigueId(elementProperty.get().getId());
            session.add(troncon);
            SIRS.LOGGER.log(Level.FINE, "Id du nouveau tronçon : "+troncon.getId());
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