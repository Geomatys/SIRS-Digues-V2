package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefTypeGlissiere;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeGlissiereImporter extends GenericTypeReferenceImporter<RefTypeGlissiere> {

    @Override
    public String getTableName() {
        return TYPE_GLISSIERE.toString();
    }

    @Override
    protected Class<RefTypeGlissiere> getDocumentClass() {
	return RefTypeGlissiere.class;
    }
}
