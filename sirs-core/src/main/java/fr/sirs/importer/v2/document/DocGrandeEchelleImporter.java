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
package fr.sirs.importer.v2.document;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class DocGrandeEchelleImporter extends AbstractImporter<DocumentGrandeEchelle> {

    @Autowired
    DocTypeRegistry registry;

    @Override
    public Class<DocumentGrandeEchelle> getElementClass() {
        return DocumentGrandeEchelle.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.DOCUMENT.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_DOC";
    }

    @Override
    protected DocumentGrandeEchelle createElement(Row input) {
        final Class docType = registry.getDocType(input);
        if (docType != null && DocumentGrandeEchelle.class.isAssignableFrom(docType)) {
            return super.createElement(input);
        } else {
            return null;
        }
    }
}
