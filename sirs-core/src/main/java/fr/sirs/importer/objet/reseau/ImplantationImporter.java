package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefImplantation;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

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
    protected Class<RefImplantation> getElementClass() {
	return RefImplantation.class;
    }


}
