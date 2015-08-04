/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import fr.sirs.SIRS;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 * A task whose role is to copy all files / folders in input to the
 * given directory. If target directory does not exists, it will be created.
 * If a conflict is detected dduring the operation, user is asked what to do
 * (replace, ignore, cancel).
 *
 * Note : If you set a path resolver, using
 *
 * @author Alexis Manin (Geomatys)
 */
public class CopyTask extends Task<Boolean> {

        private final Collection<Path> toCopy;
        private final Path destination;

        /**
         * A function whose role is to analyze input paths to give a new path, relative
         * to destination directory. The returned path will be used as new location for
         * input file.
         * This resolver can be used to specify a sub-directory to copy into destination,
         * or simply to rename input files are they're copied.
         */
        private final SimpleObjectProperty<Function<Path, Path>> pathResolver = new SimpleObjectProperty<>();

        /**
         * Create a new task which is parametered to copy each file of the given
         * collection into destination directory.
         *
         * @param toCopy List of files or folders to copy. It must be absolute paths.
         * @param destination The output directory.
         */
        public CopyTask(final Collection<Path> toCopy, final Path destination) {
            this(toCopy, destination, null);
        }

        public CopyTask(final Collection<Path> toCopy, final Path destination, final Function<Path, Path> resolver) {
            ArgumentChecks.ensureNonNull("Files to copy", toCopy);
            ArgumentChecks.ensureNonNull("Destination", destination);
            if (toCopy.isEmpty()) {
                throw new IllegalArgumentException("No file to copy");
            }

            if (Files.isRegularFile(destination)) {
                throw new IllegalArgumentException("Destination path is not a directory !");
            }
            this.toCopy = toCopy;
            this.destination = destination;

            pathResolver.set(resolver);
        }

        /**
         * @return the function used for making relative paths from input absolute paths. It's
         * needed if you want input files not to be copied directly in destination
         * directory. All path fragment under relativized paths are reproduced into
         * destination.
         */
        public Function<Path, Path> getPathResolver() {
            return pathResolver.get();
        }

        /**
         *
         * @param resolver the function to use to relativize input paths, in order to
         * make them relative to the given destination. It allow us to copy files
         * in sub-directories of destination, or to rename them when copied. If
         * null, no rename will be applied nor sub-directory created.
         */
        public void setPathResolver(final Function<Path, Path> resolver) {
            pathResolver.set(resolver);
        }

        /**
         * @return the function used for making relative paths from input absolute paths. It's
         * needed if you want input files not to be copied directly in destination
         * directory. All path fragment under relativized paths are reproduced into
         * destination.
         */
        public ObjectProperty<Function<Path, Path>> pathResolverProperty() {
            return pathResolver;
        }

