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
package fr.sirs.plugins.synchro;

import fr.sirs.Plugin;
import fr.sirs.SIRS;
import java.awt.Color;
import java.io.File;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.internal.GeotkFX;

/**
 * Plugin for synchronisation between mobile application and desktop one.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class SynchroPlugin extends Plugin {

    private static final String NAME = "plugin-synchro";
    private static final String TITLE = "Synchronisation mobile";

    private static final Image PLUGIN_ICON = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_MOBILE, 100, Color.BLACK), null);

    public static final Path MOBILE_APP_DIR = Paths.get("Android/data/com.rdardie.sirsMobile");
    public static final Path DOCUMENT_FOLDER = Paths.get("files", "documents");
    public static final Path PHOTO_FOLDER = Paths.get("files", "medias");

    public SynchroPlugin() {
        name = NAME;
        loadingMessage.set("Chargement du module pour la synchronisation bureau/mobile");
        themes.add(new DocumentExportTheme());
        themes.add(new PhotoImportTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
    }

    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    @Override
    public Image getImage() {
        return PLUGIN_ICON;
    }

    /**
     * Resolve suffix path over a given prefix. If prefix path contains a part
     * of given suffix, we truncate suffix to resolve its non-common parts over
     * prefix. If the two paths have no common parts, calling this method is
     * functionally equivalent to {@link Path#resolve(java.nio.file.Path) } with
     * prefix path as caller, and suffix as parameter.
     *
     * Ex : prefix is /home/user/toto/tata suffix is tata/titi/ result will be
     * /home/user/toto/tata/titi.
     *
     * @param prefix The path which will form root part of the result.
     * @param suffix The path which will form the
     * @return
     */
    public static Path resolvePath(final Path prefix, final Path suffix) {
        Iterator<Path> fragments = suffix.iterator();
        Path searchedEnd = Paths.get("");
        while (fragments.hasNext()) {
            searchedEnd = searchedEnd.resolve(fragments.next());
            if (prefix.endsWith(searchedEnd)) {
                // Concordance found. Now we'll add remaining suffix fragments.
                Path result = prefix;
                while (fragments.hasNext()) {
                    result = result.resolve(fragments.next());
                }
                return result;
            }
        }

        // No common part found, we just resolve input paths.
        return prefix.resolve(suffix);
    }

    /**
     * Open a {@link DirectoryChooser} for user to indicates a media containing
     * SIRS mobile application.
     *
     * @param fileChooserOwner A window to set as owner for the directory chooser.
     * @return The path to SIRS mobile application in chosen media, or null if no media
     * has been chosen, or if we cannot find mobile application in it.
     */
    public static Path chooseMedia(final Window fileChooserOwner) {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir un périphérique portable.");
        chooser.setInitialDirectory(FileSystems.getDefault().getRootDirectories().iterator().next().toFile());

        final File chosen = chooser.showDialog(fileChooserOwner);
        if (chosen != null) {
            try {
                final Path chosenPath = chosen.toPath();
                FileStore fileStore = Files.getFileStore(chosenPath);
                if (fileStore.isReadOnly()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Le périphérique ne peut pas être choisi, car il est en lecture seule.", ButtonType.OK);
                    alert.setResizable(true);
                    alert.show();
                } else if (fileStore.getUsableSpace() < 1) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Le périphérique ne peut pas être choisi, car il ne reste plus de place disponible.", ButtonType.OK);
                    alert.setResizable(true);
                    alert.show();
                } else {
                    Path result = null;
                    final HashSet<Path> toIterateOn = new HashSet<>();
                    toIterateOn.add(chosenPath);
                    for (final Path root : chosenPath.getFileSystem().getRootDirectories()) {
                        toIterateOn.add(root);
                    }

                    for (final Path toAnalyze : toIterateOn) {
                        final Path appDir = SynchroPlugin.resolvePath(toAnalyze, SynchroPlugin.MOBILE_APP_DIR);
                        if (Files.isDirectory(appDir)) {
                            result = appDir;
                            break;
                        }
                    }

                    if (result == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Impossible de trouver l'application SIRS mobile sur le media choisi.", ButtonType.OK);
                        alert.setResizable(true);
                        alert.show();
                    } else {
                        return result;
                    }
                }
            } catch (Exception e) {
                SIRS.LOGGER.log(Level.WARNING, "Impossible to analyze chosen output drive.", e);
                GeotkFX.newExceptionDialog("Une erreur est survenue pendant l'analyse du média choisi.", e).show();
            }
        }
        return null;
    }
}
