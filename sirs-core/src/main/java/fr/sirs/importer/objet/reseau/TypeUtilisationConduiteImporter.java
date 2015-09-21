package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefUtilisationConduite;
import static fr.sirs.importer.DbImporter.TableName.UTILISATION_CONDUITE;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeUtilisationConduiteImporter extends GenericTypeReferenceImporter<RefUtilisationConduite> {

    @Override
    public String getTableName() {
        return UTILISATION_CONDUITE.toString();
    }

    @Override
    protected Class<RefUtilisationConduite> getElementClass() {
	return RefUtilisationConduite.class;
    }
}
