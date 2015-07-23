
package fr.sirs.plugin.document;

import fr.sirs.plugin.document.ui.DocumentsPane;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.scene.control.TreeItem;

/**
 * Tree item used in the tree-table representing the documents.
 * 
 * @author Guilhem Legal (Geomatys)
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
    
    /**
     * List the files in the specified directory and order them.
     * 
     * @param directory
     * @return 
     */
    private List<File> listFiles(File directory) {
        final List<File> result = Arrays.asList(directory.listFiles());
        Collections.sort(result, new FileComparator());
        return result;
    }
    
    /**
     * Update the current items. 
     */
    public void update() {
        if (getValue().isDirectory()) {
            List<FileTreeItem> children = listChildrenItem();
            for (File f : listFiles(getValue())) {
                if (!f.getName().equals("sirs.properties")) {
                    FileTreeItem item = getChildrenItem(f);
                    if (item == null) {
                        getChildren().add(new FileTreeItem(f));
                    } else {
                        children.remove(item);
                        item.update();
                    }
                }
            }
            // remove the destroyed node
            for (FileTreeItem item : children) {
                getChildren().remove(item);
            }
        }
    }
    
    /**
     * Return The Tree item correspounding to the specified file in the current node children.
     * 
     * @param f
     * @return 
     */
    private FileTreeItem getChildrenItem(final File f) {
        for (TreeItem item : getChildren()) {
            FileTreeItem fitem = (FileTreeItem) item;
            if (fitem.getValue().getPath().equals(f.getPath())) {
                return fitem;
            }
        }
        return null;
    }
    
    /**
     * Return a list of the children items.
     * 
     * @return 
     */
    public List<FileTreeItem> listChildrenItem() {
        final List<FileTreeItem> results = new ArrayList<>();
        for (TreeItem item : getChildren()) {
            results.add((FileTreeItem) item);
        }
        return results;
    }
    
    public List<FileTreeItem> listChildrenItem(boolean directory) {
        final List<FileTreeItem> results = new ArrayList<>();
        for (TreeItem item : getChildren()) {
            final FileTreeItem fitem = (FileTreeItem) item;
            if (fitem.isDirectory() && directory || 
                !fitem.isDirectory() && !directory ) {
                results.add(fitem);
            }
        }
        return results;
    }
    
    public String getLibelle() {
        final File f = getValue();
        String name = PropertiesFileUtilities.getProperty(getValue(), DocumentsPane.LIBELLE);
        if (name.isEmpty()) {
            name = f.getName();
        }
        return name;
    }
    
    public String getSize() {
        return PropertiesFileUtilities.getStringSizeFile(getValue());
    }
    
    public String getInventoryNumber() {
        return PropertiesFileUtilities.getProperty(getValue(), DocumentsPane.INVENTORY_NUMBER);
    }
    
    public String getClassPlace() {
        return PropertiesFileUtilities.getProperty(getValue(), DocumentsPane.CLASS_PLACE);
    }
    
    public boolean isDirectory() {
        return getValue().isDirectory();
    }
            
    private static class FileComparator implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 != null && o2 != null) {
                if (o1.getName().equals(DocumentsPane.SAVE_FOLDER)) {
                    return 1;
                } else if (o1.getName().equals(DocumentsPane.UNCLASSIFIED)) {
                    return 1;
                } else if (o2.getName().equals(DocumentsPane.SAVE_FOLDER)) {
                    return -1;
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
