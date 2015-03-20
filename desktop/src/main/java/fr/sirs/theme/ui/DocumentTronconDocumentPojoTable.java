package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DocumentTronconDocumentPojoTable extends ListenPropertyPojoTable {
    
    private final Function<DocumentTroncon, Void> addAction;

    public DocumentTronconDocumentPojoTable(Class pojoClass, String title, 
            final Function<DocumentTroncon, Void> addAction) {
        super(pojoClass, title);
        this.addAction = addAction;
    }
    
    @Override
    protected Object createPojo() {
        final DocumentTroncon documentTroncon = (DocumentTroncon) super.createPojo();
        addAction.apply(documentTroncon);
        return documentTroncon;
    }
    
    @Override
    protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event){
        final DocumentTroncon obj = (DocumentTroncon) event.getRowValue();
        if(obj!=null && obj.getParent()!=null){
            Injector.getSession().getTronconDigueRepository().update((TronconDigue) obj.getParent());
        }
    }
    
    @Override
    protected void deletePojos(Element... pojos) {
        ObservableList<Element> items = uiTable.getItems();
        for (Element pojo : pojos) {
            final DocumentTroncon dt = (DocumentTroncon) pojo;
            final TronconDigueRepository tronconDigueRepository = Injector.getSession().getTronconDigueRepository();
            final TronconDigue tronconDigue = tronconDigueRepository.get(dt.getDocumentId());
            if(tronconDigue!=null && tronconDigue.getDocumentTroncon().contains(dt)){
                tronconDigue.getDocumentTroncon().remove(dt);
                tronconDigueRepository.update(tronconDigue);
            }
            items.remove(pojo);
        }
    }
    
}
