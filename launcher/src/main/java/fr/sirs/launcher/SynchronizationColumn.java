package fr.sirs.launcher;

import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsDBInfo;
import fr.sirs.core.component.DatabaseRegistry;
import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SynchronizationColumn extends TableColumn<String, Callable> {

    public static final Image ICON_SYNCHRO_STOPPED = SwingFXUtils.toFXImage(IconBuilder.createImage(
            FontAwesomeIcons.ICON_EXCHANGE, 16, new Color(130, 0, 0)), null);

    public static final Image ICON_SYNCHRO_RUNNING = SwingFXUtils.toFXImage(IconBuilder.createImage(
            FontAwesomeIcons.ICON_EXCHANGE, 16, Color.GREEN), null);

    public static final Tooltip PAUSE_SYNCHRO = new Tooltip("Passer en mode hors-ligne.");
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

        setCellValueFactory((CellDataFeatures<String, Callable> param) -> {
            final String dbName = param.getValue();
            if (dbName == null || dbName.isEmpty()) {
                return null;
            } else {
                try {
                    if (dbRegistry.getSynchronizationTasks(dbName).count() > 0) {
                        return new SimpleObjectProperty<>(new StopSync(dbName));
                    } else {
                        final String remoteName = dbRegistry.getInfo(dbName).orElse(new SirsDBInfo()).getRemoteDatabase();
                        if (remoteName != null) {
                            return new SimpleObjectProperty<>(new ResumeSync(dbName, remoteName));
                        }
                    }
                } catch (IOException ex) {
                    SirsCore.LOGGER.log(Level.WARNING, null, ex);
                }
            }
            return null;
        });

        setCellFactory((TableColumn<String, Callable> param) -> new SynchronizationCell());
    }

    private final class SynchronizationCell extends ButtonTableCell<String, Callable> {

        public SynchronizationCell() {
            super(false, null, (Callable c) -> ((c instanceof StopSync) || (c instanceof ResumeSync)), new Function<Callable, Callable>() {
                @Override
                public Callable apply(Callable t) {
                    try {
                        return (Callable) (t == null? null : t.call());
                    } catch (Exception ex) {
                        GeotkFX.newExceptionDialog("Une erreur est survenue lors de la mise à jour des synchronisations.", ex).show();
                        return t;
                    }
                }
            });
        }

        @Override
        protected void updateItem(Callable item, boolean empty) {
            if (empty) {
                button.setVisible(false);

            } else if (item instanceof StopSync) {
                button.setGraphic(new ImageView(ICON_SYNCHRO_RUNNING));
                button.setTooltip(PAUSE_SYNCHRO);
                button.setVisible(true);

            } else if (item instanceof ResumeSync) {
                button.setGraphic(new ImageView(ICON_SYNCHRO_STOPPED));
                button.setTooltip(RESUME_SYNCHRO);
                button.setVisible(true);

            } else {
                button.setVisible(false);
            }
        }
    }

    /**
     * A simple action to stop all replication tasks running on a given
     * database. returns a callable allowing to restart synchronization between
     * input database and itss remote, specified into {@link SirsDBInfo}.
     */
    private class StopSync implements Callable<ResumeSync> {

        private final String dbName;

        public StopSync(final String dbName) {
            this.dbName = dbName;
        }

        @Override
        public ResumeSync call() throws Exception {
            dbRegistry.cancelAllSynchronizations(dbName);
            final String remoteDatabase = dbRegistry.getInfo(dbName).orElse(new SirsDBInfo()).getRemoteDatabase();
            if (remoteDatabase != null) {
                return new ResumeSync(dbName, remoteDatabase);
            } else {
                return null;
            }
        }
    }

    /**
     * A simple action to start synchronisation between two given databases.
     * Returns a callable to put main database offline.
     */
    private class ResumeSync implements Callable<StopSync> {

        private final String dbName;
        private final String remoteName;

        /**
         *
         * @param dbName Main database.
         * @param remoteName Remote database to synchronize with.
         */
        public ResumeSync(final String dbName, final String remoteName) {
            this.dbName = dbName;
            this.remoteName = remoteName;
        }

        @Override
        public StopSync call() throws Exception {
            dbRegistry.synchronizeSirsDatabases(remoteName, dbName, true);
            return new StopSync(dbName);
        }

    }
}
