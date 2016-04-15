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
package fr.sirs.importer.v2.objet;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import fr.sirs.importer.v2.MultipleSubTypes;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class StructureImporter extends AbstractImporter<Objet> implements MultipleSubTypes<Objet> {

    private enum Columns {
        ID_ELEMENT_STRUCTURE,
        ID_TYPE_ELEMENT_STRUCTURE
    }

    @Autowired
    private StructureRegistry registry;

    @Override
    public Class<Objet> getElementClass() {
        return Objet.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ELEMENT_STRUCTURE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ELEMENT_STRUCTURE.name();
    }

    @Override
    protected Objet createElement(Row input) {
        final Object type = input.get(Columns.ID_TYPE_ELEMENT_STRUCTURE.toString());
        if (type == null) {
            context.reportError(new ErrorReport(null, input, getTableName(), Columns.ID_TYPE_ELEMENT_STRUCTURE.name(), null, null, "No structure type defined", CorruptionLevel.ROW));
        }

        // Find what type of element must be imported.
        Class<? extends Objet> clazz = registry.getElementType(type);
        return clazz == null ? null : ElementCreator.createAnonymValidElement(clazz);
    }

    @Override
    public Collection<Class<? extends Objet>> getSubTypes() {
        return registry.allTypes();
    }
}
