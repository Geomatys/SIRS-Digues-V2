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
import fr.sirs.importer.v2.SimpleUpdater;
import java.io.IOException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
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
    protected Class<ParametreHydrauliqueProfilTravers> getElementClass() {
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
            throw new AccessDbImporterException("No importer found for "+LeveProfilTravers.class);
        }
        try {
            leveTable = context.inputDb.getTable(leveImporter.getTableName());
        } catch (IOException ex) {
            throw new AccessDbImporterException("Cannot access table "+leveImporter.getTableName(), ex);
        }
        leveColumn = leveTable.getColumn((leveImporter).getRowIdFieldName());
        profilColumn = leveTable.getColumn(((AbstractLinker)leveImporter).getHolderColumn());
    }


    @Override
    protected ProfilTravers getDocument(final int rowId, Row input, ParametreHydrauliqueProfilTravers output) {
        Integer leveId = input.getInt(getDocumentIdField());
        if (leveId == null) {
            throw new IllegalStateException("Input has no valid ID in " + getDocumentIdField());
        }

        final Cursor cursor = leveTable.getDefaultCursor();
        try {
            while (cursor.findNextRow(leveColumn, leveId)) {
                final Integer profilId = cursor.getCurrentRow().getInt(profilColumn.getName());
                if (profilId == null) {
                    throw new IllegalStateException("No valid profil found for leve " + leveId);
                }
                try {
                    return masterRepository.get(masterImporter.getImportedId(profilId));
                } catch (Exception ex) {
                    throw new IllegalStateException("No valid profil found for id " + profilId);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot access table " + leveImporter.getTableName(), ex);
        }
        throw new IllegalStateException("No valid profil found.");
    }
}
