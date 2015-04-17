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

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DefaultSRChangeListener implements ChangeListener<String> {

    private static final String CONFIRMATION_TEXT = "Êtes-vous sûr de vouloir changer le SR par défaut ? Les PRs de tous les objets associés au tronçon seront recalculés.";
    
    final WeakReference<TronconDigue> target;
    private String previousValue;
    
    public DefaultSRChangeListener(final TronconDigue troncon) {
        target = new WeakReference<>(troncon);
        if (troncon != null) {
            troncon.systemeRepDefautIdProperty().addListener(this);
        } 
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (previousValue != null && previousValue.equals(newValue)) {
            previousValue = null;
            
        } else {
            TronconDigue troncon = target.get();
            if (troncon != null) {
                final Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, CONFIRMATION_TEXT, ButtonType.NO, ButtonType.YES);
                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get().equals(ButtonType.YES)) {
                    // TODO : Task which compute new PRs.
                } else {
                    previousValue = oldValue;
                    troncon.setSystemeRepDefautId(oldValue);
                }
            }
        }
    }
    
    private static class ComputePRForStructures extends Task<Boolean> {

        private final TronconDigue troncon;
        public ComputePRForStructures(final TronconDigue toOperateOn) {
            troncon = toOperateOn;
            updateTitle("Mise à jour des PRs");
        }
        
        @Override
        protected Boolean call() throws Exception {
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
                final TronconUtils.PosInfo position = new TronconUtils.PosInfo(current, session);
                
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
            
            return true;
        }
        
    }
}
