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
package fr.sirs.importer.v2.contact;

import fr.sirs.importer.*;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 * @author Alexis Manin (Geometys)
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class OrganismeDisposeIntervenantImporter extends SimpleUpdater<ContactOrganisme, Organisme> {

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_INTERVENANT.name();
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_ORGANISME.name();
    }

    @Override
    public void put(Organisme container, ContactOrganisme toPut) {
        container.contactOrganisme.add(toPut);
    }

    @Override
    public Class<Organisme> getDocumentClass() {
        return Organisme.class;
    }

    private enum Columns {
        ID_ORGANISME,
        ID_INTERVENANT
    };

    @Override
    public Class<ContactOrganisme> getElementClass() {
        return ContactOrganisme.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ORGANISME_DISPOSE_INTERVENANT.name();
    }
}
