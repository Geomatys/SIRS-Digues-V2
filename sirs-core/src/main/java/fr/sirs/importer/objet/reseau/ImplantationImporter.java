package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefImplantation;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class ImplantationImporter extends GenericTypeReferenceImporter<RefImplantation> {

    @Override
    public String getTableName() {
        return IMPLANTATION.toString();
    }

    @Override
    protected Class<RefImplantation> getOutputClass() {
	return RefImplantation.class;
    }


}
