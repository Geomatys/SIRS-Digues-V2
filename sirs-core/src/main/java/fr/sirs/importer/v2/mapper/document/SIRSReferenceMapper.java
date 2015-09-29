/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.SIRSReference;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SIRSReferenceMapper extends AbstractMapper<SIRSReference> {

    private static final String COLUMN_NAME = "REFERENCE_PAPIER";

    public SIRSReferenceMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, SIRSReference output) throws IllegalStateException, IOException, AccessDbImporterException {
        String ref = input.getString(COLUMN_NAME);
        if (ref != null) {
            output.setReferencePapier(ref);
        }
    }

    @Component
    public static class SIRSReferenceMapperSpi implements MapperSpi<SIRSReference> {

        @Override
        public Optional<Mapper<SIRSReference>> configureInput(Table inputType) throws IllegalStateException {
            if (ImportContext.columnExists(inputType, COLUMN_NAME)) {
                return Optional.of(new SIRSReferenceMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<SIRSReference> getOutputClass() {
            return SIRSReference.class;
        }

    }
}
