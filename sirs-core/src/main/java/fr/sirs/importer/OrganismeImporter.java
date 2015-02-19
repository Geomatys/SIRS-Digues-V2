package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Organisme;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.intervenant.OrganismeDisposeIntervenantImporter;
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
public class OrganismeImporter extends GenericImporter {

    private Map<Integer, Organisme> organismes = null;
    private OrganismeDisposeIntervenantImporter organismeDisposeIntervenantImporter;

    private OrganismeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    OrganismeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final OrganismeDisposeIntervenantImporter organismeDisposeIntervenantImporter) {
        this(accessDatabase, couchDbConnector);
        this.organismeDisposeIntervenantImporter = organismeDisposeIntervenantImporter;
    }

    private enum Columns {
        ID_ORGANISME, 
        RAISON_SOCIALE, 
        STATUT_JURIDIQUE,
        ADRESSE_L1_ORG, 
        ADRESSE_L2_ORG, 
        ADRESSE_L3_ORG,
        ADRESSE_CODE_POSTAL_ORG, 
        ADRESSE_NOM_COMMUNE_ORG,
        TEL_ORG, 
        MAIL_ORG, 
        FAX_ORG,
        DATE_DEBUT, 
        DATE_FIN, 
        DATE_DERNIERE_MAJ
    };

    public Map<Integer, Organisme> getOrganismes() throws IOException {
        if (organismes == null) compute();
        return organismes;
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
        return DbImporter.TableName.ORGANISME.toString();
    }

    @Override
    protected void compute() throws IOException {
        organismes = new HashMap<>();
        
        final Map<Integer, List<ContactOrganisme>> contactsOrganismes = organismeDisposeIntervenantImporter.getContactOrganismeByOrganismeId();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Organisme organisme = new Organisme();

            organisme.setNom(row.getString(Columns.RAISON_SOCIALE.toString()));
            
            organisme.setStatut_juridique(row.getString(Columns.STATUT_JURIDIQUE.toString()));
            
            organisme.setAdresse(cleanNullString(row.getString(Columns.ADRESSE_L1_ORG.toString()))
                    + cleanNullString(row.getString(Columns.ADRESSE_L2_ORG.toString()))
                    + cleanNullString(row.getString(Columns.ADRESSE_L3_ORG.toString())));
            
            organisme.setCode_postal(cleanNullString(String.valueOf(row.getInt(Columns.ADRESSE_CODE_POSTAL_ORG.toString()))));
            
            organisme.setCommune(row.getString(Columns.ADRESSE_NOM_COMMUNE_ORG.toString()));
            
            organisme.setTelephone(row.getString(Columns.TEL_ORG.toString()));
            
            organisme.setEmail(row.getString(Columns.MAIL_ORG.toString()));
            
            organisme.setFax(row.getString(Columns.FAX_ORG.toString()));
            
            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
                organisme.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
                organisme.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                organisme.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if (contactsOrganismes.get(row.getInt(Columns.ID_ORGANISME.toString()))!=null){
                organisme.setContactOrganisme(contactsOrganismes.get(row.getInt(Columns.ID_ORGANISME.toString())));
            }
            
            organisme.setPseudoId(String.valueOf(row.getInt(Columns.ID_ORGANISME.toString())));
            organisme.setValid(true);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            organismes.put(row.getInt(Columns.ID_ORGANISME.toString()), organisme);
        }
        couchDbConnector.executeBulk(organismes.values());
    }
}
