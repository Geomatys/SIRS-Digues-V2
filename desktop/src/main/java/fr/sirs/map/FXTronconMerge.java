
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.Positionable;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.util.FXDeleteTableColumn;
import org.geotoolkit.gui.javafx.util.FXMoveDownTableColumn;
import org.geotoolkit.gui.javafx.util.FXMoveUpTableColumn;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconMerge extends VBox {
    
    @FXML private TableView uiTable;
    @FXML private TextField uiLinearName;
    @FXML private Label infoLabel;
    @FXML private Label nameLabel;

    private final ObservableList<TronconDigue> troncons = FXCollections.observableArrayList();
    private final MergeTask task = new MergeTask();
    private final FXMap map;
    private final String typeName;
    
    public FXTronconMerge(final FXMap map, final String typeName) {
        SIRS.loadFXML(this);
        
        this.typeName = typeName;
        
        infoLabel.setText("Nom du " + typeName + " résultant de la fusion :"); // TODO GENDER
        nameLabel.setText("Les " + typeName + "s avant fusion seront archivés.");
        final TableColumn<TronconDigue,String> col = new TableColumn<>("Nom");
        col.setEditable(false);
        col.setCellValueFactory((TableColumn.CellDataFeatures<TronconDigue, String> param) -> param.getValue().libelleProperty());
        
        uiTable.setItems(troncons);
        uiTable.getColumns().add(new FXMoveUpTableColumn());
        uiTable.getColumns().add(new FXMoveDownTableColumn());
        uiTable.getColumns().add(col);
        uiTable.getColumns().add(new FXDeleteTableColumn(false));        
        this.map = map;
    }

    public ObservableList<TronconDigue> getTroncons() {
        return troncons;
    }
 
    public void processMerge() {
        
        final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment fusionner les " + typeName + "s ? Si oui, vos modifications seront enregistrées.", ButtonType.YES, ButtonType.NO);
        confirm.setResizable(true);
        confirm.showAndWait();
        final ButtonType result = confirm.getResult();
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
            
            updateTitle("Fusion de " + typeName + "s");
            updateProgress(0, troncons.size());
            
            final Session session = Injector.getSession();

            final TronconDigue merge = troncons.get(0).copy();
            final AbstractSIRSRepository<TronconDigue> tronconRepo = session.getRepositoryForClass(TronconDigue.class);
            tronconRepo.add(merge);
            try {
                final StringBuilder sb = new StringBuilder(troncons.get(0).getLibelle());
                for (int i = 1, n = troncons.size(); i < n; i++) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException("La fusion de " + typeName + " a été interrompue.");
                    
                    final TronconDigue current = troncons.get(i);
                    updateProgress(i, troncons.size());
                    updateMessage("Ajout du " + typeName + " "+current.getLibelle()); // TODO GENDER
                    
                    TronconUtils.mergeTroncon(merge, current, session);
                    sb.append(" + ").append(current.getLibelle());
                }
                final String mergedName;
                if(uiLinearName.getText()==null || uiLinearName.getText().equals("")) mergedName = sb.toString();
                else mergedName = uiLinearName.getText();
                merge.setLibelle(mergedName);
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
                LocalDate now = LocalDate.now();
                current.date_finProperty().set(now);
                for (Positionable obj : TronconUtils.getPositionableList(current)) {
                    if (obj instanceof AvecBornesTemporelles) {
                        ((AvecBornesTemporelles) obj).date_finProperty().set(LocalDate.now());
                        try {
                            ((AbstractSIRSRepository) session.getRepositoryForClass(obj.getClass())).add(obj);
                        } catch (Exception e) {
                            SirsCore.LOGGER.log(Level.WARNING, "Positioned object cannot be archived : " + obj.getId(), e);
                        }
                    }
                }
                session.getRepositoryForClass(TronconDigue.class).update(current);
                it.remove();
            }
            
            try {
                Injector.getSession().getFrame().getMapTab().getMap().setTemporalRange(LocalDate.now(), map);
            } catch (Exception ex) {
                SirsCore.LOGGER.log(Level.WARNING, "Map temporal range cannot be updated.", ex);
            }
            return true;
        }
    }
}
