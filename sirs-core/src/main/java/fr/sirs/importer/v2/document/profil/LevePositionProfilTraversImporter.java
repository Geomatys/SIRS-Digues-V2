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
package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.LevePositionProfilTravers;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.PROFIL_EN_TRAVERS_TRONCON;
import fr.sirs.importer.v2.AbstractLinker;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class LevePositionProfilTraversImporter extends AbstractLinker<LevePositionProfilTravers, PositionProfilTravers> {

    @Override
    public void bind(PositionProfilTravers holder, Collection<String> targetIds) throws AccessDbImporterException {
        holder.getLevePositionIds().addAll(targetIds);
    }

    @Override
    public String getHolderColumn() {
        return Columns.ID_DOC.name();
    }

    @Override
    public Class<PositionProfilTravers> getHolderClass() {
        return PositionProfilTravers.class;
    }

    private enum Columns {
        ID_DOC,
        ID_PROFIL_EN_TRAVERS_LEVE
    }

    @Override
    public Class<LevePositionProfilTravers> getElementClass() {
        return LevePositionProfilTravers.class;
    }

    @Override
    public String getTableName() {
        return PROFIL_EN_TRAVERS_TRONCON.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_DOC.name();
    }

}
