
package fr.sirs.plugin.document;

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
    
    public static void write(final FileTreeItem item, File file) throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();
        for (FileTreeItem child : item.listChildrenItem()) {
            write(doc, child, 4, "");
        }
        doc.save(file);
    }
    
    public static void write(final TextDocument doc, final FileTreeItem item, int level, final String margin) {
        final Paragraph paragraph = doc.addParagraph("");
        paragraph.applyHeading(true, level);
        paragraph.appendTextContent(margin);
        
        paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.REGULAR, 12, Color.BLACK, StyleTypeDefinitions.TextLinePosition.THROUGHUNDER));
        
        paragraph.appendTextContent(item.getLibelle());
        
        
                
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
        }
        
        for (FileTreeItem child : directories) {
            write(doc, child, level + 1, "\t" + margin);
        }
    }
}
