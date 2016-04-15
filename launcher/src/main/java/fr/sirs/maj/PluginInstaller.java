/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 * Classe utilitaire permettant de retrouver / installer des plugins. Note : Le
 * chargement des plugins est fait au chargement de l'application, dans
 * {@link fr.sirs.Loader}.
 */
public class PluginInstaller {

    /**
     * References operations registered for each plugin. Multiple operations can
     * be present for a plugin. For example, when updating a plugin, we first
     * uninstall it, then proceed to next version installation.
     *
     * /!\ All thread submissions must be synchronized over this tree map,
     * because it is used for checking / acting on task states.
     */
    private static final TreeMap<String, List<PluginOperation>> OPERATIONS = new TreeMap<>();

    public static PluginList listLocalPlugins() throws IOException {
        final PluginList list = new PluginList();
        if (Files.isDirectory(SirsCore.PLUGINS_PATH)) {
            final Pattern jsonPattern = Pattern.compile("(?i).*(\\.json)$");
            final ObjectMapper jsonMapper = new ObjectMapper();
            final List<PluginInfo> oldVersionPlugins = new ArrayList<>();
            Files.walkFileTree(SirsCore.PLUGINS_PATH, Collections.singleton(FileVisitOption.FOLLOW_LINKS), 2, new SimpleFileVisitor<Path>() {
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
     * Affiche une popup avertissant que des plugins sont incompatibles avec la
     * version actuelle de l'application.
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
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int i = 0;
        grid.add(new Label(oldVersionPlugins.size() > 1 ? "Des plugins précédemment installés sont incompatibles"
                : "Un plugin précédemment installé est incompatible"), 0, i++);
        grid.add(new Label("avec la version de l'application lancée."), 0, i++);
        grid.add(new Label(oldVersionPlugins.size() > 1 ? "Ils vont être supprimés." : "Il va être supprimé."), 0, i++);
        i++;
        grid.add(new Label(oldVersionPlugins.size() > 1 ? "Plugins concernés :" : "Plugin concerné :"), 0, i++);
        for (final PluginInfo oldPlugin : oldVersionPlugins) {
            grid.add(new Label(oldPlugin.getName() + " v" + oldPlugin.getVersionMajor() + "." + oldPlugin.getVersionMinor()), 0, i++);
        }
        final Button ok = new Button("Valider");
        grid.add(ok, 0, ++i);
        GridPane.setHalignment(ok, HPos.RIGHT);

        ok.setOnAction(event -> {
            oldVersionPlugins.forEach(PluginInstaller::uninstall);
            stage.close();
        });

        stage.setOnCloseRequest(event -> {
            oldVersionPlugins.forEach(PluginInstaller::uninstall);
            stage.close();
        });

        final Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    public static PluginList listDistantPlugins(URL serverUrl) throws IOException {
        final PluginList list = new PluginList();
        URLConnection connection = serverUrl.openConnection();
        try (final InputStream input = connection.getInputStream()) {
            final List<PluginInfo> plugins = new ObjectMapper().readValue(
                    input, new TypeReference<List<PluginInfo>>() {
                    });
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
     * Vérifie si un plugin est compatible avec la version de l'application
     * actuellement lancée.
     *
     * @param plugin Le plugin à vérifier.
     * @return {@code True} si le plugin est compatible pour cette version de
     * l'application, {@code false} sinon.
     */
    private static boolean isCompatible(final PluginInfo plugin) {
        String appVersion = SirsCore.getVersion();
        if (appVersion == null || appVersion.isEmpty() || !appVersion.contains(".")) {
            // Impossible de récupérer la version de l'application, celà indique que l'application
            // a été lancée via un IDE comme Intellij, pour ne pas bloquer les futures développements
            // on valide tous les plugins.
            return true;
        }

        final int currentAppVersion;
        try {
            currentAppVersion = Integer.parseInt(appVersion.substring(appVersion.indexOf(".") + 1));
        } catch (NumberFormatException e) {
            // Nous sommes en dev dans une version de type 0.x-SNAPSHOT, dans ce cadre on active tous les plugins.
            return true;
        }

        if (plugin.getAppVersionMin() < 0) {
            // La version minimale de l'application pour laquelle ce plugin fonctionne n'a pas été définie,
            // ce plugin vient d'une ancienne version et doit être supprimé.
            return false;
        }
        return (plugin.getAppVersionMax() < 0 && currentAppVersion >= plugin.getAppVersionMin())
                || (currentAppVersion >= plugin.getAppVersionMin() && currentAppVersion <= plugin.getAppVersionMax());
    }

    /**
     * Create a task whose job is to install plugin pointed by input
     * information. If a task attempting to install the plugin is already
     * submitted, we do not launch a new one, and simply return the one going or
     * succeeded.
     *
     * @param serverUrl Url of the server where the plugin to install is
     * located.
     * @param toInstall Plugin information about the plugin to install.
     * @return A task ready to be submitted (not launched yet) to install the
     * plugin.
     */
    public static Task install(URL serverUrl, PluginInfo toInstall) {
        ArgumentChecks.ensureNonNull("Plugin location", serverUrl);
        ArgumentChecks.ensureNonNull("Information about plugin to install", toInstall);
        return scheduleTask(InstallPlugin.class, toInstall, () -> new InstallPlugin(serverUrl, toInstall));
    }

    /**
     * Create a task to remove plugin pointed by given information. The task
     * result is a boolean which indicates that the plugin have been found and
     * deleted, or not.
     *
     * @param toRemove Information about the plugin to remove.
     * @return A task (not submitted yet) in charge of plugin deletion.
     */
    public static Task<Boolean> uninstall(final PluginInfo toRemove) {
        ArgumentChecks.ensureNonNull("Information about plugin to remove", toRemove);
        return scheduleTask(UninstallPlugin.class, toRemove, () -> new UninstallPlugin(toRemove));
    }

    /**
     * Submit a task for execution. The task will be executed after all other tasks
     * already submitted for the same plugin (whatever its version is) are over.
     * If the last task found is the same as the task which should be submitted,
     * no task will be added for execution.
     * @param <T> Type of operation to submit.
     * @param operationType Type of operation to submit.
     * @param plugin Information about the plugin to install.
     * @param operationSupplier Gives the operation to submit.
     * @return The submitted task, or a task already running / submitted performing the same operation.
     */
    private static <T extends PluginOperation> PluginOperation scheduleTask(
            final Class<T> operationType, final PluginInfo plugin, final Supplier<T> operationSupplier) {

        synchronized (OPERATIONS) {
            List<PluginOperation> tasks = OPERATIONS.computeIfAbsent(plugin.getName(), param -> new ArrayList());
            final PluginOperation op;
            if (!tasks.isEmpty())
                op = tasks.get(tasks.size() - 1);
            else
                op = null;

            // If the same task is already running or scheduled, we return it directly.
            if (op != null) {
                if (operationType.isAssignableFrom(op.getClass())
                        && op.pluginInfo.getVersionMajor() == plugin.getVersionMajor()
                        && op.pluginInfo.getVersionMinor() == plugin.getVersionMinor()
                        && !op.isDone()) {

                    return op;
                }
            }

            // Prepare installation
            final T toSubmit = operationSupplier.get();
            toSubmit.setOnFailed(evt -> SirsCore.LOGGER.log(Level.WARNING, "An operation on a plugin failed !", toSubmit.getException()));
            tasks.add(toSubmit);

            // Schedule current operation AFTER all other job on it has been done.
            final Runnable runner = () -> TaskManager.INSTANCE.submit(toSubmit);
            if (op != null && !op.isDone()) {
                op.runningProperty().addListener((obs, oldValue, newValue) -> runner.run());
            } else {
                runner.run();
            }

            return toSubmit;
        }
    }

    /**
     * Recursively delete a folder and all its content.
     * @param directory Folder to remove.
     * @throws IOException
     */
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

    private static abstract class PluginOperation<T> extends Task<T> {

        final PluginInfo pluginInfo;

        protected PluginOperation(PluginInfo pluginInfo) {
            ArgumentChecks.ensureNonNull("Plugin information", pluginInfo);
            this.pluginInfo = pluginInfo;
        }
    }

    /**
     * Task in charge of plugin deletion.
     */
    private static class UninstallPlugin extends PluginOperation<Boolean> {

        public UninstallPlugin(PluginInfo toRemove) {
            super(toRemove);
        }

        @Override
        protected Boolean call() throws Exception {
            // TODO : happen version in plugin directory name ?
            final Path pluginDir = SirsCore.PLUGINS_PATH.resolve(pluginInfo.getName());
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
    private static class InstallPlugin extends PluginOperation<Boolean> {
        /**
         * Number of millis for opening the connection before timeout.
         */
        private static final int CONNECTION_TIMEOUT = 5000;

        /**
         * Number of millis to wait after launching a timeout if no data has been downloaded.
         */
        private static final int READ_TIMEOUT = 20000;

        private final URL serverUrl;

        public InstallPlugin(URL serverURL, PluginInfo pluginInfo) {
            super(pluginInfo);
            ArgumentChecks.ensureNonNull("Download URL", serverURL);
            this.serverUrl = serverURL;
        }

        @Override
        protected Boolean call() throws Exception {
            updateTitle(pluginInfo.getTitle());

            // Delete plugin if already present.
            new UninstallPlugin(pluginInfo).call();

            final Path tmpFile = Files.createTempFile(UUID.randomUUID().toString(), ".zip");
            final Path pluginDir = SirsCore.PLUGINS_PATH.resolve(pluginInfo.getName());

            try {
                // Start by target directory and descriptor file creation, cause if
                // those two simple operations fail, it's useless to download plugin.
                Files.createDirectories(pluginDir);
                final Path pluginDescriptor = pluginDir.resolve(pluginInfo.getName() + ".json");
                try (final OutputStream stream = Files.newOutputStream(pluginDescriptor)) {
                    new ObjectMapper().writeValue(stream, pluginInfo);
                }

                // Download temporary zip file
                URL bundleURL = pluginInfo.bundleURL(serverUrl);
                updateTitle("Téléchargement : " + pluginInfo.getTitle());
                final URLConnection bundleConnec = bundleURL.openConnection();
                bundleConnec.setConnectTimeout(CONNECTION_TIMEOUT);
                bundleConnec.setReadTimeout(READ_TIMEOUT);
                final long totalLength = bundleConnec.getContentLengthLong();
                long downloaded = 0;
                final String totalReadableSize = SIRS.toReadableSize(totalLength);
                try (final InputStream input = bundleConnec.getInputStream();
                        final OutputStream output = Files.newOutputStream(tmpFile)) {
                    int readBytes;
                    final byte[] buffer = new byte[65536];
                    while ((readBytes = input.read(buffer)) >= 0) {
                        output.write(buffer, 0, readBytes);
                        downloaded += readBytes;
                        updateProgress(downloaded, totalLength);
                        updateTitle("Téléchargement ("+ pluginInfo.getTitle() + ") "+ SIRS.toReadableSize(downloaded) + " sur " + totalReadableSize);
                    }
                    output.flush();
                }

                // Verify that the content was fully downloaded
                if (downloaded != totalLength) {
                    throw new IOException("Le plugin \""+ pluginInfo.getTitle() +"\" n'a pas été entièrement téléchargé");
                }

                updateTitle("Extraction : " + pluginInfo.getTitle());

                // Copy zip content into plugin directory.
                try (FileSystem zipSystem = FileSystems.newFileSystem(URI.create("jar:" + tmpFile.toUri().toString()), new HashMap<>())) {
                    final Path tmpZip = zipSystem.getRootDirectories().iterator().next();

                    // Count files to copy
                    final AtomicLong fileCount = new AtomicLong();
                    Files.walkFileTree(tmpZip, new SimpleFileVisitor<Path>() {
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
