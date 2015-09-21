package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefReseauHydroCielOuvert;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeReseauEauImporter extends GenericTypeReferenceImporter<RefReseauHydroCielOuvert> {

    @Override
    public String getTableName() {
        return TYPE_RESEAU_EAU.toString();
    }

    @Override
    protected Class<RefReseauHydroCielOuvert> getElementClass() {
	return RefReseauHydroCielOuvert.class;
    }
}
