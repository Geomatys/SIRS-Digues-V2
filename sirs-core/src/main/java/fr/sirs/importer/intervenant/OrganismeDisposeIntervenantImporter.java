package fr.sirs.importer.intervenant;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
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
            final IntervenantImporter intervenantImporter) {
        this(accessDatabase, couchDbConnector);
        this.intervenantImporter = intervenantImporter;
    }

    private enum Columns {
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
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
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

            contactOrganisme.setContactId(organismes.get(row.getInt(Columns.ID_INTERVENANT.toString())).getId());
            
            if (row.getDate(Columns.DATE_DEBUT_INTERV_ORG.toString()) != null) {
                contactOrganisme.setDateDebutIntervenant(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_INTERV_ORG.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN_INTERV_ORG.toString()) != null) {
                contactOrganisme.setDateFinIntervenant(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_INTERV_ORG.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                contactOrganisme.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            // Table de jointure, donc pas d'ID propre. On choisit arbitrairement l'ID de l'intervenant comme pseudo-id.
            contactOrganisme.setPseudoId(row.getInt(Columns.ID_INTERVENANT.toString()));

            List<ContactOrganisme> listByIntervenantId = contactOrganismesByOrganismeId.get(row.getInt(Columns.ID_ORGANISME.toString()));
            if (listByIntervenantId == null) {
                listByIntervenantId = new ArrayList<>();
            }
            listByIntervenantId.add(contactOrganisme);
            contactOrganismesByOrganismeId.put(row.getInt(Columns.ID_ORGANISME.toString()), listByIntervenantId);
        }
        
    }
}
