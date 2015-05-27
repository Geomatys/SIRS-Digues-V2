
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Positionable;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
    private final MergeTask task = new MergeTask();
    
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
 
    public void processMerge() {
        
        final Alert confirmCut = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment fusionner les tronçons ? Si oui, vos modifications seront enregistrées.", ButtonType.YES, ButtonType.NO);
        confirmCut.showAndWait();
        final ButtonType result = confirmCut.getResult();
        if(result==ButtonType.YES){
            if (!task.isDone() || !task.isRunning()) {
                TaskManager.INSTANCE.submit(task);
            }
        }
    }
    
    /**
     * A task whose job is to perform fusion of {@link TronconDigue} selected via merge tool.
     */
    private class MergeTask extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {
            if (troncons.size() <= 1) {
                return false;
            }
            
            updateTitle("Fusion de tronçons");
            updateProgress(0, troncons.size());
            
            final Session session = Injector.getSession();

            final TronconDigue merge = troncons.get(0).copy();
            final TronconDigueRepository tronconRepo = session.getTronconDigueRepository();
            tronconRepo.add(merge);
            try {
                final StringBuilder sb = new StringBuilder(troncons.get(0).getLibelle());
                for (int i = 1, n = troncons.size(); i < n; i++) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException("La fusion de tronçon a été interrompue.");
                    
                    final TronconDigue current = troncons.get(i);
                    updateProgress(i, troncons.size());
                    updateMessage("Ajout du tronçon "+current.getLibelle());
                    
                    TronconUtils.mergeTroncon(merge, current, session);
                    sb.append(" + ").append(current.getLibelle());
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
            
            // Merge succeeded, we must now archive old ones.
            Iterator<TronconDigue> it = troncons.iterator();
            while (it.hasNext()) {
                final TronconDigue current = it.next();
                LocalDateTime now = LocalDateTime.now();
                current.date_finProperty().set(now);
                for (Positionable obj : TronconUtils.getPositionableList(current)) {
                    if (obj instanceof AvecBornesTemporelles) {
                        ((AvecBornesTemporelles) obj).date_finProperty().set(LocalDateTime.now());
                        try {
                            AbstractSIRSRepository repo = session.getRepositoryForClass(obj.getClass());
                            repo.add(obj);
                        } catch (Exception e) {
                            SirsCore.LOGGER.log(Level.WARNING, "Positioned object cannot be archived : " + obj.getId(), e);
                        }
                    }
                }
                session.getTronconDigueRepository().update(current);
                it.remove();
            }
            return true;
        }
    }
}
