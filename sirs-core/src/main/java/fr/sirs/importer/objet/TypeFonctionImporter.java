package fr.sirs.importer.objet;

import fr.sirs.core.model.RefFonction;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeFonctionImporter extends GenericTypeReferenceImporter<RefFonction> {

    @Override
    public String getTableName() {
        return TYPE_FONCTION.toString();
    }

    @Override
    protected Class<RefFonction> getElementClass() {
	return RefFonction.class;
    }
}
