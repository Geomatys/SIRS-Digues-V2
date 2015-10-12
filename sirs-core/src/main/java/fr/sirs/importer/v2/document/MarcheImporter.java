package fr.sirs.importer.v2.document;

import fr.sirs.core.model.Marche;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MarcheImporter extends AbstractImporter<Marche> {

    @Autowired
    DocTypeRegistry registry;

    @Override
    public Class<Marche> getElementClass() {
        return Marche.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.MARCHE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_MARCHE";
    }
}
