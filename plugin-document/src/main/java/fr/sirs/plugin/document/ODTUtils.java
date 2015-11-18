
package fr.sirs.plugin.document;

import fr.sirs.plugin.document.ui.DocumentsPane;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ODTUtils extends fr.sirs.util.odt.ODTUtils {

    public static void writeSummary(final FileTreeItem item, File file) throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();
        for (FileTreeItem child : item.listChildrenItem()) {
            if (!child.getLibelle().equals(DocumentsPane.SAVE_FOLDER)) {
                write(doc, child, false, null);
            }
        }
        doc.save(file);
    }

    public static void writeDoSynth(final FileTreeItem item, File file, final Label uiProgressLabel) throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();
        write(doc, (FileTreeItem) item, true, uiProgressLabel);
        doc.save(file);
    }

    private static void write(final TextDocument doc, final FileTreeItem item, boolean doSynth, final Label uiProgressLabel) throws Exception {
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
                final String prefix = item.getLibelle() + " Concatenation des fichiers : ";
                final int n = files.size();
                int i = 1;
                for (FileTreeItem child : files) {
                    final int I = i++;
                    Platform.runLater(() -> uiProgressLabel.setText(prefix + I + "/" + n));
                    doc.addParagraph(child.getLibelle()).applyHeading(true, 6);
                    append(doc, child.getValue());
                }
            } else {
                final Table table = Table.newTable(doc);
                // header
                Row row = table.getRowByIndex(0);
                row.setDefaultCellStyle(getOrCreateTableHeaderStyle(doc));
                row.getCellByIndex(0).setStringValue("Nom");
                row.getCellByIndex(1).setStringValue("Taille");
                row.getCellByIndex(2).setStringValue("N° Inventaire");
                row.getCellByIndex(3).setStringValue("Lieu classement");

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
            write(doc, child, doSynth, uiProgressLabel);
        }
    }
}
