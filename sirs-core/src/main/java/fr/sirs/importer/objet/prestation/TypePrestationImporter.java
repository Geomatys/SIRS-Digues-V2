package fr.sirs.importer.objet.prestation;

import fr.sirs.core.model.RefPrestation;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypePrestationImporter extends GenericTypeReferenceImporter<RefPrestation> {

    @Override
    public String getTableName() {
        return TYPE_PRESTATION.toString();
    }

    @Override
    protected Class<RefPrestation> getDocumentClass() {
	return RefPrestation.class;
    }
}
