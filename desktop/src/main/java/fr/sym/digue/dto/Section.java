

package fr.sym.digue.dto;

import javafx.beans.property.SimpleStringProperty;

/**
 * @deprecated Switch to TronconDigue as soon as possible.
 * @author Johann Sorel (Geomatys)
 */
@Deprecated
public class Section {
    
    private SimpleStringProperty name = new SimpleStringProperty();

    public SimpleStringProperty getName() {
        return name;
    }
    
}
