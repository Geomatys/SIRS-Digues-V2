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

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.ParametreHydrauliqueProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.AbstractLinker;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import fr.sirs.importer.v2.SimpleUpdater;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ParametrePTraversImporter extends SimpleUpdater<ParametreHydrauliqueProfilTravers, ProfilTravers> {

    private AbstractImporter<LeveProfilTravers> leveImporter;
    private Table leveTable;
    private Column leveColumn;
    private Column profilColumn;

    private enum Columns {

        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_EVENEMENT_HYDRAU
    }

    @Override
    public Class<ParametreHydrauliqueProfilTravers> getElementClass() {
        return ParametreHydrauliqueProfilTravers.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_TRAVERS_EVT_HYDRAU.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_EVENEMENT_HYDRAU.name();
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_PROFIL_EN_TRAVERS_LEVE.name();
    }

    @Override
    public void put(ProfilTravers container, ParametreHydrauliqueProfilTravers toPut) {
        container.parametresHydrauliques.add(toPut);
    }

    @Override
    public Class<ProfilTravers> getDocumentClass() {
        return ProfilTravers.class;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        leveImporter = context.importers.get(LeveProfilTravers.class);
        if (leveImporter == null) {
            throw new AccessDbImporterException("No importer found for " + LeveProfilTravers.class);
        }
        try {
            leveTable = context.inputDb.getTable(leveImporter.getTableName());
        } catch (IOException ex) {
            throw new AccessDbImporterException("Cannot access table " + leveImporter.getTableName(), ex);
        }
        leveColumn = leveTable.getColumn((leveImporter).getRowIdFieldName());
        profilColumn = leveTable.getColumn(((AbstractLinker) leveImporter).getHolderColumn());
    }

    @Override
    protected ProfilTravers getDocument(final Object rowId, Row input, ParametreHydrauliqueProfilTravers output) {
        final Object leveId = input.get(getDocumentIdField());
        if (leveId == null) {
            context.reportError(new ErrorReport(null, input, getTableName(), getDocumentIdField(), output, null, "Cannot import a profile parameter due to null foreign key.", CorruptionLevel.ROW));
            return null;
        }

        ProfilTravers result = null;
        try {
            final Cursor cursor = leveTable.newCursor().beforeFirst().toCursor();
            if (cursor.findFirstRow(leveColumn, leveId)) {
                final Integer profilId = cursor.getCurrentRow().getInt(profilColumn.getName());
                if (profilId == null) {
                    context.reportError(new ErrorReport(null, cursor.getCurrentRow(), leveTable.getName(), leveColumn.getName(), output, null, "Cannot import a profile parameter due to null foreign key.", CorruptionLevel.ROW));
                } else {
                    try {
                        result = masterRepository.get(masterImporter.getImportedId(profilId));
                    } catch (Exception ex) {
                        context.reportError(new ErrorReport(null, cursor.getCurrentRow(), leveTable.getName(), leveColumn.getName(), output, null, "No valid profile found for id " + profilId, CorruptionLevel.ROW));
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot access table " + leveImporter.getTableName(), ex);
        }
        return result;
    }
}
