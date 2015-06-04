package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.core.model.SIRSDocument;
import fr.sirs.theme.ui.PojoTable;
import javafx.scene.Parent;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public class DocumentTheme<T extends SIRSDocument> extends Theme {

    private final Class<T> documentClass;
    
    public DocumentTheme(final String name, final Class<T> documentClass) {
        super(name, Type.UNLOCALIZED);
        this.documentClass = documentClass;
    }

    @Override
    public Parent createPane() {
        return new PojoTable(Injector.getSession().getRepositoryForClass(documentClass), getName());
    }
    
}
