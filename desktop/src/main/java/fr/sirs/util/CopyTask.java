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
import javafx.application.Platform;
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
 * A task whose role is to copy all files under application root path to the
 * given directory. If target directory does not exists, it will be created.
 * If a conflict is detected dduring the operation, user is asked what to do
 * (replace, ignore, cancel).
 *
 * @author Alexis Manin (Geomatys)
 */
public class CopyTask extends Task<Boolean> {

        private final Collection<Path> toCopy;
        private final Path destination;

        /**
         * Create a new task which is parametered to copy each file of the given
         * collection into destination directory.
         *
         * @param toCopy List of files or folders to copy. It must be paths retrieved
         * using {@link SIRS#getDocumentAbsolutePath(java.lang.String) }.
         * @param destination The output directory.
         */
        public CopyTask(final Collection<Path> toCopy, final Path destination) {
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

            Path srcRoot = SIRS.getDocumentRootPath();
            final Thread currentThread = Thread.currentThread();
            for (final Path p : inputs) {
                if (currentThread.isInterrupted() || isCancelled()) {
                    return false;
                }
                Path target = destination.resolve(srcRoot.relativize(p));

                updateProgress(0, inputs.size());
                updateMessage("Copie de " + p.toString() + " vers " + target.toString());

                if (!Files.isDirectory(target.getParent())) {
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
                    ButtonType replace = new ButtonType("Remplacer");
                    ButtonType ignore = new ButtonType("Ignorer");
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
