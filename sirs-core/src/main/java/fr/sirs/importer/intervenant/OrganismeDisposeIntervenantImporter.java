package fr.sirs.importer.intervenant;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.ContactRepository;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
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
public class OrganismeDisposeIntervenantImporter extends GenericImporter {

    private Map<Integer, List<ContactOrganisme>> contactOrganismesByOrganismeId = null;
    private IntervenantImporter intervenantImporter;

    private OrganismeDisposeIntervenantImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public OrganismeDisposeIntervenantImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final ContactRepository contactRepository,
            final IntervenantImporter intervenantImporter) {
        this(accessDatabase, couchDbConnector);
        this.intervenantImporter = intervenantImporter;
    }

    private enum OrganismeDisposeIntervenantColumns {
        ID_ORGANISME,
        ID_INTERVENANT,
        DATE_DEBUT_INTERV_ORG,
        DATE_FIN_INTERV_ORG,
        DATE_DERNIERE_MAJ,
    };

    public Map<Integer, List<ContactOrganisme>> getContactOrganismeByOrganismeId() throws IOException {
        if (contactOrganismesByOrganismeId == null) compute();
        return contactOrganismesByOrganismeId;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (OrganismeDisposeIntervenantColumns c : OrganismeDisposeIntervenantColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ORGANISME_DISPOSE_INTERVENANT.toString();
    }

    @Override
    protected void compute() throws IOException {
        contactOrganismesByOrganismeId = new HashMap<>();
        
        final Map<Integer, Contact> organismes = intervenantImporter.getIntervenants();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ContactOrganisme contactOrganisme = new ContactOrganisme();

            contactOrganisme.setContactId(organismes.get(row.getInt(OrganismeDisposeIntervenantColumns.ID_INTERVENANT.toString())).getId());
            
            if (row.getDate(OrganismeDisposeIntervenantColumns.DATE_DEBUT_INTERV_ORG.toString()) != null) {
                contactOrganisme.setDateDebutIntervenant(LocalDateTime.parse(row.getDate(OrganismeDisposeIntervenantColumns.DATE_DEBUT_INTERV_ORG.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(OrganismeDisposeIntervenantColumns.DATE_FIN_INTERV_ORG.toString()) != null) {
                contactOrganisme.setDateFinIntervenant(LocalDateTime.parse(row.getDate(OrganismeDisposeIntervenantColumns.DATE_FIN_INTERV_ORG.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(OrganismeDisposeIntervenantColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                contactOrganisme.setDateMaj(LocalDateTime.parse(row.getDate(OrganismeDisposeIntervenantColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }

            List<ContactOrganisme> listByIntervenantId = contactOrganismesByOrganismeId.get(row.getInt(OrganismeDisposeIntervenantColumns.ID_ORGANISME.toString()));
            if (listByIntervenantId == null) {
                listByIntervenantId = new ArrayList<>();
            }
            listByIntervenantId.add(contactOrganisme);
            contactOrganismesByOrganismeId.put(row.getInt(OrganismeDisposeIntervenantColumns.ID_ORGANISME.toString()), listByIntervenantId);
        }
        
    }
}
