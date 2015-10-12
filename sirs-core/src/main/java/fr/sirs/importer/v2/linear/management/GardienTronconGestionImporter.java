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
