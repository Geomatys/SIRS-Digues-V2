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
package fr.sirs.importer.v2.document.position;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.PositionConvention;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PositionConventionImporter extends AbstractImporter<PositionConvention> {

    @Autowired
    private ConventionTypeRegistry registry;

    @Override
    public Class<PositionConvention> getElementClass() {
        return PositionConvention.class;
    }

    @Override
    public String getTableName() {
        return "DOCUMENT";
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_DOC";
    }

    @Override
    protected PositionConvention createElement(Row input) {
        if (registry.isPositionConvention(input)) {
            return ElementCreator.createAnonymValidElement(getElementClass());
        } else {
            return null;
        }
    }
}
