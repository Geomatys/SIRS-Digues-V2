package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.Desordre;
import static fr.sirs.importer.DbImporter.TableName.DESORDRE;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class DesordreImporter extends AbstractImporter<Desordre> {

    private enum Columns {
        ID_DESORDRE
    }

    @Override
    protected Class<Desordre> getElementClass() {
        return Desordre.class;
    }

    @Override
    public String getTableName() {
        return DESORDRE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_DESORDRE.name();
    }

}
