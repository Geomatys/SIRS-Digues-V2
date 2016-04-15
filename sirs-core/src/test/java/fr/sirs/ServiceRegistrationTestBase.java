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
package fr.sirs;

import com.sun.nio.zipfs.JarFileSystemProvider;
import com.sun.tools.javac.util.ServiceLoader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.sis.util.logging.Logging;
import org.junit.Assert;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ServiceRegistrationTestBase {

    protected static final Logger LOGGER = Logging.getLogger("Registration test");

    /**
     * Scan all packages matching a given predicate to find concrete inherited
     * classes registered according to given regstration predicate.
     * @param serviceClass Base class of the service to test (only subclasses are checked).
     * @param registeredPredicate Tells if a given class is well registered or not.
     * @param packageFilter A filter to reduce package scan to the ones matching
     * the predicate. If null, no filter is applied.
     * @return A set which contains ffound implementations not registered.
     * @throws java.lang.Exception If something goes wrong while scanning packages.
     */
    public Set<Class> checkRegistration(
            final Class serviceClass,
            final Predicate<Class> registeredPredicate,
            final Predicate<Package> packageFilter
    ) throws Exception {

        // keep reference to already scanned package, to ensure not to browse same path twice.
        final ArrayList<Path> scannedPackages = new ArrayList<>();

        // Scan packages of current class loader to retrieve all effective implementations of input service.
        final ClassLoader cl = this.getClass().getClassLoader();//Thread.currentThread().getContextClassLoader();
        final HashSet<Class> unregistered = new HashSet<>();
        scan:
        for (final Package p : Package.getPackages()) {
            if (packageFilter != null && !packageFilter.test(p)) {
                continue;
            }

            final Path packageDir = getPath(cl.getResource(p.getName().replaceAll("\\.", "/")));
            // If we've already scanned entire package, we don't bother analyzing it again.
            for (final Path scannedPath : scannedPackages) {
                if (packageDir.startsWith(scannedPath)) {
                    continue scan;
                }
            }

            if (!Files.isDirectory(packageDir)) {
                continue;
            }

            Files.walkFileTree(packageDir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // Check if current package has already been visited.
                    final Iterator<Path> it = scannedPackages.iterator();
                    while (it.hasNext()) {
                        Path scannedPath = it.next();
                        if (dir.startsWith(scannedPath)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }

                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final String fileName = file.getFileName().toString();
                    if (fileName.toLowerCase().endsWith(".class")) {
                        String packageName = p.getName() + "." + packageDir.relativize(file.getParent()).toString().replaceAll(Pattern.quote(File.separator), ".");
                        if (!packageName.endsWith(".")) {
                            packageName = packageName + ".";
                        }
                        final Class<?> loaded;
                        try {
                            loaded = cl.loadClass(packageName + fileName.substring(0, fileName.length() - 6));
                        } catch (ClassNotFoundException ex) {
                            throw new IllegalStateException("file scan failed : " + file, ex);
                        }

                        // If we've got a concrete implementation, we check if it's been registered or not.
                        int modifiers = loaded.getModifiers();
                        if (!(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))
                                && serviceClass.isAssignableFrom(loaded)) {
                            if (!registeredPredicate.test(loaded)) {
                                unregistered.add(loaded);
                            }
                        }

                        // Same for inner classes
                        for (final Class subClass : loaded.getDeclaredClasses()) {
                            modifiers = subClass.getModifiers();
                            if (!(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))
                                    && serviceClass.isAssignableFrom(subClass)) {
                                if (!registeredPredicate.test(subClass)) {
                                    unregistered.add(subClass);
                                }
                            }
                        }
                    }
                    return super.visitFile(file, attrs);
                }
            });

            // Re-organize scanned paths to mark entire package done.
            final Iterator<Path> it = scannedPackages.iterator();
            while (it.hasNext()) {
                if (it.next().startsWith(packageDir)) {
                    it.remove();
                }
            }
            scannedPackages.add(packageDir);
        }

        return unregistered;
    }


    /**
     * Check that service loader providing implementations for a given service
     * contains all implementations in current project.
     * @param serviceClass Interface of the service to test.
     * @param packageFilter A filter to reduce package scan to the ones matching
     * the predicate. If null, no filter is applied.
     * @throws java.lang.Exception If something goes wrong while scanning packages.
     */
    public void checkServiceLoading(final Class serviceClass, final Predicate<Package> packageFilter) throws Exception {
        // First, we build a collection of all implementation registered in service loader.
        ServiceLoader sLoader = ServiceLoader.load(serviceClass);
        final HashSet<Class> registered = new HashSet<>();
        final Iterator iterator = sLoader.iterator();
        while (iterator.hasNext()) {
            registered.add(iterator.next().getClass());
        }

        final Predicate<Class> p = input -> registered.remove(input);
        final Set<Class> unregistered = checkRegistration(serviceClass, p, packageFilter);

        Assert.assertTrue(
                logCollection("Unregistered implementations found for "+serviceClass.getCanonicalName(), unregistered, c -> c.getCanonicalName()).get(),
                unregistered.isEmpty());

        Assert.assertTrue(
                logCollection("Invalid implementations registered for "+serviceClass.getCanonicalName(), registered, c -> c.getCanonicalName()).get(),
                registered.isEmpty());
    }

    /**
     * Scan packages to ensure all implementation of a given class are annotated
     * with {@link Component}.
     * @param serviceClass Base class of Spring component.
     * @param packageFilter Predicate to specify if a package should be scanned.
     * @throws Exception
     */
    public void checkSpringComponents(final Class serviceClass, final Predicate<Package> packageFilter) throws Exception {
        final Predicate<Class> p = input -> input.getDeclaredAnnotation(Component.class) != null;
        final Set<Class> unregistered = checkRegistration(serviceClass, p, packageFilter);

        Assert.assertTrue(
                logCollection("Unregistered implementations found for "+serviceClass.getCanonicalName(), unregistered, c -> c.getCanonicalName()).get(),
                unregistered.isEmpty());
    }

    /**
     * Log all elements contained in input collection, one element per line.
     * @param <T> Type of object to print.
     * @param title A title to display before collection. If null, no title will be printed.
     * @param c Collection to print.
     * @param converter A converter to define how input objects should be print. If null, toString() method will be used.
     * @return  A supplier ready to deliver message.
     */
    private static <T> Supplier<String> logCollection(final String title, final Collection<T> c, final Function<T, String> converter) {
        return () -> {
            final StringBuilder builder = new StringBuilder();
            if (title != null && !title.isEmpty()) {
                builder.append(title).append(System.lineSeparator());
            }

            final Consumer<T> consumer;
            if (converter == null) {
                consumer = (t) -> builder.append(t);
            } else {
                consumer = (t) -> builder.append(converter.apply(t));
            }

            final Iterator<T> it = c.iterator();
            if (it.hasNext()) {
                consumer.accept(it.next());
            }

            while (it.hasNext()) {
                builder.append(System.lineSeparator());
                consumer.accept(it.next());
            }

            return builder.toString();
        };
    }

    private static Path getPath(final URL resource) throws URISyntaxException, IOException {
        final String protocol = resource.getProtocol().toLowerCase();
        if ("file".equals(protocol)) {
            return Paths.get(resource.toURI());
        } else if ("jar:file".equals(protocol)) {
            final String[] splittedResource = resource.toURI().toString().split("!");
            if (splittedResource.length == 2) {
                return new JarFileSystemProvider().newFileSystem(new URI(splittedResource[0]), new HashMap<>()).getPath(splittedResource[1]);
            } else {
                throw new UnsupportedOperationException("Unupported syntax for jar file URI.");
            }
        } else {
            throw new UnsupportedOperationException("Unsupported protocol : "+protocol);
        }
    }
}
