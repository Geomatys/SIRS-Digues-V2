package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ContactTroncon;
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
public class TronconGestionDigueGestionnaireImporter extends GenericImporter {

    private Map<Integer, List<ContactTroncon>> gestionsByTronconId = null;
    private OrganismeImporter organismeImporter;

    private TronconGestionDigueGestionnaireImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public TronconGestionDigueGestionnaireImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final OrganismeImporter organismeImporter) {
        this(accessDatabase, couchDbConnector);
        this.organismeImporter = organismeImporter;
    }

    private enum TronconGestionDigueGestionnaireColumns {
        ID_TRONCON_GESTION,
        ID_ORG_GESTION,
        DATE_DEBUT_GESTION, 
        DATE_FIN_GESTION,
        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all ContactTroncon instances accessibles from
     * the internal database <em>TronconGestion</em> identifier.
     * @throws IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    public Map<Integer, List<ContactTroncon>> getGestionsByTronconId() throws IOException, AccessDbImporterException {
        if (gestionsByTronconId == null) compute();
        return gestionsByTronconId;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TronconGestionDigueGestionnaireColumns c : TronconGestionDigueGestionnaireColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TRONCON_GESTION_DIGUE_GESTIONNAIRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        gestionsByTronconId = new HashMap<>();

        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ContactTroncon gestion = new ContactTroncon();
            
            gestion.setTypeContact("Gestionnaire");

            if (row.getDate(TronconGestionDigueGestionnaireColumns.DATE_DEBUT_GESTION.toString()) != null) {
                gestion.setDate_debut(LocalDateTime.parse(row.getDate(TronconGestionDigueGestionnaireColumns.DATE_DEBUT_GESTION.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(TronconGestionDigueGestionnaireColumns.DATE_FIN_GESTION.toString()) != null) {
                gestion.setDate_fin(LocalDateTime.parse(row.getDate(TronconGestionDigueGestionnaireColumns.DATE_FIN_GESTION.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(TronconGestionDigueGestionnaireColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                gestion.setDateMaj(LocalDateTime.parse(row.getDate(TronconGestionDigueGestionnaireColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }

            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            List<ContactTroncon> listeGestions = gestionsByTronconId.get(row.getInt(TronconGestionDigueGestionnaireColumns.ID_TRONCON_GESTION.toString()));
            if(listeGestions == null){
                listeGestions = new ArrayList<>();
                gestionsByTronconId.put(row.getInt(TronconGestionDigueGestionnaireColumns.ID_TRONCON_GESTION.toString()), listeGestions);
            }
            listeGestions.add(gestion);

            // Set the references.
            final Organisme organisme = organismes.get(row.getInt(TronconGestionDigueGestionnaireColumns.ID_ORG_GESTION.toString()));
            if (organisme.getId() != null) {
                gestion.setOrganismeId(organisme.getId());
            } else {
                throw new AccessDbImporterException("L'organisme " + organisme + " n'a pas encore d'identifiant CouchDb !");
            }
        }
    }
}
