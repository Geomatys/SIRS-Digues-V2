package fr.sirs.importer.v2.linear.management;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class GenericPeriodeLocaliseeImporter<T extends Element> extends AbstractImporter<T> {

    protected AbstractImporter<TronconDigue> tdImporter;

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        tdImporter = context.importers.get(TronconDigue.class);
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        tdImporter = null;
    }
}
