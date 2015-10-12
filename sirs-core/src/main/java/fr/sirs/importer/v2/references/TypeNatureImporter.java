package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefNature;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypeNatureImporter extends GenericTypeReferenceImporter<RefNature> {

    @Override
    public String getTableName() {
        return TYPE_NATURE.toString();
    }

    @Override
    public Class<RefNature> getElementClass() {
        return RefNature.class;
    }

}
