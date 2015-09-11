package fr.sirs.importer.evenementHydraulique.meteo;

import fr.sirs.core.model.RefOrientationVent;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeOrientationVentImporter extends GenericTypeReferenceImporter<RefOrientationVent> {

    @Override
    public String getTableName() {
        return TYPE_ORIENTATION_VENT.toString();
    }

    @Override
    protected Class<RefOrientationVent> getOutputClass() {
	return RefOrientationVent.class;
    }
}
