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

import fr.sirs.core.model.GestionObjet;
import fr.sirs.core.model.ObjetReseau;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_GESTIONNAIRE;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauGestionImporter extends AbstractElementReseauGestionImporter<GestionObjet> {

    private enum Columns {
        ID_ORG_GESTION
    }

    @Override
    public void put(ObjetReseau container, GestionObjet toPut) {
        container.gestions.add(toPut);
    }

    @Override
    public Class<GestionObjet> getElementClass() {
        return GestionObjet.class;
    }

    @Override
    public String getTableName() {
        return ELEMENT_RESEAU_GESTIONNAIRE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ORG_GESTION.name();
    }

}
