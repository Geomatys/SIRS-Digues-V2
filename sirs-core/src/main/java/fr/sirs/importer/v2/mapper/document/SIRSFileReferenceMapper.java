/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SIRSFileReferenceMapper extends AbstractMapper<SIRSFileReference> {

    private static final String COLUMN_NAME = "REFERENCE_NUMERIQUE";

    public SIRSFileReferenceMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, SIRSFileReference output) throws IllegalStateException, IOException, AccessDbImporterException {
        String ref = input.getString(COLUMN_NAME);
        if (ref != null) {
            output.setChemin(ref);
        }
    }

    public static class SIRSReferenceMapperSpi implements MapperSpi<SIRSFileReference> {

        @Override
        public Optional<Mapper<SIRSFileReference>> configureInput(Table inputType) throws IllegalStateException {
            if (inputType.getColumn(COLUMN_NAME) != null) {
                return Optional.of(new SIRSFileReferenceMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<SIRSFileReference> getOutputClass() {
            return SIRSFileReference.class;
        }

    }
}
