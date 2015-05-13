package fr.sirs.importer.documentTroncon.document.convention;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;

import fr.sirs.core.model.Organisme;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.OrganismeImporter;

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
class ConventionSignataireOrganismeImporter extends GenericImporter {

    private Map<Integer, List<String>> signatairesIdsByConventionId = null;
    private OrganismeImporter organismeImporter;

    private ConventionSignataireOrganismeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    ConventionSignataireOrganismeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final OrganismeImporter organismeImporter) {
        this(accessDatabase, couchDbConnector);
        this.organismeImporter = organismeImporter;
    }

    private enum Columns {
        ID_CONVENTION,
        ID_ORG_SIGNATAIRE,
        DATE_DERNIERE_MAJ
    };
    
    public Map<Integer, List<String>> getOrganisationsSignatairesIds() throws IOException, AccessDbImporterException {
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
        return CONVENTION_SIGNATAIRES_PM.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        signatairesIdsByConventionId = new HashMap<>();

        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            // Set the references.
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_SIGNATAIRE.toString()));
            
            List<String> listeSignatairesIds = signatairesIdsByConventionId.get(row.getInt(Columns.ID_CONVENTION.toString()));
            if(listeSignatairesIds == null){
                listeSignatairesIds = new ArrayList<>();
            }
            
            if (organisme.getId() != null) {
                listeSignatairesIds.add(organisme.getId());
            } else {
                throw new AccessDbImporterException("L'organisme " + organisme + " n'a pas encore d'identifiant CouchDb !");
            }
            signatairesIdsByConventionId.put(row.getInt(Columns.ID_CONVENTION.toString()), listeSignatairesIds);
        }
    }
}
