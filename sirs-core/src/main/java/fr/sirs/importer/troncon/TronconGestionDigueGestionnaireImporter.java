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
class TronconGestionDigueGestionnaireImporter extends GenericImporter {

    private Map<Integer, List<ContactTroncon>> gestionsByTronconId = null;
    private final OrganismeImporter organismeImporter;

    TronconGestionDigueGestionnaireImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        this.organismeImporter = organismeImporter;
    }

    private enum Columns {
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
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
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

            if (row.getDate(Columns.DATE_DEBUT_GESTION.toString()) != null) {
                gestion.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_GESTION.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN_GESTION.toString()) != null) {
                gestion.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_GESTION.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                gestion.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }

            // Set the references.
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_GESTION.toString()));
            if (organisme.getId() != null) {
                gestion.setOrganismeId(organisme.getId());
            } else {
                throw new AccessDbImporterException("L'organisme " + organisme + " n'a pas encore d'identifiant CouchDb !");
            }
            
            // Pas d'ID propre car table de jointure : on affecte arbitrairement l'id de l'organisme.
            gestion.setDesignation(String.valueOf(row.getInt(Columns.ID_ORG_GESTION.toString())));
            gestion.setValid(true);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            List<ContactTroncon> listeGestions = gestionsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(listeGestions == null){
                listeGestions = new ArrayList<>();
            }
            listeGestions.add(gestion);
            gestionsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listeGestions);
        }
    }
}
