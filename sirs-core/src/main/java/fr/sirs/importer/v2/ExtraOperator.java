package fr.sirs.importer.v2;

import fr.sirs.importer.AccessDbImporterException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public interface ExtraOperator {

    public void compute() throws AccessDbImporterException;
}
