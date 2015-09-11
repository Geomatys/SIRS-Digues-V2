package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefSeuil;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeSeuilImporter extends GenericTypeReferenceImporter<RefSeuil> {

    @Override
    public String getTableName() {
        return TYPE_SEUIL.toString();
    }

    @Override
    protected Class<RefSeuil> getOutputClass() {
	return RefSeuil.class;
    }
}
