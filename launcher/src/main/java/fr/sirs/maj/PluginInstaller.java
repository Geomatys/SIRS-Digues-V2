
package fr.sirs.maj;

import fr.sirs.PluginInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.Loader;
import fr.sirs.core.SirsCore;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Classe utilitaire permettant de retrouver / installer des plugins.
 * Note : Le chargement des plugins est fait au chargement de l'application, 
 * dans {@link Loader}.
 */
public class PluginInstaller {
        
    public static PluginList listLocalPlugins() throws IOException {
        final PluginList list = new PluginList();
        if (Files.isDirectory(SirsCore.PLUGINS_PATH)) {
            final Pattern jsonPattern = Pattern.compile("(?i).*(\\.json)$");
            final ObjectMapper jsonMapper = new ObjectMapper();
            Files.walkFileTree(SirsCore.PLUGINS_PATH, new HashSet<>(), 2, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes bfa) throws IOException {
                    if (jsonPattern.matcher(file.getFileName().toString()).matches()) {
                        list.plugins.add(jsonMapper.readValue(file.toFile(), PluginInfo.class));
                    }
                    return super.visitFile(file, bfa);
                }
            
            });
        }
        return list;
    }
    
    public static PluginList listDistantPlugins(URL serverUrl) {
        final PluginList list = new PluginList();
        // TODO
        return list;
    }
        
    public static void install(URL serverUrl, PluginInfo toInstall) throws IOException {
        final Path tmpFile = Files.createTempFile(UUID.randomUUID().toString(), "tmp");
        final Path pluginDir = SirsCore.PLUGINS_PATH.resolve(toInstall.getName());

        try {
            // Start by target directory and descriptor file creation, cause if 
            // those two simple operations fail, it's useless to download plugin.
            Files.createDirectory(pluginDir);
            final Path pluginDescriptor = pluginDir.resolve(toInstall.getName()+".json");            
            try (final OutputStream stream = Files.newOutputStream(pluginDescriptor)) {
                new ObjectMapper().writeValue(stream, toInstall);
            }
            
            // Download temporary zip file
            URL bundleURL = toInstall.bundleURL(serverUrl);
            try (final InputStream input = bundleURL.openStream()) {
                Files.copy(input, tmpFile);
            }

            // Copy zip content into plugin directory.
            try (FileSystem zipSystem = FileSystems.newFileSystem(URI.create("jar:file" + tmpFile.toUri().toString()), new HashMap<>())) {
                final Path tmpZip = zipSystem.getRootDirectories().iterator().next();

                Files.walkFileTree(tmpZip, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException {
                        Files.copy(t, pluginDir.resolve(tmpZip.relativize(t)));
                        return super.visitFile(t, bfa);
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
                        Files.createDirectory(pluginDir.resolve(tmpZip.relativize(t)));
                        return super.preVisitDirectory(t, bfa);
                    }

                });
            }

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
    
    public static void uninstall(final PluginInfo toRemove) throws IOException {
        // TODO : happen version in plugin directory name ?
        final Path pluginDir = SirsCore.PLUGINS_PATH.resolve(toRemove.getName());
        deleteDirectory(pluginDir);
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
    
}
