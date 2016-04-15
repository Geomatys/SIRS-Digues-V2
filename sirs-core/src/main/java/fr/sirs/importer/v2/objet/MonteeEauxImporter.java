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

import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractLinker;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MonteeEauxImporter extends AbstractLinker<MonteeEaux, EvenementHydraulique>{

    @Override
    public void bind(EvenementHydraulique holder, Collection<String> targetIds) throws AccessDbImporterException {
        holder.getMonteeEauxIds().addAll(targetIds);
    }

    @Override
    public String getHolderColumn() {
        return "ID_EVENEMENT_HYDRAU";
    }

    @Override
    public Class<MonteeEaux> getElementClass() {
        return MonteeEaux.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.MONTEE_DES_EAUX.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_MONTEE_DES_EAUX";
    }

    @Override
    public Class<EvenementHydraulique> getHolderClass() {
        return EvenementHydraulique.class;
    }

}
