package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefUtilisationConduite;
import static fr.sirs.importer.DbImporter.TableName.UTILISATION_CONDUITE;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class UtilisationConduiteImporter extends GenericTypeReferenceImporter<RefUtilisationConduite> {

    @Override
    public String getTableName() {
        return UTILISATION_CONDUITE.toString();
    }

    @Override
    protected Class<RefUtilisationConduite> getOutputClass() {
	return RefUtilisationConduite.class;
    }
}
