/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.document.DocTypeRegistry;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class AbstractPositionDocumentAssociableMapper extends AbstractMapper<AbstractPositionDocumentAssociable> {

    @Autowired
    private DocTypeRegistry registry;

    private enum Columns {
        ID_DOC,
        ID_PROFIL_EN_TRAVERS,
        ID_TYPE_DOCUMENT
    }

    private AbstractPositionDocumentAssociableMapper(final Table t) {
        super(t);
    }

    @Override
    public void map(Row input, AbstractPositionDocumentAssociable output) throws IllegalStateException, IOException, AccessDbImporterException {
        if (output instanceof PositionProfilTravers) {
            Integer ptId = input.getInt(Columns.ID_PROFIL_EN_TRAVERS.name());
            if (ptId != null) {
                String importedId = context.importers.get(ProfilTravers.class).getImportedId(ptId);
                output.setDocumentId(importedId);
            }
        } else if (output instanceof AbstractPositionDocumentAssociable) {
            Integer docId = input.getInt(Columns.ID_DOC.name());
            if (docId != null) {
                final Integer typeDoc = input.getInt(Columns.ID_TYPE_DOCUMENT.name());
                if (typeDoc == null) {
                    throw new AccessDbImporterException("No valid document type associated.");
                }
                Class docType = registry.getDocType(typeDoc);
                if (docType == null) {
                    throw new AccessDbImporterException("No mapping found for document type " + typeDoc);
                }

                final AbstractImporter importer = context.importers.get(docType);
                if (importer == null) {
                    throw new AccessDbImporterException("No importer found for document type " + docType);
                }

                final String importedId = importer.getImportedId(docId);
                if (importedId != null) {
                    output.setDocumentId(importedId);
                }
            }
        }
    }

    @Component
    public static class Spi implements MapperSpi<AbstractPositionDocumentAssociable> {

        @Override
        public Optional<Mapper<AbstractPositionDocumentAssociable>> configureInput(Table inputType) throws IllegalStateException {
            if (inputType.getColumn(Columns.ID_DOC.name()) != null && inputType.getColumn(Columns.ID_TYPE_DOCUMENT.name()) != null) {
                return Optional.of(new AbstractPositionDocumentAssociableMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<AbstractPositionDocumentAssociable> getOutputClass() {
            return AbstractPositionDocumentAssociable.class;
        }
    }


}
