
package fr.sirs.plugin.document;

import fr.sirs.plugin.document.ui.DocumentsPane;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.scene.control.TreeItem;

/**
 *
 * @author guilhem
 */
public class FileTreeItem extends TreeItem<File> {
    
    
    public FileTreeItem(File item) {
        super(item);

        if (item.isDirectory()) {
            for (File f : listFiles(item)) {
                if (!f.getName().equals("sirs.properties")) {
                    getChildren().add(new FileTreeItem(f));
                }
            }
        }
    }
    
    @Override
    public boolean isLeaf() {
        return !getValue().isDirectory();
    }
    
    private List<File> listFiles(File directory) {
        final List<File> result = Arrays.asList(directory.listFiles());
        Collections.sort(result, new FileComparator());
        return result;
    }
    
    private static class FileComparator implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 != null && o2 != null) {
                if (o1.getName().equals(DocumentsPane.UNCLASSIFIED)) {
                    return 1;
                } else if (o2.getName().equals(DocumentsPane.UNCLASSIFIED)) {
                    return -1;
                } else if (o1.getName().equals(DocumentsPane.DOCUMENT_FOLDER)) {
                    return -1;
                } else if (o2.getName().equals(DocumentsPane.DOCUMENT_FOLDER)) {
                    return 1;
                } else {
                    return o1.getName().compareTo(o2.getName());
                }
            } else if (o1 == null){
                return -1;
            } else if (o2 == null){
                return 1;
            }
            // should never happen
            throw new IllegalStateException("Error in file comparator");
        }
        
    }
}
