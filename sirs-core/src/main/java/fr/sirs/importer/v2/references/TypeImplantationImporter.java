package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefImplantation;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeImplantationImporter extends GenericTypeReferenceImporter<RefImplantation> {

    @Override
    public String getTableName() {
        return IMPLANTATION.toString();
    }

    @Override
    protected Class<RefImplantation> getElementClass() {
        return RefImplantation.class;
    }

}
