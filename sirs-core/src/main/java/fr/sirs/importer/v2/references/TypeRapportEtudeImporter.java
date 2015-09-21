package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefRapportEtude;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeRapportEtudeImporter extends GenericTypeReferenceImporter<RefRapportEtude> {

    @Override
    public String getTableName() {
        return TYPE_RAPPORT_ETUDE.toString();
    }

    @Override
    protected Class<RefRapportEtude> getElementClass() {
	return RefRapportEtude.class;
    }
}
