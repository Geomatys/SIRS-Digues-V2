package fr.sirs.theme.ui;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractTronconDigueRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.ui.Growl;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import org.geotoolkit.internal.GeotkFX;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDiguePane extends AbstractFXElementPane<Digue> {
       
    @Autowired private Session session;

    @FXML private TextField libelle;
    @FXML private TextField uiDesignation;
    @FXML private DatePicker date_maj;
    @FXML private ComboBox uiSystemeEndiguementId;
    @FXML private HTMLEditor uiComment;
    
    @FXML private VBox centerContent;
    @FXML private FXEditMode uiMode;

    private final TronconPojoTable table = new TronconPojoTable();
    private final Previews previewRepository;

    public FXDiguePane(final Digue digue) {
        SIRS.loadFXML(this, Digue.class);
        Injector.injectDependencies(this);
        
        previewRepository = Injector.getBean(Session.class).getPreviews();
        //mode edition
        uiMode.setSaveAction(this::save);
        uiMode.requireEditionForElement(digue);
        disableFieldsProperty().bind(uiMode.editionState().not());
        
        libelle.disableProperty().bind(disableFieldsProperty());
        uiDesignation.disableProperty().bind(disableFieldsProperty());
        uiComment.disableProperty().bind(disableFieldsProperty());
        table.editableProperty().bind(disableFieldsProperty().not());
        uiSystemeEndiguementId.disableProperty().bind(disableFieldsProperty());
        
        date_maj.setDisable(true);
        
        table.parentElementProperty().bind(elementProperty);
        elementProperty.addListener(this::initFields);
        setElement(digue);
        
        centerContent.getChildren().add(table);
    }
    
    public Digue getDigue() {
        return elementProperty.get();
    }
    
    private void save() {
        try {
            preSave();
            session.getRepositoryForClass(Digue.class).update(this.elementProperty.get());
            final Growl growlInfo = new Growl(Growl.Type.INFO, "Enregistrement effectué.");
            growlInfo.showAndFade();
        } catch (Exception e) {
            final Growl growlError = new Growl(Growl.Type.ERROR, "Erreur survenue pendant l'enregistrement.");
            growlError.showAndFade();
            GeotkFX.newExceptionDialog("L'élément ne peut être sauvegardé.", e).show();
            SIRS.LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * 
     */
    public void initFields(ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) {
        uiMode.validProperty().unbind();
        uiMode.authorIDProperty().unbind();
        this.uiComment.setHtmlText(null);
        table.setTableItems(()->null);
        
        if (oldValue != null) {
            this.libelle.textProperty().unbindBidirectional(oldValue.libelleProperty());
            this.uiDesignation.textProperty().unbindBidirectional(oldValue.designationProperty());
            this.date_maj.valueProperty().unbindBidirectional(oldValue.dateMajProperty());
        }
        
        if (newValue != null) {
            // Binding digue name.----------------------------------------------
            this.libelle.textProperty().bindBidirectional(newValue.libelleProperty());

            // Binding digue name.----------------------------------------------
            this.uiDesignation.textProperty().bindBidirectional(newValue.designationProperty());

            // Display levee's update date.-------------------------------------
            this.date_maj.valueProperty().bindBidirectional(newValue.dateMajProperty());

            // Initialize comment editor.---------------------------------------
            this.uiComment.setHtmlText(newValue.getCommentaire());

            uiMode.validProperty().bind(newValue.validProperty());
            uiMode.authorIDProperty().bind(newValue.authorProperty());
            
            table.setTableItems(()->FXCollections.observableArrayList(((AbstractTronconDigueRepository) session.getRepositoryForClass(TronconDigue.class)).getByDigue(newValue)));
            
            SIRS.initCombo(uiSystemeEndiguementId, FXCollections.observableArrayList(
                    previewRepository.getByClass(SystemeEndiguement.class)),
                    newValue.getSystemeEndiguementId()== null ? null : previewRepository.get(newValue.getSystemeEndiguementId()));
        }
    }

    @Override
    public void preSave() {
        final Digue digue = elementProperty.get();
        if (digue != null) {
            digue.commentaireProperty().set(uiComment.getHtmlText());
            
            Object cbValue;
            cbValue = uiSystemeEndiguementId.getValue();
            if (cbValue instanceof Preview) {
                digue.setSystemeEndiguementId(((Preview)cbValue).getElementId());
            } else if (cbValue instanceof Element) {
                digue.setSystemeEndiguementId(((Element)cbValue).getId());
            } else if (cbValue == null) {
                digue.setSystemeEndiguementId(null);
            }
        }
    }
    
    private class TronconPojoTable extends PojoTable {
    
        public TronconPojoTable() {
            super(TronconDigue.class, "Tronçons de la digue");
        }

        @Override
        protected TronconDigue createPojo() {
            TronconDigue createdPojo = (TronconDigue) super.createPojo();
            if (elementProperty.get() != null) {
                ((TronconDigue)createdPojo).setDigueId(elementProperty.get().getId());
            }
            return createdPojo;
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    
    

//    public static class CustomizedFXLocalDateTimeCell<S> extends TableCell<S, Object> {
//
//        private final CustomizedFXDateField field = new CustomizedFXDateField();
//        private boolean secondAction;
//
//        public CustomizedFXLocalDateTimeCell() {
//            setGraphic(field);
//            setAlignment(Pos.CENTER);
//            setContentDisplay(ContentDisplay.CENTER);
//
//            this.secondAction = false;
//
//            field.setOnAction((ActionEvent event) -> {
//                if (secondAction) {
//                    commitEdit(field.getValue());
//                }
//                secondAction = !secondAction;
//            });
//        }
//
//        @Override
//        public void startEdit() {
//            LocalDateTime time = (LocalDateTime) getItem();
//            if (time == null) {
//                time = LocalDateTime.now();
//            }
//            field.setValue(time);
//            super.startEdit();
//            setText(null);
//            setGraphic(field);
//            field.requestFocus();
//        }
//
//        @Override
//        protected void updateItem(Object item, boolean empty) {
//            super.updateItem(item, empty);
//            setText(null);
//            setGraphic(null);
//            if (item != null) {
//                setText(((LocalDateTime) item).toString());
//            }
//        }
//    }
//
//    public static class CustomizedFXDateField extends LocalDateTimeField {
//
//        private boolean getValueFirstCall = true;
//
//        public CustomizedFXDateField() {
//
//            super("yyyy-MM-dd HH:mm:ss");
//
//            this.setAutoAdvance(false);
//            this.setAutoReformat(false);
//            this.setAutoSelectAll(true);
//            this.setPopupButtonVisible(true);
//
//        }
//
//        /**
//         * Overriden to support null values.
//         *
//         * @return
//         */
//        @Override
//        public LocalDateTime getValue() {
//            String text = getText();
//            LocalDateTime value = super.getValue();
//
//            if ("-- ::".equals(text)) {
//                if (value == null && getValueFirstCall) {
//                    super.setValue(LocalDateTime.now());
//                } else {
//                    super.setValue(null);
//                }
//            }
//
//            getValueFirstCall = false;
//
//            return super.getValue();
//        }
//
//        @Override
//        public void replaceText(int start, int end, String text) {
//            if (text.length() >= 1) {
//                // Change behavior, avoid erasing all text when replacing a full group
//                String existingText = getText();
//                int newEnd = Math.max(0, Math.min(end, existingText.length()));
//                String deletedText = existingText.substring(start, newEnd);
//                if (deletedText.length() > text.length()) {
//                    end = start + text.length();
//                }
//            }
//            super.replaceText(start, end, text);
//        }
//    }
}
