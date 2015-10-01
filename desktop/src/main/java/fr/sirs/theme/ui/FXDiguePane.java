package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractTronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.TronconDigue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDiguePane extends FXDiguePaneStub {
       
    @Autowired private Session session;
    
    @FXML private VBox centerContent;

    private final TronconPojoTable table = new TronconPojoTable();

    
    protected FXDiguePane() {
        super();
        Injector.injectDependencies(this);
        table.editableProperty().bind(disableFieldsProperty().not());
        table.parentElementProperty().bind(elementProperty);
        centerContent.getChildren().add(table);
    }

    public FXDiguePane(final Digue digue){
        this();
        this.elementProperty().set(digue);
    }

    /**
     * 
     * @param observable
     * @param oldValue
     * @param newValue
     */
    @Override
    public void initFields(ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) {
        super.initFields(observable, oldValue, newValue);
        
        if (newValue != null) {
            table.setTableItems(()->FXCollections.observableArrayList(((AbstractTronconDigueRepository) session.getRepositoryForClass(TronconDigue.class)).getByDigue(newValue)));
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
