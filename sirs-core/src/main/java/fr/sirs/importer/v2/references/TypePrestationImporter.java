package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefPrestation;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypePrestationImporter extends GenericTypeReferenceImporter<RefPrestation> {

    @Override
    public String getTableName() {
        return TYPE_PRESTATION.toString();
    }

    @Override
    public Class<RefPrestation> getElementClass() {
        return RefPrestation.class;
    }
}
