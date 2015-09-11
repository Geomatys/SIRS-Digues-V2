package fr.sirs.importer.objet.geometry;

import fr.sirs.core.model.Objet;
import fr.sirs.core.model.RefLargeurFrancBord;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.objet.*;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericGeometrieImporter<T extends Objet> extends GenericObjetImporter<T> {

    protected GenericImporter<RefLargeurFrancBord> typeLargeurFrancBordImporter;

    private enum Columns {
        ID_ELEMENT_GEOMETRIE
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ELEMENT_GEOMETRIE.name();
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        typeLargeurFrancBordImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        typeLargeurFrancBordImporter = context.importers.get(RefLargeurFrancBord.class);
    }


}
