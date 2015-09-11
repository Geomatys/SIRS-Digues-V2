package fr.sirs.importer.objet.structure;

import fr.sirs.core.model.ObjetStructure;
import fr.sirs.importer.objet.*;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericStructureImporter<T extends ObjetStructure> extends GenericObjetImporter<T> {

    private enum Columns {
        ID_ELEMENT_STRUCTURE
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ELEMENT_STRUCTURE.name();
    }

}
