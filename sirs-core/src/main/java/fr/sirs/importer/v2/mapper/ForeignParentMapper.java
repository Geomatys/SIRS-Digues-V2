/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ForeignParentMapper extends AbstractMapper<AvecForeignParent> {

    private static final String TRONCON_ID_COLUMN = "ID_TRONCON_GESTION";
    private final AbstractImporter<TronconDigue> tdImporter;

    private ForeignParentMapper(final Table t) {
        super(t);
        tdImporter = context.importers.get(TronconDigue.class);
        if (tdImporter == null) {
            throw new IllegalStateException("Missing import resource !");
        }
    }

    @Override
    public void map(Row input, AvecForeignParent output) throws IllegalStateException, IOException, AccessDbImporterException {
        Integer tdId = input.getInt(TRONCON_ID_COLUMN);
        if (tdId != null) {
            final String importedId = tdImporter.getImportedId(tdId);
            if (importedId != null) {
                output.setForeignParentId(importedId);
                return;
            }
        }
        throw new AccessDbImporterException("Input row does not reference any valid " + TRONCON_ID_COLUMN);
    }

    public static class Spi implements MapperSpi<AvecForeignParent> {

        @Override
        public Optional<Mapper<AvecForeignParent>> configureInput(Table inputType) throws IllegalStateException {
            if (inputType.getColumn(TRONCON_ID_COLUMN) != null) {
                return Optional.of(new ForeignParentMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<AvecForeignParent> getOutputClass() {
            return AvecForeignParent.class;
        }
    }
}
