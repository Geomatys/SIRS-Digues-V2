package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefSystemeReleveProfil;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeSystemeReleveProfilImporter extends GenericTypeReferenceImporter<RefSystemeReleveProfil> {

    @Override
    public String getTableName() {
        return TYPE_SYSTEME_RELEVE_PROFIL.toString();
    }

    @Override
    protected Class<RefSystemeReleveProfil> getDocumentClass() {
	return RefSystemeReleveProfil.class;
    }
}
