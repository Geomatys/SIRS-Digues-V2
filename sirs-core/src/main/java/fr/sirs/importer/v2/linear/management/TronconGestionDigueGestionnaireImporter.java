package fr.sirs.importer.v2.linear.management;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.GestionTroncon;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.SimpleUpdater;
import fr.sirs.importer.v2.mapper.Mapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TronconGestionDigueGestionnaireImporter extends SimpleUpdater<GestionTroncon, TronconDigue> {

    private AbstractImporter<Organisme> organismeImporter;

    private enum Columns {
        ID_TRONCON_GESTION,
        ID_ORG_GESTION
    };

    @Override
    protected Class<GestionTroncon> getElementClass() {
        return GestionTroncon.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ORG_GESTION.name();
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_TRONCON_GESTION.name();
    }

    @Override
    public void put(TronconDigue container, GestionTroncon toPut) {
        container.gestions.add(toPut);
    }

    @Override
    public Class<TronconDigue> getDocumentClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        return TRONCON_GESTION_DIGUE_GESTIONNAIRE.toString();
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        organismeImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        organismeImporter = context.importers.get(Organisme.class);
    }

    @Override
    public GestionTroncon importRow(Row row, GestionTroncon gestion) throws IOException, AccessDbImporterException {
        gestion = super.importRow(row, gestion);

        Set<Mapper<GestionTroncon>> compatibleMappers = context.getCompatibleMappers(context.inputDb.getTable(getTableName()), GestionTroncon.class);
        for (final Mapper<GestionTroncon> m : compatibleMappers) {
            m.map(row, gestion);
        }

        // Set the references.
        final String rowId = String.valueOf(row.getInt(Columns.ID_ORG_GESTION.toString()));
        final String organisme = organismeImporter.getImportedId(row.getInt(Columns.ID_ORG_GESTION.toString()));
        if (organisme != null) {
            gestion.setOrganismeId(organisme);
        } else {
            throw new AccessDbImporterException("Aucun organisme trouvé pour le gestionnaire " + rowId);
        }

        return gestion;
    }
}
