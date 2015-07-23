
package fr.sirs.plugin.document;

import fr.sirs.plugin.document.ui.DocumentsPane;
import java.io.File;
import java.util.List;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ODTUtils {
    
    private static String TAB = "        ";
    
    public static void write(final FileTreeItem item, File file) throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();
        for (FileTreeItem child : item.listChildrenItem()) {
            if (!child.getLibelle().equals(DocumentsPane.SAVE_FOLDER)) {
                write(doc, child, "");
            }
        }
        doc.save(file);
    }
    
    public static void write(final TextDocument doc, final FileTreeItem item, String margin) {
        final Paragraph paragraph = doc.addParagraph("");
        
        if (item.isSe()) {
            paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 22, Color.BLACK, StyleTypeDefinitions.TextLinePosition.UNDER));
            margin = "";
            paragraph.appendTextContent(margin + item.getLibelle() + "\n");
            
        } else if (item.isDg()) {
            paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 18, Color.BLACK, StyleTypeDefinitions.TextLinePosition.UNDER));
            margin = "";
            paragraph.appendTextContent(margin + item.getLibelle() + "\n");
        } else if (item.isTr()) {
            paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 14, Color.BLACK, StyleTypeDefinitions.TextLinePosition.UNDER));
            margin = "";
            paragraph.appendTextContent(margin + item.getLibelle() + "\n");
        } else {
            paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 12, Color.BLACK, StyleTypeDefinitions.TextLinePosition.REGULAR));
            paragraph.appendTextContent(margin + " - " + item.getLibelle() + "\n");
        }
                
        List<FileTreeItem> directories = item.listChildrenItem(true);
        List<FileTreeItem> files       = item.listChildrenItem(false);
        
        if (!files.isEmpty()) {
            final Table table = Table.newTable(doc, files.size() + 1, 4);
            table.getCellByPosition(0, 0).setStringValue("Nom");
            table.getCellByPosition(0, 0).setCellBackgroundColor(new Color(109,149,182));
            
            table.getCellByPosition(1, 0).setStringValue("Taille");
            table.getCellByPosition(1, 0).setCellBackgroundColor(new Color(109,149,182));
            
            table.getCellByPosition(2, 0).setStringValue("N° Inventaire");
            table.getCellByPosition(2, 0).setCellBackgroundColor(new Color(109,149,182));
            
            table.getCellByPosition(3, 0).setStringValue("Lieu classement");
            table.getCellByPosition(3, 0).setCellBackgroundColor(new Color(109,149,182));
                    
            table.getRowByIndex(0).getDefaultCellStyle();
            int i = 1; 
            for (FileTreeItem child : files) {
        
                final String name      = child.getLibelle();
                final String size      = child.getSize();
                final String inventory = child.getInventoryNumber();
                final String place     = child.getClassPlace();

                table.getCellByPosition(0, i).setStringValue(name);
                table.getCellByPosition(1, i).setStringValue(size);
                table.getCellByPosition(2, i).setStringValue(inventory);
                table.getCellByPosition(3, i).setStringValue(place);

                i++;
            }
            doc.addParagraph("");
        }
        
        for (FileTreeItem child : directories) {
            write(doc, child, TAB + margin);
        }
    }
}
