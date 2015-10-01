
package fr.sirs.theme.ui;

import static fr.sirs.CorePlugin.initTronconDigue;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.TronconLitRepository;
import fr.sirs.core.model.Lit;
import fr.sirs.core.model.TronconLit;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXLitPane extends FXLitPaneStub {
    @Autowired private Session session;
    
    @FXML protected VBox contentBox;

    private final TronconLitPojoTable table = new TronconLitPojoTable();

    /**
     * Constructor. Initialize part of the UI which will not require update when 
     * element edited change.
     */
    protected FXLitPane() {
        super();
        Injector.injectDependencies(this);
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
     * @param observableElement
     * @param oldElement
     * @param newElement
     */
    @Override
    protected void initFields(ObservableValue<? extends Lit > observableElement, Lit oldElement, Lit newElement) {
        super.initFields(observableElement, oldElement, newElement);
        if(newElement!=null){
            table.setTableItems(()->FXCollections.observableArrayList(
                ((TronconLitRepository) session.getRepositoryForClass(TronconLit.class)).getByLit(newElement)));
        }
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
            initTronconDigue(result, Injector.getSession());
            return result;
        }
    }
}
