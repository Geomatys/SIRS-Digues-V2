
package fr.sirs.plugin.document;

import java.io.File;
import javafx.scene.control.TreeItem;

/**
 *
 * @author guilhem
 */
public class FileTreeItem extends TreeItem<File> {
    
    
    public FileTreeItem(File item) {
        super(item);

        if (item.isDirectory()) {
            for (File f : item.listFiles()) {
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
    
}
