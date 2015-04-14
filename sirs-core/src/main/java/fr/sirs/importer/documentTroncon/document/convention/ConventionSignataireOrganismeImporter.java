package fr.sirs.importer.documentTroncon.document.convention;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;

import fr.sirs.core.model.ContactConvention;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.OrganismeImporter;

import java.io.IOException;
import java.time.LocalDateTime;
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

    private Map<Integer, List<ContactConvention>> signatairesByConventionId = null;
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

    /**
     *
     * @return A map containing all ContactTroncon instances accessibles from
     * the internal database <em>TronconGestion</em> identifier.
     * @throws IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    public Map<Integer, List<ContactConvention>> getOrganisationSignataire() throws IOException, AccessDbImporterException {
        if (signatairesByConventionId == null) compute();
        return signatairesByConventionId;
    }
    
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
        return DbImporter.TableName.CONVENTION_SIGNATAIRES_PM.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        signatairesByConventionId = new HashMap<>();
        signatairesIdsByConventionId = new HashMap<>();

        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ContactConvention signataire = new ContactConvention();
            

            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                signataire.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            // Pas d'Id unique car table de jointure. Arbitrairement, on met comme Id celui du signataire.
            signataire.setDesignation(String.valueOf(row.getInt(Columns.ID_ORG_SIGNATAIRE.toString())));
            signataire.setValid(true);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            List<ContactConvention> listeSignataires = signatairesByConventionId.get(row.getInt(Columns.ID_CONVENTION.toString()));
            if(listeSignataires == null){
                listeSignataires = new ArrayList<>();
            }
            listeSignataires.add(signataire);
            signatairesByConventionId.put(row.getInt(Columns.ID_CONVENTION.toString()), listeSignataires);

            // Set the references.
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_SIGNATAIRE.toString()));
            if (organisme.getId() != null) {
                signataire.setOrganismeId(organisme.getId());
            } else {
                throw new AccessDbImporterException("L'organisme " + organisme + " n'a pas encore d'identifiant CouchDb !");
            }
            signatairesByConventionId.put(row.getInt(Columns.ID_CONVENTION.toString()), listeSignataires);
            
            
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
