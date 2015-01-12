
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReferencePane extends BorderPane {
    
    private final PojoTable references;
    private final Session session = Injector.getSession();
        
    public FXReferencePane(final Class type) {
        references = new PojoTable(Injector.getSession().getRepositoryForClass(type), type.getSimpleName());
        references.editableProperty().bind(session.nonGeometryEditionProperty());
        references.fichableProperty().set(false);
        references.detaillableProperty().set(false);
        references.searchableProperty().set(false);
        this.setCenter(references);
    }
}
