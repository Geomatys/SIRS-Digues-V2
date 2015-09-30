/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.RefDocumentGrandeEchelle;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
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
public class DocumentGrandeEchelleMapper extends AbstractMapper<DocumentGrandeEchelle> {

    private enum Columns {
        REFERENCE_CALQUE,
        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE
    }

    private final AbstractImporter<RefDocumentGrandeEchelle> typeImporter;

    public DocumentGrandeEchelleMapper(Table table) {
        super(table);
        typeImporter = context.importers.get(RefDocumentGrandeEchelle.class);
        if (typeImporter == null) {
            throw new IllegalStateException("No importer found for reference class : RefDocumentGrandeEchelle");
        }
    }

    @Override
    public void map(Row input, DocumentGrandeEchelle output) throws IllegalStateException, IOException, AccessDbImporterException {
        String ref = input.getString(Columns.REFERENCE_CALQUE.name());
        if (ref != null) {
            output.setReference_calque(ref);
        }

        final Object typeId = input.get(Columns.ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE.name());
        if (typeId != null) {
            String importedId = typeImporter.getImportedId(typeId);
            if (importedId == null) {
                throw new AccessDbImporterException("RefDocumentGrandeEchelle : No reference found for ID "+typeId);
            }
            output.setTypeDocumentGrandeEchelleId(importedId);
        }
    }

    @Component
    public static class Spi implements MapperSpi<DocumentGrandeEchelle> {

        @Override
        public Optional<Mapper<DocumentGrandeEchelle>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (!ImportContext.columnExists(inputType, c.name())) {
                    return Optional.empty();
                }
            }
            return Optional.of(new DocumentGrandeEchelleMapper(inputType));
        }

        @Override
        public Class<DocumentGrandeEchelle> getOutputClass() {
            return DocumentGrandeEchelle.class;
        }
    }
}
