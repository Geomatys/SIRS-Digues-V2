package fr.sirs.util.property;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.AvecPhotos;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SIRSFileReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.Static;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DocumentRoots extends Static {

    private static final String DEFAULT_ROOT = "default_root";

    /**
     * Search for a root path to use for photographs associated with an object
     * of given type. If no type is given, search for default root defined for
     * {@link AbstractPhoto} objects.
     *
     * @param associatedType Reference type to retrieve a root path for. If null,
     * default photo directory will be returned.
     * @param strict If true, an empty optional is returned if no path is
     * defined for given reference, even if a default root is configured. If
     * false, default path is returned if none is defined for the input
     * reference.
     * @return An empty optional if no root path has been configured for given
     * reference or (if not strict) for default use. A path to resolve given
     * reference with otherwise.
     */
    public static Optional<Path> getPhotoRoot(final Class<? extends AvecPhotos> associatedType, final boolean strict) {
        final ArrayList<String> nodePaths = new ArrayList<>(2);
        nodePaths.add(AbstractPhoto.class.getCanonicalName());
        if (associatedType != null) {
            nodePaths.add(associatedType.getCanonicalName());
        }

        return getRoot(nodePaths, strict);
    }

    /**
     *
     * @param refClass Reference type to retrieve a root path for.
     * @param strict If true, an empty optional is returned if no path is
     * defined for given reference, even if a default root is configured. If
     * false, default path is returned if none is defined for the input
     * reference.
     * @return An empty optional if no root path has been configured for given
     * reference or (if not strict) for default use. A path to resolve given
     * reference with otherwise.
     */
    public static Optional<Path> getRoot(final Class<? extends SIRSFileReference> refClass, final boolean strict) {
        final ArrayList<String> nodePaths = new ArrayList<>(1);
        if (refClass != null) {
            nodePaths.add(refClass.getCanonicalName());
        }

        return getRoot(nodePaths, strict);
    }

    /**
     * Retrieve root path to use to resolve file path pointed by given file
     * reference.
     *
     * @param ref The file reference to get a root path for.
     * @return An empty optional if no root path has been configured for given
     * reference or for default use. A path to resolve given reference with
     * otherwise.
     */
    public static Optional<Path> getRoot(final SIRSFileReference ref) {
        final ArrayList<String> nodePaths = new ArrayList<>(2);
        if (ref instanceof AbstractPhoto) {
            nodePaths.add(AbstractPhoto.class.getCanonicalName());
            // First, we'll try to get root specified for the photo parent.
            final Element parent = ref.getParent();
            final String parentType;
            if (parent != null) {
                parentType = parent.getClass().getCanonicalName();
            } else if (ref.getId() != null) {
                // Try to retrieve parent by querying object preview.
                Preview photoPreview = null;
                try {
                    photoPreview = InjectorCore.getBean(Previews.class).get(ref.getId());
                } catch (Exception e) {
                    SirsCore.LOGGER.log(Level.WARNING, "Cannot access database previews !", e);
                }

                parentType = photoPreview == null ? null : photoPreview.getDocClass();
            } else {
                parentType = null;
            }

            if (parentType != null) {
                nodePaths.add(parentType);
            }

        } else if (ref != null) {
            nodePaths.add(ref.getClass().getCanonicalName());
        }

        return getRoot(nodePaths, false);
    }

    /**
     * Return root folder configured for specified paths.
     *
     * @param paths A path to browse {@link Preferences} nodes.
     * @return An empty optional if no path is found for target node nor any of
     * its parents. The found path otherwise.
     */
    private static Optional<Path> getRoot(final List<String> paths, final boolean strict) {
        Preferences target = getRootNode();
        Path root = getPathOrNull(target);
        Path tmpRoot;
        if (paths != null && !paths.isEmpty()) {
            for (final String str : paths) {
                if (str != null && !str.isEmpty()) {
                    target = target.node(str);
                    tmpRoot = getPathOrNull(target);
                    if (strict || tmpRoot != null) {
                        root = tmpRoot;
                    }
                }
            }
        }

        return Optional.ofNullable(root);
    }

    /**
     * Try to convert input string into absolute path. If it's not possible, a
     * null value is returned.
     *
     * @param strPath The string to convert into path.
     * @return The read path, or null if an error happened.
     */
    private static Path getPathOrNull(final Preferences node) {
        if (node != null) {
            final String strValue = node.get(DEFAULT_ROOT, null);
            if (strValue != null) {
                try {
                    return Paths.get(strValue).toAbsolutePath();
                } catch (Exception e) {
                    SirsCore.LOGGER.log(Level.FINE, "Unreadable path !", e);
                }
            }
        }

        return null;
    }

    /**
     * Return preference node which contains all configured document roots.
     *
     * @return Root node. If it does not exists yet, it is created and returned
     * (empty).
     */
    private static Preferences getRootNode() {
        return Preferences.userNodeForPackage(DocumentRoots.class);
    }

    public static void setDefaultRoot(Path toSet) {
        setRoot(toSet, (String[]) null);
    }

    /**
     * Update root folder to use for {@link AbstractPhoto} bound to objects of
     * given type.
     *
     * @param toSet The path to put as new root.
     * @param associatedType The type of object containing the photos which have
     * to use the root.
     */
    public static void setPhotoRoot(final Path toSet, final Class<? extends AvecPhotos> associatedType) {
        if (associatedType == null) {
            setDefaultPhotoRoot(toSet);
        } else {
            setRoot(toSet, AbstractPhoto.class.getCanonicalName(), associatedType.getCanonicalName());
        }
    }

    /**
     * Update default root to use for photo objects, if no root is present for a
     * specific photo container.
     *
     * @param toSet The path to put as new root.
     */
    public static void setDefaultPhotoRoot(final Path toSet) {
        setRoot(toSet, AbstractPhoto.class.getCanonicalName());
    }

    /**
     * Update root folder to use for documents of the given type.
     *
     * @param toSet The path to put as new root.
     * @param refType Type of document to use the root with. Cannot be null.
     */
    public static void setDocumentRoot(final Path toSet, final Class<? extends SIRSFileReference> refType) {
        ArgumentChecks.ensureNonNull("Type de document associé", refType);
        setRoot(toSet, refType.getCanonicalName());
    }

    /**
     * Set root path property for the node specified by given path.
     *
     * @param toSet The path to register as file root.
     * @param paths The path to the node to set, going from root node.
     */
    private static void setRoot(Path toSet, final String... paths) {
        if (toSet != null) {
            toSet = toSet.toAbsolutePath();
            if (!Files.isDirectory(toSet)) {
                throw new IllegalArgumentException("Le chemin fourni n'est pas un dossier. Il ne peut être utilisé comme racine.");
            }
        }

        Preferences target = getRootNode();
        if (paths != null && paths.length > 0) {
            for (final String str : paths) {
                if (str != null && !str.isEmpty()) {
                    target = target.node(str);
                }
            }
        }

        if (toSet == null) {
            target.remove(DEFAULT_ROOT);
        } else {
            target.put(DEFAULT_ROOT, toSet.toString());
        }
    }

    public static void flush() throws BackingStoreException {
        getRootNode().flush();
    }
}
