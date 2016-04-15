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

import fr.sirs.core.model.RapportEtude;
import static fr.sirs.importer.DbImporter.TableName.RAPPORT_ETUDE;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class RapportEtudeImporter extends AbstractImporter<RapportEtude> {

    private enum Columns {
        ID_RAPPORT_ETUDE,
        TITRE_RAPPORT_ETUDE,
        ID_TYPE_RAPPORT_ETUDE,
        AUTEUR_RAPPORT,
        DATE_RAPPORT
    };

    @Override
    public Class<RapportEtude> getElementClass() {
        return RapportEtude.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_RAPPORT_ETUDE.name();
    }



    @Override
    public String getTableName() {
        return RAPPORT_ETUDE.toString();
    }
}
