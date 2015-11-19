
package fr.sirs.plugin.document;

import fr.sirs.Injector;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.report.ModeleRapport;
import static fr.sirs.plugin.document.PropertiesFileUtilities.getElements;
import static fr.sirs.plugin.document.PropertiesFileUtilities.getOrCreateDG;
import static fr.sirs.plugin.document.PropertiesFileUtilities.getOrCreateTR;
import static fr.sirs.plugin.document.PropertiesFileUtilities.setBooleanProperty;
import static fr.sirs.plugin.document.PropertiesFileUtilities.setProperty;
import fr.sirs.plugin.document.ui.DocumentsPane;
import static fr.sirs.plugin.document.ui.DocumentsPane.DYNAMIC;
import static fr.sirs.plugin.document.ui.DocumentsPane.MODELE;
import static fr.sirs.util.odt.ODTUtils.generateReport;
import java.io.File;
import java.util.Collection;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ODTUtils extends fr.sirs.util.odt.ODTUtils {

    private static final String[] TABLE_HEADERS = new String[]{"Nom", "Taille", "N° Inventaire", "Lieu classement"};

    public static Task generateDocsForDigues(final String docName, boolean onlySe, final ModeleRapport modele, final Collection<TronconDigue> troncons, final File seDir,
            final FileTreeItem root) {
        return TaskManager.INSTANCE.submit(new Task() {

            @Override
            protected Object call() throws Exception {
                updateTitle("Génération d'un rapport");
                updateMessage("Recherche des objets du rapport...");
                final DigueRepository digueRepo = Injector.getBean(DigueRepository.class);
                final int total = troncons.size();
                int i = 1;
                for (TronconDigue troncon : troncons) {
                    final String prefix = "Tronçon " + i + '/' + total + System.lineSeparator();
                    final File digDir = getOrCreateDG(seDir, digueRepo.get(troncon.getDigueId()));
                    final File docDir = new File(getOrCreateTR(digDir, troncon), DocumentsPane.DOCUMENT_FOLDER);
                    final File newDoc = new File(docDir, docName);

                    final Task reportGenerator = generateReport(modele, getElements(troncons), newDoc.toPath(), root.getLibelle());
                    Platform.runLater(() -> {
                        reportGenerator.messageProperty().addListener((obs, oldValue, newValue) -> updateMessage(prefix + newValue));
                        reportGenerator.workDoneProperty().addListener((obs, oldValue, newValue) -> updateProgress(newValue.doubleValue(), reportGenerator.getTotalWork()));
                    });

                    setBooleanProperty(newDoc, DYNAMIC, true);
                    setProperty(newDoc, MODELE, modele.getId());
                    i++;
                }
                updateMessage("Génération terminée");

                // reload tree
                root.update(false);
                return true;
            }
        });
    }

    public static Task<File> generateDoc(final ModeleRapport modele, final Collection<TronconDigue> troncons, final File outputDoc, final FileTreeItem root) {
        return TaskManager.INSTANCE.submit(new Task() {

            @Override
            protected Object call() throws Exception {
                updateTitle("Génération d'un rapport");
                updateMessage("Recherche des objets du rapport...");

                final Task reportGenerator = generateReport(modele, getElements(troncons), outputDoc.toPath(), root.getLibelle());

                Platform.runLater(() -> {
                    reportGenerator.messageProperty().addListener((obs, oldValue, newValue) -> updateMessage(newValue));
                    reportGenerator.workDoneProperty().addListener((obs, oldValue, newValue) -> updateProgress(newValue.doubleValue(), reportGenerator.getTotalWork()));
                });

                setBooleanProperty(outputDoc, DYNAMIC, true);
                setProperty(outputDoc, MODELE, modele.getId());

                return outputDoc;
            }
        });
    }

    /**
     * Write index of intput tree content.
     * @param item Tree to parse.
     * @param file Location for output document.
     * @return Generated file (should be the same as input).
     */
    public static Task<File> writeSummary(final FileTreeItem item, File file) {
        return TaskManager.INSTANCE.submit(new Task<File>() {

            @Override
            protected File call() throws Exception {
                updateTitle("Génération d'un index : "+item.getLibelle());

                // Define a string which will deliver task message.
                final StringProperty msgProperty = new SimpleStringProperty();
                Platform.runLater(() -> msgProperty.addListener((obs, oldValue, newValue) -> updateMessage(newValue)));

                msgProperty.set("Initialisation du document");
                final TextDocument doc = TextDocument.newTextDocument();
                for (FileTreeItem child : item.listChildrenItem()) {
                    if (!child.getLibelle().equals(DocumentsPane.SAVE_FOLDER)) {
                        write(doc, child, false, null);
                    }
                }

                msgProperty.set("Sauvegarde du document");
                doc.save(file);

                msgProperty.set("Génération terminée");
                return file;
            }
        });
    }

    /**
     * Write a synthesis about given tree content.
     * @param item Tree to parse.
     * @param file File to put synthesis into.
     * @return Input file.
     */
    public static Task<File> writeDoSynth(final FileTreeItem item, final File file) {
        return TaskManager.INSTANCE.submit(new Task<File>() {

            @Override
            protected File call() throws Exception {
                updateTitle("Génération d'une synthèse : "+item.getLibelle());

                // Define a string which will deliver task message.
                final StringProperty msgProperty = new SimpleStringProperty();
                Platform.runLater(() -> msgProperty.addListener((obs, oldValue, newValue) -> updateMessage(newValue)));

                msgProperty.set("Initialisation du document");
                final TextDocument doc = TextDocument.newTextDocument();
                write(doc, (FileTreeItem) item, true, msgProperty);

                msgProperty.set("Sauvegarde du document");
                doc.save(file);

                msgProperty.set("Génération terminée");
                return file;
            }
        });
    }

    /**
     * Write index or synthesis of a tree item.
     * @param doc Document to append index / synthesis into.
     * @param item Tree item to parse.
     * @param doSynth True if we should write synthesis, false if we write index.
     * @param progressMessage A text property to put progress message into.
     * @throws Exception
     */
    private static void write(final TextDocument doc, final FileTreeItem item, boolean doSynth, final StringProperty progressMessage) throws Exception {
        // title
        final int headingLevel;
        if (item.isSe()) {
            headingLevel = 2;
        } else if (item.isDg()) {
            headingLevel = 3;
        } else if (item.isTr()) {
            headingLevel = 4;
        } else {
            headingLevel = 5;
        }
        doc.addParagraph(item.getLibelle()).applyHeading(true, headingLevel);

        List<FileTreeItem> directories = item.listChildrenItem(true, doSynth);
        List<FileTreeItem> files       = item.listChildrenItem(false, doSynth);
        if (!files.isEmpty()) {
            if (doSynth) {
                final String prefix = item.getLibelle() + System.lineSeparator() +"Concatenation des fichiers : ";
                final int n = files.size();
                int i = 1;
                for (FileTreeItem child : files) {
                    final int I = i++;
                    Platform.runLater(() -> progressMessage.set(prefix + I + "/" + n));
                    doc.addParagraph(child.getLibelle()).applyHeading(true, 6);
                    append(doc, child.getValue());
                }
            } else {
                final Table table = Table.newTable(doc, 1, TABLE_HEADERS.length);
                // header
                Row row = table.getRowByIndex(0);
                Cell cell;
                for (int i = 0 ; i < TABLE_HEADERS.length ; i++) {
                    cell = row.getCellByIndex(i);
                    cell.setStringValue(TABLE_HEADERS[i]);
                    cell.setCellBackgroundColor(new Color(109, 149, 182));
                }

                FileTreeItem file;
                for (int i = 0; i < files.size(); i++) {
                    file = files.get(i);
                    row = row.getNextRow();
                    row.getCellByIndex(0).setStringValue(file.getLibelle());
                    row.getCellByIndex(1).setStringValue(file.getSize());
                    row.getCellByIndex(2).setStringValue(file.getInventoryNumber());
                    row.getCellByIndex(3).setStringValue(file.getClassPlace());
                }
            }
        }

        for (FileTreeItem child : directories) {
            doc.addColumnBreak();
            write(doc, child, doSynth, progressMessage);
        }
    }
}
