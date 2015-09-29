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
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import fr.sirs.importer.v2.mapper.objet.PhotoColumns;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SIRSFileReferenceMapper extends AbstractMapper<SIRSFileReference> {

    private static final String DEFAULT_COLUMN_NAME = "REFERENCE_NUMERIQUE";

    private final String columnName;

    private SIRSFileReferenceMapper(Table table, final String columnName) {
        super(table);
        this.columnName = columnName;
    }

    @Override
    public void map(Row input, SIRSFileReference output) throws IllegalStateException, IOException, AccessDbImporterException {
        String ref = input.getString(DEFAULT_COLUMN_NAME);
        if (ref != null) {
            output.setChemin(ref);
        }
    }

    @Component
    public static class SIRSReferenceMapperSpi implements MapperSpi<SIRSFileReference> {

        @Override
        public Optional<Mapper<SIRSFileReference>> configureInput(Table inputType) throws IllegalStateException {
            if (ImportContext.columnExists(inputType, DEFAULT_COLUMN_NAME)) {
                return Optional.of(new SIRSFileReferenceMapper(inputType, DEFAULT_COLUMN_NAME));
            } else if (ImportContext.columnExists(inputType, PhotoColumns.NOM_FICHIER_PHOTO.name())) {
                return Optional.of(new SIRSFileReferenceMapper(inputType, PhotoColumns.NOM_FICHIER_PHOTO.name()));
            }
            return Optional.empty();
        }

        @Override
        public Class<SIRSFileReference> getOutputClass() {
            return SIRSFileReference.class;
        }

    }
}
