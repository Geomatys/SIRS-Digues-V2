package fr.sirs.importer.v2.objet;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class StructureImporter extends AbstractImporter<Objet> {

    private enum Columns {
        ID_ELEMENT_STRUCTURE,
        ID_TYPE_ELEMENT_STRUCTURE
    }

    @Autowired
    private StructureRegistry registry;

    @Override
    protected Class<Objet> getElementClass() {
        return Objet.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ELEMENT_STRUCTURE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ELEMENT_STRUCTURE.name();
    }

    @Override
    protected Objet getOrCreateElement(Row input) {
        Integer type = input.getInt(Columns.ID_TYPE_ELEMENT_STRUCTURE.toString());
        if (type == null) {
            throw new IllegalArgumentException("No valid element type in row "+input.getInt(getRowIdFieldName())+ " of table "+getTableName());
        }

        // Find what type of element must be imported.
        Class<Objet> clazz = registry.getElementType(type);
        if (clazz == null) {
            throw new IllegalArgumentException("No valid element type in row "+input.getInt(getRowIdFieldName())+ " of table "+getTableName());
        }

        return ElementCreator.createAnonymValidElement(clazz);
    }
}
