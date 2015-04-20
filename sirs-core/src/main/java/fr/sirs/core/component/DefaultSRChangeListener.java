package fr.sirs.core.component;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import java.lang.ref.WeakReference;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.referencing.LinearReferencing;
import fr.sirs.core.model.Objet;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 * Watch over default SR property of a {@link TronconDigue}, in order to compute
 * PRs of attached objects if its value change.
 * @author Alexis Manin (Geomatys)
 */
public class DefaultSRChangeListener implements ChangeListener<String> {

    private static final String CONFIRMATION_TEXT = "Êtes-vous sûr de vouloir changer le SR par défaut ? Les PRs de tous les objets associés au tronçon seront recalculés.";
    private static final String ALREADY_RUNNING_TEXT = "Un calcul sur le SR du tronçon est déjà en cours. Veuillez attendre qu'il soit complété.";
    
    
    final WeakReference<TronconDigue> target;
    /** 
     * Used as a flag to inform that current detected change has been introduced
     * by a reset from here.
     */
    private String previousValue;
    private Task task;
    
    public DefaultSRChangeListener(final TronconDigue troncon) {
        target = new WeakReference<>(troncon);
        if (troncon != null) {
            troncon.systemeRepDefautIdProperty().addListener(this);
        } 
    }

    /**
     * Called when the default SR of a tronçon changes. We ask user if he really
     * want to change this parameter, because it will induce update of all 
     * calculate PRs for the objects on the current {@link TronconDigue}.
     * If user agree, we launch computing, otherwise we reset value.
     * 
     * @param observable The default SR observable property
     * @param oldValue The previous value for the SR property.
     * @param newValue The set value for default SR property.
     */
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        TronconDigue troncon = target.get();
        if (troncon != null) {
            // Value reset
            if (previousValue != null && previousValue.equals(newValue)) {
                previousValue = null;

            // Another task still running
            } else if (task != null && !task.isDone()) {
                new Alert(Alert.AlertType.INFORMATION, ALREADY_RUNNING_TEXT, ButtonType.OK).show();
                previousValue = oldValue;
                troncon.setSystemeRepDefautId(oldValue);

            // Ask user if he's sure of his change. 
            } else {
                final Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, CONFIRMATION_TEXT, ButtonType.NO, ButtonType.YES);
                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get().equals(ButtonType.YES)) {
                    task = new ComputePRForStructures(troncon);
                    TaskManager.INSTANCE.submit(task);
                } else {
                    previousValue = oldValue;
                    troncon.setSystemeRepDefautId(oldValue);
                }
            }
        }
    }
    
    /**
     * A task which will iterate over all objects of a {@link TronconDigue} to update
     * their PRs.
     */
    private static class ComputePRForStructures extends Task<Boolean> {

        private final TronconDigue troncon;
        public ComputePRForStructures(final TronconDigue toOperateOn) {
            troncon = toOperateOn;
            updateTitle("Mise à jour des PRs");
        }
        
        @Override
        protected Boolean call() throws Exception {
            // For optimisation purpose, we linear geometry before iteration.
            updateMessage("Calcul des paramètres de projection");
            final LineString lineString = LinearReferencing.asLineString(troncon.getGeometry());
            ArgumentChecks.ensureNonNull("Linéaire de réference", lineString);
            final LinearReferencing.SegmentInfo[] linear = LinearReferencing.buildSegments(lineString);
            final SessionGen session = InjectorCore.getBean(SessionGen.class);
            
            ArgumentChecks.ensureNonNull("SR par défaut", troncon.getSystemeRepDefautId());
            SystemeReperage sr = session.getSystemeReperageRepository().get(troncon.getSystemeRepDefautId());
                    
            final int progressMax = troncon.getStructures().size();
            int currentProgress = 0;
            updateMessage("Parcours des objets");
            updateProgress(currentProgress, progressMax);
            for (final Objet current : troncon.getStructures()) {
                final TronconUtils.PosInfo position = new TronconUtils.PosInfo(current, troncon, linear, session);
                
                Point startPoint = position.getGeoPointStart();
                current.setPR_debut(TronconUtils.computePR(linear, sr, startPoint, session.getBorneDigueRepository()));
                
                Point endPoint = position.getGeoPointEnd();
                if (startPoint.equals(endPoint)) {
                    current.setPR_fin(current.getPR_fin());
                } else {
                    current.setPR_fin(TronconUtils.computePR(linear, sr, endPoint, session.getBorneDigueRepository()));
                }
                
                updateProgress(currentProgress++, progressMax);
            }
            
            updateMessage("Mise à jour de la base de données");
            InjectorCore.getBean(SessionGen.class).getTronconDigueRepository().update(troncon);
            
            return true;
        }
        
    }
}
