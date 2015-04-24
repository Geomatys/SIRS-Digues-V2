package fr.sirs.launcher;

import fr.sirs.core.SirsCore;
import fr.sirs.core.component.DatabaseRegistry;
import java.awt.Color;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.ProgressMonitor;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SynchronizationColumn extends TableColumn<String, String> {
    
    public static final Image ICON_SYNCHRO_STOPPED = SwingFXUtils.toFXImage(IconBuilder.createImage(
            FontAwesomeIcons.ICON_EXCHANGE, 16, new Color(130, 0, 0)), null);
    
    public static final Image ICON_SYNCHRO_RUNNING = SwingFXUtils.toFXImage(IconBuilder.createImage(
            FontAwesomeIcons.ICON_EXCHANGE, 16, Color.GREEN), null);

    public static final Tooltip PAUSE_SYNCHRO = new Tooltip("Mettre en pause la synchronisation automatique.");
    public static final Tooltip RESUME_SYNCHRO = new Tooltip("Reprendre la synchronisation automatique.");
    public static final Tooltip ERROR_SYNCHRO = new Tooltip("Impossible de retrouver l'état de synchronisation.");
    
    private final DatabaseRegistry dbRegistry;
    
    public SynchronizationColumn(final DatabaseRegistry registry) {
        super();
        setSortable(false);
        setResizable(false);
        setPrefWidth(24);
        setMinWidth(24);
        setMaxWidth(24);
        
        ArgumentChecks.ensureNonNull("Database registry", registry);
        dbRegistry = registry;
        
        setCellValueFactory((CellDataFeatures<String, String> param) -> new SimpleStringProperty(param.getValue()));
        setCellFactory((TableColumn<String, String> param) -> new SynchronizationCell());
    }
    
    private final class SynchronizationCell extends ButtonTableCell<String, String> {
        
        public SynchronizationCell() {
            super(false, null, null, new Function<String, String>() {

                public String apply(String cellValue) {
                    if (cellValue == null || cellValue.isEmpty()) {
                        return null;
                    }
                    try {
                        if (dbRegistry.resumeSynchronizations(cellValue) < 1) {
                            dbRegistry.pauseSynchronizations(cellValue);
                        }
                    } catch (Exception e) {
                        GeotkFX.newExceptionDialog("Une erreur est survenue lors de la mise à jour des synchronisations.", e).show();
                    }
                    return null;
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            if (empty || item == null || item.isEmpty()) {
                button.setVisible(false);
            } else {
                try {
                if (dbRegistry.getActiveSynchronizationTasks(item).count() > 0) {
                    button.setGraphic(new ImageView(ICON_SYNCHRO_RUNNING));
                    button.setTooltip(PAUSE_SYNCHRO);
                } else if (dbRegistry.getSynchronizationTasks(item).count() > 0) {
                    button.setGraphic(new ImageView(ICON_SYNCHRO_STOPPED));
                    button.setTooltip(RESUME_SYNCHRO);
                } else {
                    button.setGraphic(null);
                    button.setTooltip(null);
                }
                } catch (Exception e) {
                    SirsCore.LOGGER.log(Level.WARNING, null, e);
                    button.setGraphic(new ImageView(ProgressMonitor.ICON_ERROR));
                    button.setTooltip(ERROR_SYNCHRO);
                }
            }
        }       
    }
}
