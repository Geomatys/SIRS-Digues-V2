
package fr.sirs.maj;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import fr.sirs.PluginInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.geotoolkit.internal.GeotkFX;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 * Classe utilitaire permettant de retrouver / installer des plugins.
 * Note : Le chargement des plugins est fait au chargement de l'application,
 * dans {@link fr.sirs.Loader}.
 */
public class PluginInstaller {

    public static PluginList listLocalPlugins() throws IOException {
        final PluginList list = new PluginList();
        if (Files.isDirectory(SirsCore.PLUGINS_PATH)) {
            final Pattern jsonPattern = Pattern.compile("(?i).*(\\.json)$");
            final ObjectMapper jsonMapper = new ObjectMapper();
            final List<PluginInfo> oldVersionPlugins = new ArrayList<>();
            Files.walkFileTree(SirsCore.PLUGINS_PATH, new HashSet<>(), 2, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes bfa) throws IOException {
                    if (jsonPattern.matcher(file.getFileName().toString()).matches()) {
                        final PluginInfo pluginInfo = jsonMapper.readValue(file.toFile(), PluginInfo.class);
                        if (isCompatible(pluginInfo)) {
                            // Les plugins compatibles avec des versions précédentes de l'application
                            // ne sont pas montrés.
                            list.plugins.add(pluginInfo);
                        } else {
                            oldVersionPlugins.add(pluginInfo);
                        }
                    }
                    return super.visitFile(file, bfa);
                }
            });

