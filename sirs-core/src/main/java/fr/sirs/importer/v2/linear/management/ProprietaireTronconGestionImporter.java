package fr.sirs.importer.v2.linear.management;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.core.model.RefProprietaire;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ProprietaireTronconGestionImporter extends GenericPeriodeLocaliseeImporter<ProprieteTroncon> {

    private AbstractImporter<Contact> intervenantImporter;
    private AbstractImporter<Organisme> organismeImporter;
    private AbstractImporter<RefProprietaire> typeProprietaireImporter;

    @Override
    protected Class<ProprieteTroncon> getElementClass() {
        return ProprieteTroncon.class;
    }

    @Override
    public String getRowIdFieldName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private enum Columns {
        ID_PROPRIETAIRE_TRONCON_GESTION,
        ID_TRONCON_GESTION,
        ID_TYPE_PROPRIETAIRE,
        ID_ORGANISME,
        ID_INTERVENANT,
    };

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
        return PROPRIETAIRE_TRONCON_GESTION.toString();
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        intervenantImporter = null;
        organismeImporter = null;
        typeProprietaireImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        intervenantImporter = context.importers.get(Contact.class);
        organismeImporter = context.importers.get(Organisme.class);
        typeProprietaireImporter = context.importers.get(RefProprietaire.class);
    }

    @Override
    public ProprieteTroncon importRow(Row row, ProprieteTroncon propriete) throws IOException, AccessDbImporterException {
        propriete = super.importRow(row, propriete);

        final String troncon = tdImporter.getImportedId(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        propriete.setLinearId(troncon);

        propriete.setDesignation(String.valueOf(row.getInt(Columns.ID_PROPRIETAIRE_TRONCON_GESTION.toString())));
        
        if (row.getInt(Columns.ID_TYPE_PROPRIETAIRE.toString()) != null) {
            propriete.setTypeProprietaireId(typeProprietaireImporter.getImportedId(row.getInt(Columns.ID_TYPE_PROPRIETAIRE.toString())));
        }

        final Integer intervenantId = row.getInt(Columns.ID_INTERVENANT.toString());
        if (intervenantId != null) {
            final String intervenant = intervenantImporter.getImportedId(intervenantId);
            if (intervenant != null) {
                propriete.setContactId(intervenant);
            } else {
                throw new AccessDbImporterException("Le contact " + intervenant + " n'a pas encore d'identifiant CouchDb !");
            }
        } else {
            final Integer organismeId = row.getInt(Columns.ID_ORGANISME.toString());
            if (organismeId != null) {
                final String organisme = organismeImporter.getImportedId(organismeId);
                if (organisme != null) {
                    propriete.setOrganismeId(organisme);
                } else {
                    throw new AccessDbImporterException("L'organisme " + organisme + " n'a pas encore d'identifiant CouchDb !");
                }
            }
        }

        return propriete;
    }
}
