
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.TronconDigueRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.geotoolkit.gui.javafx.util.FXDeleteTableColumn;
import org.geotoolkit.gui.javafx.util.FXMoveDownTableColumn;
import org.geotoolkit.gui.javafx.util.FXMoveUpTableColumn;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconMerge extends VBox{
    
    @FXML private TableView uiTable;

    private final ObservableList<TronconDigue> troncons = FXCollections.observableArrayList();
    
    public FXTronconMerge() {
        SIRS.loadFXML(this);
        
        final TableColumn<TronconDigue,String> col = new TableColumn<>("Nom");
        col.setEditable(false);
        col.setCellValueFactory((TableColumn.CellDataFeatures<TronconDigue, String> param) -> param.getValue().libelleProperty());
        
        uiTable.setItems(troncons);
        uiTable.getColumns().add(new FXMoveUpTableColumn());
        uiTable.getColumns().add(new FXMoveDownTableColumn());
        uiTable.getColumns().add(col);
        uiTable.getColumns().add(new FXDeleteTableColumn(false));
        
    }

    public ObservableList<TronconDigue> getTroncons() {
        return troncons;
    }
 
    public void processMerge(){
        if(troncons.size()<=1) return;
        
        final Session session = Injector.getSession();

        final TronconDigue merge = troncons.get(0).copy();
        final TronconDigueRepository tronconRepo = session.getTronconDigueRepository();
        tronconRepo.add(merge);
        try {
            final StringBuilder sb = new StringBuilder(troncons.get(0).getLibelle());
            for (int i = 1, n = troncons.size(); i < n; i++) {
                TronconUtils.mergeTroncon(merge, troncons.get(i), session);
                sb.append(" + ").append(troncons.get(i).getLibelle());
            }
            merge.setLibelle(sb.toString());
            tronconRepo.update(merge);
        } catch (Exception e) {
            /* An exception has been thrown. We remove the resulting troncon from
             * database, as it is not complete.
             */
            try {
                tronconRepo.remove(merge);
            } catch (Exception suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
        
    }
    
}
