package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.symadrem.sirs.core.model.GestionTroncon;
import fr.symadrem.sirs.core.model.Organisme;
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

    private Map<Integer, List<GestionTroncon>> gestionsByTronconId = null;
    private OrganismeImporter organismeImporter;

    private TronconGestionDigueGestionnaireImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    TronconGestionDigueGestionnaireImporter(final Database accessDatabase,
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
     * @return A map containing all GestionTroncon instances accessibles from
     * the internal database <em>TronconGestion</em> identifier.
     * @throws IOException
     * @throws fr.sym.util.importer.AccessDbImporterException
     */
    public Map<Integer, List<GestionTroncon>> getGestionsByTronconId() throws IOException, AccessDbImporterException {
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
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            final GestionTroncon gestion = new GestionTroncon();

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
            List<GestionTroncon> listeGestions = gestionsByTronconId.get(row.getInt(TronconGestionDigueGestionnaireColumns.ID_TRONCON_GESTION.toString()));
            if(listeGestions == null){
                listeGestions = new ArrayList<>();
                gestionsByTronconId.put(row.getInt(TronconGestionDigueGestionnaireColumns.ID_TRONCON_GESTION.toString()), listeGestions);
            }
            listeGestions.add(gestion);

            // Set the references.
            final Organisme organisme = organismeImporter.getOrganismes().get(row.getInt(TronconGestionDigueGestionnaireColumns.ID_ORG_GESTION.toString()));
            if (organisme.getId() != null) {
                System.out.println(organisme.getId());
                gestion.setGestionnaireId(organisme.getId());
            } else {
                throw new AccessDbImporterException("L'organisme " + organisme + " n'a pas encore d'identifiant CouchDb !");
            }
        }
    }
}
