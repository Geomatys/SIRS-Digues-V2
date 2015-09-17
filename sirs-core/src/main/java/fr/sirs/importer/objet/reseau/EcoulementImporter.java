package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefEcoulement;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class EcoulementImporter extends GenericTypeReferenceImporter<RefEcoulement> {

    @Override
    public String getTableName() {
        return ECOULEMENT.toString();
    }

    @Override
    protected Class<RefEcoulement> getDocumentClass() {
	return RefEcoulement.class;RefEcoulement.class;
    }
}
