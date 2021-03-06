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
package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.core.model.GardeObjet;
import fr.sirs.core.model.ObjetReseau;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_GARDIEN;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauGardienImporter extends AbstractElementReseauGestionImporter<GardeObjet> {

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_INTERV_GARDIEN
    }

    @Override
    public void put(ObjetReseau container, GardeObjet toPut) {
        container.gardes.add(toPut);
    }

    @Override
    public Class<GardeObjet> getElementClass() {
        return GardeObjet.class;
    }

    @Override
    public String getTableName() {
        return ELEMENT_RESEAU_GARDIEN.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_INTERV_GARDIEN.name();
    }

}