            if (!oldVersionPlugins.isEmpty()) {
                showOldVersionPluginsPopup(oldVersionPlugins);
            }
        }
        return list;
    }

    /**
     * Affiche une popup avertissant que des plugins sont incompatibles avec la version actuelle
     * de l'application.
     *
     * @param oldVersionPlugins Liste des plugins incompatibles.
     */
    private static void showOldVersionPluginsPopup(final List<PluginInfo> oldVersionPlugins) {
        final Stage stage = new Stage();
        stage.getIcons().add(SIRS.ICON);
        stage.setTitle("Gestion des plugins incompatibles");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setAlwaysOnTop(true);
        stage.setOnCloseRequest(event -> deletePlugins(oldVersionPlugins));
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int i = 0;
        grid.add(new Label(oldVersionPlugins.size() > 1 ? "Des plugins précédemment installés sont incompatibles" :
                "Un plugin précédemment installé est incompatible"), 0, i++);
        grid.add(new Label("avec la version de l'application lancée."), 0, i++);
        grid.add(new Label(oldVersionPlugins.size() > 1 ? "Ils vont être supprimés." : "Il va être supprimé."), 0, i++);
        i++;
        grid.add(new Label(oldVersionPlugins.size() > 1 ? "Plugins concernés :" : "Plugin concerné :"), 0, i++);
        for (final PluginInfo oldPlugin : oldVersionPlugins) {
            grid.add(new Label(oldPlugin.getName() + " v" + oldPlugin.getVersionMajor() + "." + oldPlugin.getVersionMinor()), 0, i++);
        }
        final Button ok = new Button("Valider");
        ok.setOnAction(event -> {
            deletePlugins(oldVersionPlugins);
            stage.hide();
        });
        grid.add(ok, 0, ++i);
        GridPane.setHalignment(ok, HPos.RIGHT);
        final Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Supprime les anciens plugins non compatibles avec la version actuelle.
     *
     * @param toRemove La liste des plugins à supprimer.
     */
    private static void deletePlugins(final List<PluginInfo> toRemove) {
        for (final PluginInfo oldPlugin : toRemove) {
            final Task removeTask = uninstall(oldPlugin);
            removeTask.setOnFailed(evt -> {
                final Throwable ex = removeTask.getException();
                SirsCore.LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                Platform.runLater(() -> GeotkFX.newExceptionDialog(ex.getLocalizedMessage(), ex).show());
            });
            TaskManager.INSTANCE.submit(removeTask);
        }
    }

    public static PluginList listDistantPlugins(URL serverUrl) throws IOException {
        final PluginList list = new PluginList();
        URLConnection connection = serverUrl.openConnection();
        try (final InputStream input = connection.getInputStream()) {
            final List<PluginInfo> plugins = new ObjectMapper().readValue(
                    input, new TypeReference<List<PluginInfo>>() {});
            final List<PluginInfo> finalList = new ArrayList<>();
            for (final PluginInfo plugin : plugins) {
                if (isCompatible(plugin)) {
                    // Les plugins compatibles avec des versions précédentes de l'application
                    // ne sont pas montrés.
                    finalList.add(plugin);
                }
            }
            list.setPlugins(finalList);
        } catch (JsonMappingException e) {
            // Allow URL pointing on a local plugin, try to load it
            URLConnection connection2 = serverUrl.openConnection();
            try (final InputStream input = connection2.getInputStream()) {
                list.setPlugins(Collections.singletonList(new ObjectMapper().readValue(
                        input, PluginInfo.class)));
            }
        }

        return list;
    }

    /**
     * Vérifie si un plugin est compatible avec la version de l'application actuellement lancée.
     *
     * @param plugin Le plugin à vérifier.
     * @return {@code True} si le plugin est compatible pour cette version de l'application, {@code false} sinon.
     */
    private static boolean isCompatible(final PluginInfo plugin) {
        String appVersion = SirsCore.getVersion();
        if (appVersion == null || appVersion.isEmpty()) {
            // Impossible de récupérer la version de l'application, celà indique que l'application
            // a été lancée via un IDE comme Intellij, pour ne pas bloquer les futures développements
            // on valide tous les plugins.
            return true;
            //appVersion = "0.14";
        }

        final int currentAppVersion;
        try {
            currentAppVersion = Integer.parseInt(appVersion.substring(2));
        } catch (NumberFormatException e) {
            // Nous sommes en dev dans une version de type 0.x-SNAPSHOT, dans ce cadre on active tous les plugins.
            return true;
        }

        if (plugin.getAppVersionMin() == 0) {
            // La version minimale de l'application pour laquelle ce plugin fonctionne n'a pas été définie,
            // ce plugin vient d'une ancienne version et doit être supprimé.
            return false;
        }
        return (plugin.getAppVersionMax() == 0 && currentAppVersion >= plugin.getAppVersionMin()) ||
               (currentAppVersion >= plugin.getAppVersionMin() && currentAppVersion <= plugin.getAppVersionMax());
    }

    /**
     * Create a task whose job is to install plugin pointed by input information.
     * @param serverUrl Url of the server where the plugin to install is located.
     * @param toInstall Plugin information about the plugin to install.
     * @return A task ready to be submitted (not launched yet) to install the plugin.
     */
    public static Task install(URL serverUrl, PluginInfo toInstall) {
        return new InstallPlugin(serverUrl, toInstall);
    }

    /**
     * Create a task to remove plugin pointed by given information. The task result
     * is a boolean which indicates that the plugin have been found and deleted, or not.
     *
     * @param toRemove Information about the plugin to remove.
     * @return A task (not submitted yet) in charge of plugin deletion.
     */
    public static Task<Boolean> uninstall(final PluginInfo toRemove) {
        return TaskManager.INSTANCE.submit(new UninstallPlugin(toRemove));
    }

    private static void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException ioe) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, ioe);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes bfa) throws IOException {
                Files.delete(file);
                return super.visitFile(file, bfa);
            }
        });
    }

    /**
     * Task in charge of plugin deletion.
     */
    private static class UninstallPlugin extends Task<Boolean> {

        final PluginInfo toRemove;

        public UninstallPlugin(PluginInfo toRemove) {
            this.toRemove = toRemove;
        }

        @Override
        protected Boolean call() throws Exception {
            // TODO : happen version in plugin directory name ?
            final Path pluginDir = SirsCore.PLUGINS_PATH.resolve(toRemove.getName());
            if (Files.exists(pluginDir)) {
                deleteDirectory(pluginDir);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * A task whose aim is to install a new plugin. Does not check
     */
    private static class InstallPlugin extends Task<Boolean> {

        private final URL serverUrl;
        private final PluginInfo toInstall;

        public InstallPlugin(URL serverURL, PluginInfo pluginInfo) {
            ArgumentChecks.ensureNonNull("Download URL", serverURL);
            ArgumentChecks.ensureNonNull("Information about the plugin to install", pluginInfo);
            this.serverUrl = serverURL;
            this.toInstall = pluginInfo;
        }

        @Override
        protected Boolean call() throws Exception {
            updateTitle(toInstall.getTitle());

            final Path tmpFile = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            final Path pluginDir = SirsCore.PLUGINS_PATH.resolve(toInstall.getName());

            try {
                // Start by target directory and descriptor file creation, cause if
                // those two simple operations fail, it's useless to download plugin.
                Files.createDirectories(pluginDir);
                final Path pluginDescriptor = pluginDir.resolve(toInstall.getName() + ".json");
                try (final OutputStream stream = Files.newOutputStream(pluginDescriptor)) {
                    new ObjectMapper().writeValue(stream, toInstall);
                }

                // Download temporary zip file
                URL bundleURL = toInstall.bundleURL(serverUrl);
                updateTitle("Téléchargement : "+toInstall.getTitle());
                final long totalLength = bundleURL.openConnection().getContentLengthLong();
                try (final InputStream input = bundleURL.openStream();
                        final OutputStream output = Files.newOutputStream(tmpFile)) {
                    int readBytes = 0;
                    long downloaded = 0;
                    final byte[] buffer = new byte[8192];
                    while ((readBytes = input.read(buffer)) >= 0) {
                        output.write(buffer, 0, readBytes);
                        downloaded += readBytes;
                        updateProgress(downloaded, totalLength);
                    }
                }

                updateTitle("Extraction : "+toInstall.getTitle());

                // Copy zip content into plugin directory.
                try (FileSystem zipSystem = FileSystems.newFileSystem(URI.create("jar:" + tmpFile.toUri().toString()), new HashMap<>())) {
                    final Path tmpZip = zipSystem.getRootDirectories().iterator().next();

                    // Count files to copy
                    final AtomicLong fileCount = new AtomicLong();
                    Files.walkFileTree(tmpZip, new SimpleFileVisitor<Path>(){
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            fileCount.incrementAndGet();
                            return super.visitFile(file, attrs);
                        }
                    });

                    // Proceed to extraction
                    final AtomicLong extractedEntries = new AtomicLong();
                    final long totalEntries = fileCount.get();
                    Files.walkFileTree(tmpZip, new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException {
                            Files.copy(t, pluginDir.resolve(tmpZip.relativize(t).toString()));
                            updateProgress(extractedEntries.incrementAndGet(), totalEntries);
                            return super.visitFile(t, bfa);
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
                            if (!t.equals(tmpZip)) {
                                Files.createDirectory(pluginDir.resolve(tmpZip.relativize(t).toString()));
                            }
                            return super.preVisitDirectory(t, bfa);
                        }
                    });
                }

                return true;

            } catch (Throwable e) {
                // If an error occured while copying plugin files, we clean all created files.
                try {
                    deleteDirectory(pluginDir);
                } catch (Throwable bis) {
                    e.addSuppressed(bis);
                }
                throw e;

            } finally {
                try {
                    Files.delete(tmpFile);
                } catch (Exception e) {
                    SirsCore.LOGGER.log(Level.WARNING, "A temporary file cannot be deleted.", e);
                }
            }
        }
    }
}
