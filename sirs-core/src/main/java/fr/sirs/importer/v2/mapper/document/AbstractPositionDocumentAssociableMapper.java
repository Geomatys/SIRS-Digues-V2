/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.importer.v2.mapper.document;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import fr.sirs.importer.v2.ImportContext;
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
        ID_PROFIL_EN_TRAVERS,
        ID_TYPE_DOCUMENT
    }

    private AbstractPositionDocumentAssociableMapper(final Table t) {
        super(t);
    }

    @Override
    public void map(Row input, AbstractPositionDocumentAssociable output) throws IllegalStateException, IOException, AccessDbImporterException {
        if (output instanceof PositionProfilTravers) {
            Object ptId = input.get(Columns.ID_PROFIL_EN_TRAVERS.name());
            if (ptId == null) {
                final Object typeDoc = input.get(Columns.ID_TYPE_DOCUMENT.name());
            }
            if (ptId != null) {
                String importedId = context.importers.get(ProfilTravers.class).getImportedId(ptId);
                output.setSirsdocument(importedId);
            }
        } else if (output != null) {
            final Object typeDoc = input.get(Columns.ID_TYPE_DOCUMENT.name());
            if (typeDoc == null) {
                context.reportError(new ErrorReport(null, input, tableName, Columns.ID_TYPE_DOCUMENT.name(), output, "documentId", "No valid document type associated.", CorruptionLevel.RELATION));
            }
            Class docType = registry.getDocType(typeDoc);
            if (docType != null) {
                final AbstractImporter importer = context.importers.get(docType);
                if (importer != null) {

                    try {
                        final Object docId = input.get(importer.getRowIdFieldName());
                        if (docId != null) {
                            final String importedId = importer.getImportedId(docId);
                            if (importedId != null) {
                                output.setSirsdocument(importedId);
                            }
                        }

                    } catch (Exception e) {
                        context.reportError(table.getName(), input, e);
                    }
                }
            }
        }
    }

    @Component
    public static class Spi implements MapperSpi<AbstractPositionDocumentAssociable> {

        @Override
        public Optional<Mapper<AbstractPositionDocumentAssociable>> configureInput(Table inputType) throws IllegalStateException {
            if (ImportContext.columnExists(inputType, Columns.ID_TYPE_DOCUMENT.name()) || ImportContext.columnExists(inputType, Columns.ID_PROFIL_EN_TRAVERS.name())) {
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
