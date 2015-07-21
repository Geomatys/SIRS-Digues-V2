package fr.sirs.importer.documentTroncon.document.convention;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;

import fr.sirs.core.model.Contact;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class ConventionSignataireIntervenantImporter extends GenericImporter {

    private Map<Integer, List<String>> signatairesIdsByConventionId = null;
    private IntervenantImporter intervenantImporter;

    private ConventionSignataireIntervenantImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    ConventionSignataireIntervenantImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final IntervenantImporter intervenantImporter) {
        this(accessDatabase, couchDbConnector);
        this.intervenantImporter = intervenantImporter;
    }

    private enum Columns {
        ID_CONVENTION,
        ID_INTERV_SIGNATAIRE,
        DATE_DERNIERE_MAJ
    };
    
    public Map<Integer, List<String>> getIntervenantsSignatairesIds() throws IOException, AccessDbImporterException {
        if (signatairesIdsByConventionId == null) compute();
        return signatairesIdsByConventionId;
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
        return CONVENTION_SIGNATAIRES_PP.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        signatairesIdsByConventionId = new HashMap<>();

        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            // Set the references.
            final Contact intervenant = intervenants.get(row.getInt(Columns.ID_INTERV_SIGNATAIRE.toString()));
            
            List<String> listeSignatairesIds = signatairesIdsByConventionId.get(row.getInt(Columns.ID_CONVENTION.toString()));
            if(listeSignatairesIds == null){
                listeSignatairesIds = new ArrayList<>();
            }
            if (intervenant.getId() != null) {
                listeSignatairesIds.add(intervenant.getId());
            } else {
                throw new AccessDbImporterException("Le contact " + intervenant + " n'a pas encore d'identifiant CouchDb !");
            }
            signatairesIdsByConventionId.put(row.getInt(Columns.ID_CONVENTION.toString()), listeSignatairesIds);
        }
    }
}
