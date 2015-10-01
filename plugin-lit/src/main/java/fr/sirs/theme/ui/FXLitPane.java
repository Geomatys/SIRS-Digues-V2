
package fr.sirs.theme.ui;

import static fr.sirs.CorePlugin.initTronconDigue;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.Previews;
import fr.sirs.core.component.TronconLitRepository;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Lit;
import fr.sirs.core.model.TronconLit;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXLitPane extends AbstractFXElementPane<Lit> {

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;
    private final TronconLitPojoTable table = new TronconLitPojoTable();
    
    // Propriétés de Lit
    @FXML protected TextField ui_libelle;
    @FXML protected HTMLEditor ui_commentaire;
    @FXML protected VBox contentBox;

    /**
     * Constructor. Initialize part of the UI which will not require update when 
     * element edited change.
     */
    protected FXLitPane() {
        SIRS.loadFXML(this, Lit.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);


        /*
         * Disabling rules.
         */
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        
        table.editableProperty().bind(disableFieldsProperty().not());
        table.parentElementProperty().bind(elementProperty);
        contentBox.getChildren().add(table);
    }
    
    public FXLitPane(final Lit lit){
        this();
        this.elementProperty().set(lit);
    }     

    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends Lit > observableElement, Lit oldElement, Lit newElement) {
        // Unbind fields bound to previous element.
        table.setTableItems(()->null);
        if (oldElement != null) {
        // Propriétés de Lit
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de Lit
        // * libelle
        ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
        // * commentaire
        ui_commentaire.setHtmlText(newElement.getCommentaire());
        
        table.setTableItems(()->FXCollections.observableArrayList(
                ((TronconLitRepository) session.getRepositoryForClass(TronconLit.class)).getByLit(newElement)));
        
        
    }
    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final Lit element = (Lit) elementProperty().get();


        element.setCommentaire(ui_commentaire.getHtmlText());


        Object cbValue;
    }
    
    private class TronconLitPojoTable extends PojoTable {
    
        public TronconLitPojoTable() {
            super(TronconLit.class, "Tronçon du lit");
        }

        @Override
        protected TronconLit createPojo() {
            TronconLit result = (TronconLit) super.createPojo();
            if (elementProperty().get() != null) {
                ((TronconLit) result).setLitId(elementProperty().get().getId());
            }
            initTronconDigue(result, session);
            return result;
        }
    }
}
