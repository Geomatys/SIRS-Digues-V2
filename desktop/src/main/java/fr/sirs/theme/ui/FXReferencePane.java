
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import java.util.ResourceBundle;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReferencePane extends BorderPane {
    
    private final PojoTable references;
    private final Session session = Injector.getSession();
        
    public FXReferencePane(final Class type) {
        final ResourceBundle bundle = ResourceBundle.getBundle(type.getName());
        references = new PojoTable(Injector.getSession().getRepositoryForClass(type), bundle.getString("class"));
        references.editableProperty().bind(session.nonGeometryEditionProperty());
        references.fichableProperty().set(false);
        references.detaillableProperty().set(false);
        references.searchableProperty().set(false);
        this.setCenter(references);
    }
}
