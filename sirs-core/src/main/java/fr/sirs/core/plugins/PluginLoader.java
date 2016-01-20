
package fr.sirs.core.plugins;

import fr.sirs.core.SirsCore;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * A custom class loader whose role is to load all plugin jars. To be effective, 
 * this class loader must be set as System class loader.
 * 
 * Note : to set a system class loader, put following parameter on jvm load :
 * -Djava.system.class.loader=my.package.myClassLoader
 * 
 * IMPORTANT : plugins are not loaded at initialisation, you must call {@linkplain #loadPlugins() }
 * to do so.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class PluginLoader extends URLClassLoader {

    private static final Pattern JAR_PATTERN = Pattern.compile("(?i).*(\\.jar)$");

    private boolean loaded = false;

    public PluginLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }
    
    public synchronized void loadPlugins() throws IOException, IllegalStateException {
        if (Files.isDirectory(SirsCore.PLUGINS_PATH)) {
            Files.walk(SirsCore.PLUGINS_PATH, FileVisitOption.FOLLOW_LINKS).filter(PluginLoader::isJar).map(PluginLoader::toURL).forEach(this::addURL);
            loaded = true;
        }
    }
    
    public static boolean isJar(final Path input) {
        return Files.isRegularFile(input) && JAR_PATTERN.matcher(input.getFileName().toString()).matches();
    }
    
    public static URL toURL(final Path input) {
        try {
            return input.toUri().toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public synchronized boolean isLoaded() {
        return loaded;
    }
}
