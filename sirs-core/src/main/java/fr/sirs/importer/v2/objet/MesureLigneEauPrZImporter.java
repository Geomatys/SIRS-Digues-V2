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

import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MesureLigneEauPrZ;
import static fr.sirs.importer.DbImporter.TableName.LIGNE_EAU_MESURES_PRZ;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MesureLigneEauPrZImporter extends SimpleUpdater<MesureLigneEauPrZ, LigneEau> {

    @Override
    public String getDocumentIdField() {
        return masterImporter.getRowIdFieldName();
    }

    @Override
    public Class<MesureLigneEauPrZ> getElementClass() {
        return MesureLigneEauPrZ.class;
    }

    @Override
    public String getTableName() {
        return LIGNE_EAU_MESURES_PRZ.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_POINT";
    }

    @Override
    public void put(LigneEau container, MesureLigneEauPrZ toPut) {
        container.getMesuresDZ().add(toPut);
    }

    @Override
    public Class<LigneEau> getDocumentClass() {
        return LigneEau.class;
    }

}
