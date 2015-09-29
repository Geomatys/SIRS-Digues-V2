package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOrientationVent;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypeOrientationVentImporter extends GenericTypeReferenceImporter<RefOrientationVent> {

    @Override
    public String getTableName() {
        return TYPE_ORIENTATION_VENT.toString();
    }

    @Override
    protected Class<RefOrientationVent> getElementClass() {
        return RefOrientationVent.class;
    }
}
