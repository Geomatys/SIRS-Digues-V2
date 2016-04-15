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
package fr.sirs.importer.v2.linear.management;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class GardienTronconGestionImporter extends GenericPeriodeLocaliseeImporter<GardeTroncon> {

    private AbstractImporter<Contact> intervenantImporter;

    private enum Columns {
        ID_GARDIEN_TRONCON_GESTION,
        ID_INTERVENANT,
        ID_TRONCON_GESTION
    }

    @Override
    public Class<GardeTroncon> getElementClass() {
        return GardeTroncon.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_GARDIEN_TRONCON_GESTION.name();
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return GARDIEN_TRONCON_GESTION.toString();
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        intervenantImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        intervenantImporter = context.importers.get(Contact.class);
    }

    @Override
    public GardeTroncon importRow(Row row, GardeTroncon output) throws IOException, AccessDbImporterException {
        output = super.importRow(row, output);

        final String tronconId = tdImporter.getImportedId(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        output.setLinearId(tronconId);
        final String gardeId = String.valueOf(row.getInt(Columns.ID_GARDIEN_TRONCON_GESTION.toString()));
        output.setDesignation(gardeId);

        // Set the references.
        final String intervenant = intervenantImporter.getImportedId(row.getInt(Columns.ID_INTERVENANT.toString()));
        if (intervenant != null) {
            output.setContactId(intervenant);
        } else {
            throw new AccessDbImporterException("Aucun contact valide associé au gardien "+gardeId);
        }

        return output;
    }
}
