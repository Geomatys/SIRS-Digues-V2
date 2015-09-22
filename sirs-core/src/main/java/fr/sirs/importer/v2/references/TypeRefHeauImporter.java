package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefReferenceHauteur;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeRefHeauImporter extends GenericTypeReferenceImporter<RefReferenceHauteur> {

    @Override
    public String getTableName() {
        return TYPE_REF_HEAU.toString();
    }

    @Override
    protected Class<RefReferenceHauteur> getElementClass() {
	return RefReferenceHauteur.class;
    }

}