        @Override
        protected Boolean call() throws Exception {
            updateTitle("Copie vers " + destination.toString());

            updateMessage("Analyse des fichiers d'entrée.");
            final HashSet<Path> ignored = new HashSet<>();
            final HashSet<Path> inputs = new HashSet<>();
            for (final Path p : toCopy) {
                if (Files.isRegularFile(p)) {
                    inputs.add(p);
                } else if (Files.isDirectory(p)) {
                    Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            inputs.add(file);
                            return super.visitFile(file, attrs);
                        }
                    });
                } else {
                    ignored.add(p);
                }
            }

            if (!ignored.isEmpty()) {
                TaskManager.MockTask t = new TaskManager.MockTask(() -> {
                    final Alert alert = new Alert(Alert.AlertType.WARNING, null, ButtonType.OK);
                    alert.setHeaderText("Les fichiers suivants ne seront pas copiés car ce ne sont pas des dossiers ou fichiers lisibles.");
                    alert.getDialogPane().setContent(new ListView(FXCollections.observableArrayList(ignored)));
                    alert.setResizable(true);
                    return alert.showAndWait();
                });
                Platform.runLater(t);
                t.get();
            }

            boolean replaceAll = false;
            boolean ignoreAll = false;

            final Function<Path, Path> resolver = pathResolver.get();

            final Thread currentThread = Thread.currentThread();
            int progress = 0;
            for (final Path p : inputs) {
                if (currentThread.isInterrupted() || isCancelled()) {
                    return false;
                }

                // If no resolver has beeen given, we put input file directly in
                // destination folder. otherwise, we reproduce folder structure
                // from given resolver to current input file.
                Path target = destination.resolve(resolver == null? p.getFileName() : resolver.apply(p));

                updateProgress(progress++, inputs.size());
                updateMessage("Copie de\n" + p.toString() + "\nvers\n" + target.toString());

                if (resolver != null && !Files.isDirectory(target.getParent())) {
                    Files.createDirectories(target.getParent());
                }

                try {
                    if (replaceAll) {
                        Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.copy(p, target);
                    }
                } catch (FileAlreadyExistsException e) {
                    if (ignoreAll) {
                        continue;
                    }

                    /*
                     * If we cannot copy file because it already exists, we ask
                     * user if we must replace or ignore file, or just stop here.
                     * We also propose user to repeat the same operation for all
                     * future conflicts.
                     */
                    final StringBuilder strBuilder = new StringBuilder("Impossible de copier \n")
                            .append('\t').append(p.toString()).append('\n')
                            .append("vers")
                            .append('\t').append(target.toString()).append('\n')
                            .append("Le fichier existe déjà. Voulez-vous le remplacer ?");

                    BasicFileAttributes srcAttr = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
                    BasicFileAttributes dstAttr = Files.getFileAttributeView(target, BasicFileAttributeView.class).readAttributes();

                    final Label header = new Label("Impossible de copier un fichier car il existe déjà dans le dossier destination.\nVoulez-vous le remplacer ?");
                    // source file information
                    final GridPane srcInfo = new GridPane();
                    srcInfo.add(new Label(p.toString(), new ImageView(SIRS.ICON_FILE_BLACK)), 0, 0, 2, 1);
                    srcInfo.add(new Label("Taille du fichier : "), 0, 1);
                    srcInfo.add(new Label(SIRS.toReadableSize(srcAttr.size())), 1, 1);
                    srcInfo.add(new Label("Dernière modification : "), 0, 2);
                    srcInfo.add(new Label(Timestamp.from(srcAttr.lastModifiedTime().toInstant()).toLocalDateTime().toString()), 1, 2);
                    srcInfo.setPadding(new Insets(10));

                    // destination file information
                    final GridPane dstInfo = new GridPane();
                    dstInfo.add(new Label(target.toString(), new ImageView(SIRS.ICON_FILE_BLACK)), 0, 0, 2, 1);
                    dstInfo.add(new Label("Taille du fichier : "), 0, 1);
                    dstInfo.add(new Label(SIRS.toReadableSize(dstAttr.size())), 1, 1);
                    dstInfo.add(new Label("Dernière modification : "), 0, 2);
                    dstInfo.add(new Label(Timestamp.from(dstAttr.lastModifiedTime().toInstant()).toLocalDateTime().toString()), 1, 2);
                    dstInfo.setPadding(new Insets(10));

                    final CheckBox repeat = new CheckBox("Appliquer ce choix pour les futurs conflits");

                    final BorderPane msgDisplay = new BorderPane(new Label("vers"), header, dstInfo, repeat, srcInfo);

                    final ButtonType replace = new ButtonType("Remplacer");
                    final ButtonType ignore = new ButtonType("Ignorer");
                    final Task<ButtonType> askUser = new TaskManager.MockTask(() -> {
                        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Un conflit a été détecté", ButtonType.CANCEL, ignore, replace);
                        alert.getDialogPane().setContent(new VBox(10, msgDisplay, repeat));
                        alert.setResizable(true);
                        return alert.showAndWait().orElse(ButtonType.CANCEL);
                    });
                    Platform.runLater(askUser);
                    final ButtonType result = askUser.get();
                    if (ButtonType.CANCEL.equals(result)) {
                        throw e;
                    } else if (replace.equals(result)) {
                        Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
                        if (repeat.isSelected()) {
                            replaceAll = true;
                        }
                    } else {
                        if (repeat.isSelected()) {
                            ignoreAll = true;
                        }
                    }
                }
            }
            return true;
        }
    }
