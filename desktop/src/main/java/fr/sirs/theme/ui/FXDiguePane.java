package fr.sirs.theme.ui;

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
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDiguePane extends AbstractFXElementPane<Digue> {
       
    @Autowired private Session session;

    @FXML private TextField libelle;
    @FXML private ComboBox uiSystemeEndiguementId;
    @FXML private HTMLEditor uiComment;
    
    @FXML private VBox centerContent;

    private final TronconPojoTable table = new TronconPojoTable();
    private final Previews previewRepository;

    public FXDiguePane(final Digue digue) {
        SIRS.loadFXML(this, Digue.class);
        Injector.injectDependencies(this);
        
        previewRepository = Injector.getBean(Session.class).getPreviews();
        
        libelle.disableProperty().bind(disableFieldsProperty());
        uiComment.disableProperty().bind(disableFieldsProperty());
        table.editableProperty().bind(disableFieldsProperty().not());
        uiSystemeEndiguementId.disableProperty().bind(disableFieldsProperty());
        
        table.parentElementProperty().bind(elementProperty);
        elementProperty.addListener(this::initFields);
        setElement(digue);
        
        centerContent.getChildren().add(table);
    }

    /**
     * 
     * @param observable
     * @param oldValue
     * @param newValue
     */
    public void initFields(ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) {
        this.uiComment.setHtmlText(null);
        table.setTableItems(()->null);
        
        if (oldValue != null) {
            this.libelle.textProperty().unbindBidirectional(oldValue.libelleProperty());
        }
        
        if (newValue != null) {
            // Binding digue name.----------------------------------------------
            this.libelle.textProperty().bindBidirectional(newValue.libelleProperty());

            // Initialize comment editor.---------------------------------------
            this.uiComment.setHtmlText(newValue.getCommentaire());
            
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
            final TronconDigue createdPojo = (TronconDigue) super.createPojo();
            if (elementProperty.get() != null) {
                ((TronconDigue)createdPojo).setDigueId(elementProperty.get().getId());
            }
            return createdPojo;
        }
    }
}
